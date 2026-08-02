/* (C)2026 */
package de.klassenserver7b.k7bot.database;

import de.klassenserver7b.k7bot.database.entities.*;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HibernateManager {
	private static SessionFactory sessionFactory;

	// Executor for thread-safe SQLite operations
	public static final ExecutorService DB_EXECUTOR = Executors.newSingleThreadExecutor();

	public static void init(Configuration configuration) {

		configuration.addAnnotatedClass(ModLogEntity.class);
		configuration.addAnnotatedClass(BotUtilEntity.class);
		configuration.addAnnotatedClass(CommandLogEntity.class);
		configuration.addAnnotatedClass(SlashCommandLogEntity.class);
		configuration.addAnnotatedClass(LoggingConfigEntity.class);
		configuration.addAnnotatedClass(MemeChannelsEntity.class);
		configuration.addAnnotatedClass(CreatedPrivateVcsEntity.class);
		configuration.addAnnotatedClass(MusicLogsEntity.class);
		configuration.addAnnotatedClass(MessageLogsEntity.class);
		configuration.addAnnotatedClass(ReactRolesEntity.class);
		configuration.addAnnotatedClass(UserReactsEntity.class);

		StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder()
				.applySettings(configuration.getProperties());
		sessionFactory = configuration.buildSessionFactory(builder.build());
	}

	public static SessionFactory getSessionFactory() {
		if (sessionFactory == null) {
			throw new IllegalStateException("SessionFactory is not initialized. Call init() first.");
		}
		return sessionFactory;
	}

	@SuppressWarnings("unused")
	public static void shutdown() {
		if (sessionFactory != null) {
			sessionFactory.close();
		}
		DB_EXECUTOR.shutdown();
	}
}
