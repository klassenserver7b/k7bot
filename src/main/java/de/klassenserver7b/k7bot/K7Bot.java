package de.klassenserver7b.k7bot;

import de.klassenserver7b.k7bot.audio.AudioManager;
import de.klassenserver7b.k7bot.listener.*;
import de.klassenserver7b.k7bot.logging.LoggingFilter;
import de.klassenserver7b.k7bot.manage.*;
import de.klassenserver7b.k7bot.sql.LiteSQL;
import de.klassenserver7b.k7bot.sql.SQLManager;
import de.klassenserver7b.k7bot.subscriptions.SubscriptionManager;
import de.klassenserver7b.k7bot.threads.LoopThread;
import de.klassenserver7b.k7bot.tu.navigator.TUNavigator;
import de.klassenserver7b.k7bot.util.BotState;
import de.klassenserver7b.k7bot.util.StatsCategoryUtil;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.libraries.jda.JDAVoiceUpdateListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class K7Bot {

    private static K7Bot INSTANCE;

    private final Logger logger = LoggerFactory.getLogger("K7Bot-Main");
    private final PropertiesManager propMgr;
    private ShardManager shardMgr;
    private CommandManager cmdMgr;
    private SystemNotificationChannelManager sysChannelMgr;
    private PrefixManager prefixMgr;
    private SubscriptionManager subMgr;
    private SlashCommandManager slashMgr;
    private LoopedEventManager loopedEventMgr;

    private LavalinkClient lavalinkClient;

    public AudioManager getAudioManager() {
        return audioManager;
    }

    private AudioManager audioManager;

    private LoopThread loop;
    private TUNavigator tuNavigator;

    private Long ownerId;
    private BotState state;

    private K7Bot() throws IllegalArgumentException {
        INSTANCE = this;
        this.state = BotState.STARTING;
        this.propMgr = new PropertiesManager();

        if (!propMgr.loadProps() || !propMgr.isBotTokenValid()) {
            return;
        }

        if (!initializeBot()) {
            logger.error("Bot couldn't be initialized - EXITING");
            System.exit(1);
        }

        awaitJDAReady();

        initListeners();
        runLoop();
    }

    /**
     * This method is used to get the Bot Instance.
     * The Bot is managed by this class as a Singleton.
     *
     * @return the K7Bot Instance
     * @throws IllegalArgumentException if something failed while logging into discord
     */
    public static K7Bot getInstance() throws IllegalArgumentException {

        if (INSTANCE == null) {
            return new K7Bot();
        }

        return INSTANCE;
    }

    /**
     * Initialize the Bot.
     *
     * @return if the Bot was successfully initialized
     * @see #buildBot(String, int)
     * @see #initializeObjects()
     * @see LoopedEventManager#initializeDefaultEvents()
     */
    protected boolean initializeBot() {
        LiteSQL.connect();

        SQLManager.onCreate();

        String token = propMgr.getProperty("token");

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
            shardMgr = buildBot(token, shardc);
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
     * The Bot will be built with the specified token
     * The Bot will also be built with the specified shard count.
     * <p>
     * If the config is invalid, the Bot will exit with the specified exit code. @see {@link #invalidConfigExit(String, int, RuntimeException)}
     *
     * @param token  the Bot's token
     * @param shardc the shard count
     * @return the ShardManager
     * @throws IllegalArgumentException if the Bot couldn't be built see {@link DefaultShardManagerBuilder#build()}
     */
    protected ShardManager buildBot(String token, int shardc) throws IllegalArgumentException {

        DefaultShardManagerBuilder builder;

        builder = DefaultShardManagerBuilder.create(token, EnumSet.allOf(GatewayIntent.class));

        builder.setShardsTotal(shardc);
        builder.setMemberCachePolicy(MemberCachePolicy.ALL);
        builder.setActivity(Activity.listening("-help"));

        this.lavalinkClient = LavaLinkManager.initialize(token);
        builder.setVoiceDispatchInterceptor(new JDAVoiceUpdateListener(lavalinkClient));

        builder.setStatus(OnlineStatus.ONLINE);

        builder.addEventListeners(new CommandListener());
        builder.addEventListeners(new SlashCommandListener());
        builder.addEventListeners(LoggingFilter.getInstance());
        builder.addEventListeners(new VoiceListener());
        builder.addEventListeners(new ReactRoleListener());
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
     */
    protected void initializeObjects() {

        this.prefixMgr = new PrefixManager();

        this.subMgr = new SubscriptionManager();
        this.loopedEventMgr = new LoopedEventManager();

        this.sysChannelMgr = new SystemNotificationChannelManager();
        this.audioManager = new AudioManager();

        this.tuNavigator = new TUNavigator();

        this.cmdMgr = new CommandManager();
        this.slashMgr = new SlashCommandManager();
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
        logger.info("Bot started");
        this.state = BotState.RUNNING;
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

    public void shutdown() {

        logger.info("Bot is shutting down!");

        ShardManager shardMgr = K7Bot.getInstance().getShardManager();

        if (shardMgr != null) {

            ArrayList<Object> listeners = new ArrayList<>();

            for (JDA jda : shardMgr.getShards()) {
                listeners.addAll(jda.getEventManager().getRegisteredListeners());
            }

            shardMgr.removeEventListener(listeners.toArray());

            K7Bot.getInstance().stopLoop();

            K7Bot.getInstance().getLoopedEventManager().shutdownLoopedEvents();

            StatsCategoryUtil.onShutdown();

            shardMgr.setStatus(OnlineStatus.OFFLINE);

            shardMgr.shutdown();
            logger.info("Bot offline");

            LiteSQL.disconnect();
            return;

        }

        logger.info("ShardMan was null!");

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

    public TUNavigator getTuNavigator() {
        return tuNavigator;
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
     * @return the {@link BotState state} of the Bot
     */
    public BotState getState() {
        return this.state;
    }

    /**
     * @param state set the {@link BotState state} of the bot
     */
    public void setState(BotState state) {
        this.state = state;
    }

    /**
     * @return the ShardManager
     */
    public ShardManager getShardManager() {
        return this.shardMgr;
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

    public LavalinkClient getLavalinkClient() {
        return this.lavalinkClient;
    }
}
