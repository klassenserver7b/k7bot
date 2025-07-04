package de.klassenserver7b.k7bot;

import com.jagrosh.jlyrics.LyricsClient;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import de.klassenserver7b.k7bot.listener.*;
import de.klassenserver7b.k7bot.logging.LoggingFilter;
import de.klassenserver7b.k7bot.manage.*;
import de.klassenserver7b.k7bot.music.asms.ExtendedLocalAudioSourceManager;
import de.klassenserver7b.k7bot.music.asms.SpotifyAudioSourceManager;
import de.klassenserver7b.k7bot.music.spotify.SpotifyInteractions;
import de.klassenserver7b.k7bot.music.utilities.AudioPlayerUtil;
import de.klassenserver7b.k7bot.music.utilities.gla.GLAWrapper;
import de.klassenserver7b.k7bot.sql.LiteSQL;
import de.klassenserver7b.k7bot.sql.SQLManager;
import de.klassenserver7b.k7bot.subscriptions.SubscriptionManager;
import de.klassenserver7b.k7bot.threads.ConsoleReadThread;
import de.klassenserver7b.k7bot.threads.LoopThread;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class K7Bot {

    private static K7Bot INSTANCE;

    private final Logger logger = LoggerFactory.getLogger("K7Bot-Main");

    private ShardManager shardMgr;
    private CommandManager cmdMgr;

    private SystemNotificationChannelManager sysChannelMgr;
    private PrefixManager prefixMgr;

    private SubscriptionManager subMgr;

    private SlashCommandManager slashMgr;

    private final PropertiesManager propMgr;
    private LoopedEventManager loopedEventMgr;

    private AudioPlayerManager audioPlayerManager;
    private AudioPlayerUtil playerutil;

    private LoopThread loop;
    private ConsoleReadThread shutdownT;
    private LyricsClient lyricsapi;
    private GLAWrapper lyricsapiold;
    private SpotifyInteractions spotifyinteractions;

    private Long ownerId;
    private boolean exit = false;
    private boolean indev;

    private K7Bot(boolean indev) throws IllegalArgumentException {
        INSTANCE = this;
        this.indev = indev;
        this.propMgr = new PropertiesManager();

        if (!propMgr.loadProps() || !propMgr.isBotTokenValid()) {
            return;
        }

        if (!initializeBot()) {
            logger.error("Bot couldn't be initialized - EXITING");
            System.exit(1);
        }

        awaitJDAReady();

        runShutdown();

        initListeners();
        runLoop();
    }

    /**
     * Initialize the Bot.
     *
     * @return if the Bot was successfully initialized
     * @see #buildBot(String, String, int)
     * @see #initializeObjects()
     * @see LoopedEventManager#initializeDefaultEvents()
     */
    protected boolean initializeBot() {
        LiteSQL.connect();

        SQLManager.onCreate();

        String token = propMgr.getProperty("token");

        String canaryToken;
        if ((canaryToken = propMgr.getProperty("canary-token")) == null) {
            this.indev = false;
        }

        String shards;
        int shardc;

        if ((shards = propMgr.getProperty("shardCount")) != null && !shards.equalsIgnoreCase("")) {
            shardc = Integer.parseInt(shards);
        } else {
            shardc = -1;
            this.logger.info("Empty Shard-Count Config");
        }

        this.ownerId = Long.valueOf(propMgr.getProperty("ownerId"));

        try {
            shardMgr = buildBot(token, canaryToken, shardc);
        } catch (IllegalArgumentException e) {
            invalidConfigExit("Couldn't start Bot! - EXITING", 1, e);
            return false;
        }

        propMgr.checkAPIProps();
        initializeObjects();
        loopedEventMgr.initializeDefaultEvents();

        return true;
    }

    /**
     * Build the Bot.
     * The Bot will be built with the specified token or canary token.
     * The Bot will also be built with the specified shard count.
     * <p>
     * If the config is invalid, the Bot will exit with the specified exit code. @see {@link #invalidConfigExit(String, int, RuntimeException)}
     *
     * @param token       the Bot's token
     * @param canaryToken the Bot's canary token
     * @param shardc      the shard count
     * @return the ShardManager
     * @throws IllegalArgumentException if the Bot couldn't be built see {@link DefaultShardManagerBuilder#build()}
     */
    protected ShardManager buildBot(String token, String canaryToken, int shardc) throws IllegalArgumentException {

        DefaultShardManagerBuilder builder;

        if (!indev) {
            builder = DefaultShardManagerBuilder.create(token, EnumSet.allOf(GatewayIntent.class));
        } else {
            builder = DefaultShardManagerBuilder.create(canaryToken, EnumSet.allOf(GatewayIntent.class));
        }

        //builder.setAudioSendFactory(new NativeAudioSendFactory(400));
        builder.setShardsTotal(shardc);
        builder.setMemberCachePolicy(MemberCachePolicy.ALL);
        builder.setActivity(Activity.listening("-help"));

        builder.setStatus(OnlineStatus.ONLINE);

        builder.setAudioSendFactory(new NativeAudioSendFactory());

        builder.addEventListeners(new CommandListener());
        builder.addEventListeners(new SlashCommandListener());
        builder.addEventListeners(LoggingFilter.getInstance());
        builder.addEventListeners(new VoiceListener());
        builder.addEventListeners(new ReactRoleListener());
        builder.addEventListeners(new AutoRickroll());
        builder.addEventListeners(new MemesReact());
        builder.addEventListeners(new BotLeaveGuildListener());
        builder.addEventListeners(new MessageListener());

        ShardManager initShardMgr = null;

        try {
            initShardMgr = builder.build();
        } catch (InvalidTokenException e) {
            invalidConfigExit("INVALID TOKEN - EXITING", 5, e);
        } catch (IllegalArgumentException e) {
            invalidConfigExit("ILLEGAL LOGIN ARGUMENT / EMPTY TOKEN - EXITING", 1, e);
        }

        if (initShardMgr != null) {
            return initShardMgr;
        } else {
            throw new IllegalArgumentException("BOT CREATION FAILED - EXITED", new Throwable().fillInStackTrace());
        }
    }

    /**
     * Initialize the Objects that require an initialization.
     * The Objects are initialized and the Bot will log the result.
     * <p>
     * While initializing the Objects, the Bot will also initialize the Music Configuration.
     *
     * @see #InitializeMusic(AudioPlayerManager)
     */
    protected void initializeObjects() {

        this.prefixMgr = new PrefixManager();

        this.subMgr = new SubscriptionManager();
        this.loopedEventMgr = new LoopedEventManager();

        this.sysChannelMgr = new SystemNotificationChannelManager();

        this.audioPlayerManager = new DefaultAudioPlayerManager();
        this.playerutil = new AudioPlayerUtil();

        this.lyricsapi = new LyricsClient("Genius");
        this.lyricsapiold = new GLAWrapper();

        this.spotifyinteractions = new SpotifyInteractions();

        InitializeMusic(this.audioPlayerManager);

        this.cmdMgr = new CommandManager();
        this.slashMgr = new SlashCommandManager();
    }

    /**
     * Initialize the Music Configuration.
     * The Music Configuration is used to play Music in the Bot.
     *
     * @param manager the AudioPlayerManager
     */
    public void InitializeMusic(AudioPlayerManager manager) {

        spotifyinteractions.initialize();

        manager.getConfiguration().setFilterHotSwapEnabled(true);
        manager.registerSourceManager(new SpotifyAudioSourceManager());
        manager.registerSourceManager(new ExtendedLocalAudioSourceManager());
        AudioSourceManagers.registerRemoteSources(manager);

    }

    /**
     * Await the JDA to be ready.
     * The Bot will await the JDA to be ready for all Shards.
     */
    public void awaitJDAReady() {

        logger.info("Awaiting jda ready");
        shardMgr.getShards().forEach(jda -> {

            try {
                logger.debug("Awaiting jda ready for shard: {}", jda.getShardInfo());
                jda.awaitReady();
            } catch (InterruptedException e) {
                logger.info("could not start shardInfo: {} and Self-Username :{}", jda.getShardInfo(), jda.getSelfUser().getName());
                logger.error(e.getMessage(), e);
            }

        });
        if (!this.indev) {
            logger.info("Bot was started (nondev)");
        } else {
            logger.info("Bot was started in Canary mode");
        }
    }

    /**
     * Initialize the Listeners that require an initialization.
     * The Listeners are initialized and the Bot will log the result.
     */
    protected void initListeners() {

        HashMap<CompletableFuture<Integer>, InitRequiringListener> futures = new HashMap<>();

        for (JDA jda : shardMgr.getShards()) {
            for (Object eventlistener : jda.getEventManager().getRegisteredListeners()) {
                if (eventlistener instanceof InitRequiringListener listener) {
                    futures.put(listener.initialize(), listener);
                }
            }
        }

        for (CompletableFuture<Integer> future : futures.keySet()) {
            int code;
            try {
                code = future.get();
            } catch (InterruptedException | ExecutionException e) {
                logger.error(e.getMessage(), e);
                return;
            }

            if (code != 0) {
                logger.warn("{} failed to initialize, ExitCode: {}", futures.get(future).getClass().getSimpleName(), code);
                continue;
            }

            logger.info("{} successfully initialized", futures.get(future).getClass().getSimpleName());
        }

    }

    /**
     * The Error-Handling method for invalid Configurations.
     * The Bot will log the error and open the bot.properties file in the resources folder.
     * The Bot will then exit with the specified exit code.
     *
     * @param message  the error message
     * @param exitCode the exit code
     * @param e        the exception to throw and log
     */
    protected void invalidConfigExit(String message, int exitCode, RuntimeException e) {
        logger.error(message, e);
        try {
            Desktop.getDesktop().open(new File("resources/bot.properties"));
        } catch (IOException e1) {
            // EXIT AS USUAL;
        }
        System.exit(exitCode);
    }

    /**
     * Shut down the Bot.
     * The Bot will stop all Threads and disconnect from Discord.
     */
    protected void runShutdown() {
        this.shutdownT = new ConsoleReadThread();
    }

    /**
     * Start the LoopThread.
     */
    public void runLoop() {
        this.loop = new LoopThread();
    }

    /**
     * Restart the LoopThread.
     */
    public void restartLoop() {
        this.loop.restart();
    }

    /**
     * Stop the LoopThread.
     */
    public void stopLoop() {
        this.loop.stopLoop();
    }

    /**
     * This method is used to get the Bot's Name.
     * If the Bot is in a Guild, the Bots custom Guildname is returned.
     * Otherwise, the Bot's global Name is returned.
     *
     * @param guildid the Guild's ID
     * @return the Bot's Name
     */
    public String getSelfName(Long guildid) {

        Guild g;

        if (guildid != null && (g = K7Bot.getInstance().getShardManager().getGuildById(guildid)) != null) {
            return g.getSelfMember().getEffectiveName();
        }

        return K7Bot.getInstance().getShardManager().getShards().getFirst().getSelfUser().getEffectiveName();
    }


    /**
     * This method is used to get the Bot Instance.
     * The Bot is managed by this class as a Singleton.
     * see {@link #getInstance(boolean)}
     *
     * @return the Bot Instance
     * @throws IllegalArgumentException if something failed while logging into discord
     */
    public static K7Bot getInstance() throws IllegalArgumentException {
        return getInstance(false);
    }

    /**
     * This method is used to get the Bot Instance with the specified canary mode.
     * The Bot is managed by this class as a Singleton.
     *
     * @param indev if the Bot should start in canary mode
     * @return the K7Bot Instance
     * @throws IllegalArgumentException if something failed while logging into discord
     */
    public static K7Bot getInstance(boolean indev) throws IllegalArgumentException {

        if (INSTANCE == null) {
            return new K7Bot(indev);
        }

        return INSTANCE;
    }

    /**
     * @return the CommandManager
     */
    public CommandManager getCmdMan() {
        return this.cmdMgr;
    }

    /**
     * @return the SlashCommandManager
     */
    public SlashCommandManager getslashMan() {
        return this.slashMgr;
    }

    /**
     * @return the JLyricsAPI
     */
    public LyricsClient getLyricsAPI() {
        return this.lyricsapi;
    }

    /**
     * @return the GeniusLyricsAPI
     */
    public GLAWrapper getLyricsAPIold() {
        return this.lyricsapiold;
    }

    /**
     * @return the MainLogger
     */
    public Logger getMainLogger() {
        return this.logger;
    }

    /**
     * @return the OwnerId
     */
    public Long getOwnerId() {
        return this.ownerId;
    }

    /**
     * @return the SystemNotificationChannelManager
     */
    public SystemNotificationChannelManager getSysChannelMgr() {
        return sysChannelMgr;
    }

    /**
     * @return if the Bot is currently exiting
     */
    public boolean isInExit() {
        return this.exit;
    }

    /**
     * @return if the Bot is in canary mode
     */
    public boolean isDevMode() {
        return this.indev;
    }

    /**
     * @return the ShardManager
     */
    public ShardManager getShardManager() {
        return this.shardMgr;
    }

    /**
     * @return the AudioPlayerUtil
     */
    public AudioPlayerUtil getPlayerUtil() {
        return this.playerutil;
    }

    /**
     * @return the AudioPlayerManager
     */
    public AudioPlayerManager getAudioPlayerManager() {
        return this.audioPlayerManager;
    }

    /**
     * @return the SubscriptionManager
     */
    public SubscriptionManager getSubscriptionManager() {
        return this.subMgr;
    }

    /**
     * @return the PropertiesManager
     */
    public PropertiesManager getPropertiesManager() {
        return this.propMgr;
    }

    /**
     * @return the LoopedEventManager
     */
    public LoopedEventManager getLoopedEventManager() {
        return this.loopedEventMgr;
    }

    /**
     * @return the PrefixManager
     */
    public PrefixManager getPrefixMgr() {
        return this.prefixMgr;
    }

    /**
     * @return the shutdownThread
     */
    public ConsoleReadThread getShutdownThread() {
        return this.shutdownT;
    }

    /**
     * @param exit set if the Bot is currently exiting
     */
    public void setExit(boolean exit) {
        this.exit = exit;
    }

    /**
     * @return the SpotifyInteractions
     */
    public SpotifyInteractions getSpotifyinteractions() {
        return spotifyinteractions;
    }
}
