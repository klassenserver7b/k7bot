/* (C)2026 */
package de.klassenserver7b.k7bot;

import de.klassenserver7b.k7bot.audio.AudioManager;
import de.klassenserver7b.k7bot.database.Database;
import de.klassenserver7b.k7bot.database.DatabaseType;
import de.klassenserver7b.k7bot.database.SQLManager;
import de.klassenserver7b.k7bot.database.config.SqliteConfig;
import de.klassenserver7b.k7bot.exceptions.InvalidConfigException;
import de.klassenserver7b.k7bot.listener.*;
import de.klassenserver7b.k7bot.logging.LoggingFilter;
import de.klassenserver7b.k7bot.manage.*;
import de.klassenserver7b.k7bot.threads.LoopThread;
import de.klassenserver7b.k7bot.tu.navigator.TUNavigator;
import de.klassenserver7b.k7bot.util.BotState;
import de.klassenserver7b.k7bot.util.InternalStatusCodes;
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

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class K7Bot {

	private static K7Bot INSTANCE;

	private final Logger logger = LoggerFactory.getLogger("K7Bot-Main");

	private final ConfigManager cfgMgr;
	private Database db;

	private ShardManager shardMgr;
	private CommandManager cmdMgr;
	private SystemNotificationChannelManager sysChannelMgr;
	private PrefixManager prefixMgr;
	private SlashCommandManager slashMgr;
	private LoopedEventManager loopedEventMgr;

	private LavalinkClient lavalinkClient;

	private AudioManager audioManager;

	private LoopThread loop;
	private TUNavigator tuNavigator;

	private long ownerId;
	private BotState state;

	private K7Bot() throws IllegalArgumentException, InvalidConfigException {
		INSTANCE = this;
		this.state = BotState.STARTING;

		this.cfgMgr = new ConfigManager();
		this.cfgMgr.validate();

		try {
			setupDB();
		} catch (SQLException | IOException e) {
			logger.error("Failed to setup database", e);
			System.exit(InternalStatusCodes.SQL_ERROR.getId());
		}
		initializeBot();

		awaitJDAReady();

		initListeners();
		runLoop();
	}

	/**
	 * This method is used to get the Bot Instance. The Bot is managed by this class
	 * as a Singleton.
	 *
	 * @return the K7Bot Instance
	 * @throws IllegalArgumentException if something failed while logging into
	 *                                  discord
	 */
	public static K7Bot getInstance() throws IllegalArgumentException {

		if (INSTANCE == null) {
			return new K7Bot();
		}

		return INSTANCE;
	}

	protected void setupDB() throws SQLException, IOException {

		SqliteConfig cfg = new SqliteConfig(
				new File(cfgMgr.getToml().getString("global/sqlite_path").orElse("resources/k7bot.db")));
		this.db = DatabaseType.createFor(cfg);

		db.connect(cfg);
		SQLManager.onCreate();
	}

	/**
	 * Initialize the Bot.
	 *
	 * @see #buildShardManager(String, int)
	 * @see #initializeObjects()
	 * @see LoopedEventManager#initializeDefaultEvents()
	 */
	protected void initializeBot() throws InvalidConfigException, IllegalArgumentException {

		String token = cfgMgr.getEnvVar(ConfigManager.BOT_TOKEN_ENV_NAME);

		int shardCount = Math.toIntExact(cfgMgr.getToml().getLong("global/shardCount").orElse(-1L));
		this.ownerId = cfgMgr.getToml().getLong("global/ownerId").orElse(-1L);

		try {
			shardMgr = buildShardManager(token, shardCount);
		} catch (InvalidTokenException e) {
			throw new InvalidConfigException("Invalid Bot Token");
		}

		initializeObjects();
		loopedEventMgr.initializeDefaultEvents();
	}

	/**
	 * Build the ShardManager. The Bot will be built with the specified token The
	 * Bot will also be built with the specified shard count.
	 * <p>
	 * If the config is invalid, the Bot will exit with the specified exit code.
	 *
	 * @param token      the Bot's token
	 * @param shardCount the shard count
	 * @return the ShardManager
	 * @throws IllegalArgumentException if the Bot couldn't be built
	 * @throws InvalidTokenException    if the token is invalid
	 * @see DefaultShardManagerBuilder#build()
	 */
	protected ShardManager buildShardManager(String token, int shardCount)
			throws InvalidTokenException, IllegalArgumentException {

		DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.create(token,
				EnumSet.allOf(GatewayIntent.class));

		builder.setShardsTotal(shardCount);
		builder.setMemberCachePolicy(MemberCachePolicy.ALL);
		builder.setActivity(Activity.listening("-help"));

		this.lavalinkClient = LavaLinkManager.initialize(token);
		builder.setVoiceDispatchInterceptor(new JDAVoiceUpdateListener(lavalinkClient));

		builder.setStatus(OnlineStatus.ONLINE);

		builder.addEventListeners(new JDAReconnectListener());
		builder.addEventListeners(new CommandListener());
		builder.addEventListeners(new SlashCommandListener());
		builder.addEventListeners(LoggingFilter.getInstance());
		builder.addEventListeners(new VoiceListener());
		builder.addEventListeners(new ReactRoleListener());
		builder.addEventListeners(new MemesReact());
		builder.addEventListeners(new BotLeaveGuildListener());
		builder.addEventListeners(new MessageListener());

		return builder.build();
	}

	/**
	 * Initialize the Objects that require an initialization. The Objects are
	 * initialized and the Bot will log the result.
	 */
	protected void initializeObjects() {

		this.prefixMgr = new PrefixManager();

		this.loopedEventMgr = new LoopedEventManager();

		this.sysChannelMgr = new SystemNotificationChannelManager();
		this.audioManager = new AudioManager();

		this.tuNavigator = new TUNavigator();

		this.cmdMgr = new CommandManager();
		this.slashMgr = new SlashCommandManager();
	}

	/**
	 * Await the JDA to be ready. The Bot will await the JDA to be ready for all
	 * Shards.
	 */
	public void awaitJDAReady() {

		logger.info("Awaiting jda ready");

		shardMgr.getShards().forEach(jda -> {
			try {
				logger.debug("Awaiting jda ready for shard: {}", jda.getShardInfo());
				jda.awaitReady();
			} catch (InterruptedException e) {
				logger.warn("could not start shardInfo: {} and Self-Username :{}", jda.getShardInfo(),
						jda.getSelfUser().getName());
				logger.error(e.getMessage(), e);
			}
		});
		logger.info("Bot started");
		this.state = BotState.RUNNING;
	}

	/**
	 * Initialize the Listeners that require an initialization. The Listeners are
	 * initialized and the Bot will log the result.
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
				logger.warn("{} failed to initialize, ExitCode: {}", futures.get(future).getClass().getSimpleName(),
						code);
				continue;
			}

			logger.info("{} successfully initialized", futures.get(future).getClass().getSimpleName());
		}
	}

	@SuppressWarnings("unused")
	public void shutdown() {

		logger.info("Bot is shutting down!");

		ShardManager shardMgr = K7Bot.getInstance().getShardManager();

		if (shardMgr != null) {

			K7Bot.getInstance().stopLoop();
			K7Bot.getInstance().getLoopedEventManager().shutdownLoopedEvents();

			shardMgr.setStatus(OnlineStatus.OFFLINE);

			shardMgr.shutdown();
			logger.info("Bot offline");

			this.db.disconnect();
			return;
		}

		INSTANCE = null;
		logger.info("ShardMan was null!");
	}

	public void restart() {
		this.db.disconnect();
		this.stopLoop();
		K7Bot.getInstance().getLoopedEventManager().shutdownLoopedEvents();

		this.shardMgr.restart();
	}

	/**
	 * Start the LoopThread.
	 */
	protected void runLoop() {
		this.loop = new LoopThread();
	}

	/**
	 * Stop the LoopThread.
	 */
	protected void stopLoop() {
		this.loop.stopLoop();
	}

	/**
	 * This method is used to get the Bot's Name. If the Bot is in a Guild, the Bots
	 * custom Guildname is returned. Otherwise, the Bot's global Name is returned.
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
	@SuppressWarnings("unused")
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
	@SuppressWarnings("unused")
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
	 * @return the ConfigManager
	 */
	public ConfigManager getConfigManager() {
		return this.cfgMgr;
	}

	/**
	 * @return the Database Connection
	 */
	public Database getDb() {
		return db;
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

	public AudioManager getAudioManager() {
		return audioManager;
	}
}
