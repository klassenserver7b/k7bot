/* (C)2026 */
package de.klassenserver7b.k7bot.database.dao;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import de.klassenserver7b.k7bot.database.HibernateManager;
import de.klassenserver7b.k7bot.database.entities.BotUtilEntity;

/**
 * Data Access Object for handling BotUtilEntity operations. Provides methods
 * for retrieving and updating bot configuration.
 */
@SuppressWarnings("unused")
public class BotUtilDAO {

	/**
	 * Retrieves all bot configurations.
	 *
	 * @return a CompletableFuture containing a list of all BotUtilEntity
	 *         configurations
	 */
	public CompletableFuture<List<BotUtilEntity>> getAll() {
		return CompletableFuture
				.supplyAsync(
						() -> HibernateManager.getSessionFactory()
								.fromTransaction(session -> session
										.createQuery("FROM BotUtilEntity", BotUtilEntity.class).getResultList()),
						HibernateManager.DB_EXECUTOR);
	}

	/**
	 * Updates the bot prefix for a specific guild. Creates a new configuration
	 * entry if one does not exist.
	 *
	 * @param guildId the ID of the guild
	 * @param prefix  the new prefix to set
	 * @return a CompletableFuture that completes when the operation is finished
	 */
	public CompletableFuture<Void> updatePrefix(Long guildId, String prefix) {
		return CompletableFuture.runAsync(() -> HibernateManager.getSessionFactory().inTransaction(session -> {
			BotUtilEntity entity = session.find(BotUtilEntity.class, guildId);
			if (entity == null) {
				entity = new BotUtilEntity(guildId, null, prefix);
				session.persist(entity);
			} else {
				entity.setPrefix(prefix);
			}
		}), HibernateManager.DB_EXECUTOR);
	}

	/**
	 * Updates the system channel ID for a specific guild. Creates a new
	 * configuration entry if one does not exist.
	 *
	 * @param guildId      the ID of the guild
	 * @param syschannelId the new system channel ID to set
	 * @return a CompletableFuture that completes when the operation is finished
	 */
	public CompletableFuture<Void> updateSysChannelId(Long guildId, Long syschannelId) {
		return CompletableFuture.runAsync(() -> HibernateManager.getSessionFactory().inTransaction(session -> {
			BotUtilEntity entity = session.find(BotUtilEntity.class, guildId);
			if (entity == null) {
				entity = new BotUtilEntity(guildId, syschannelId, "-");
				session.persist(entity);
			} else {
				entity.setSyschannelId(syschannelId);
			}
		}), HibernateManager.DB_EXECUTOR);
	}

	/**
	 * Retrieves the configuration for a specific guild.
	 *
	 * @param guildId the ID of the guild
	 * @return a CompletableFuture containing the BotUtilEntity or null if not found
	 */
	public CompletableFuture<BotUtilEntity> get(Long guildId) {
		return CompletableFuture.supplyAsync(
				() -> HibernateManager.getSessionFactory()
						.fromTransaction(session -> session.find(BotUtilEntity.class, guildId)),
				HibernateManager.DB_EXECUTOR);
	}

	/**
	 * Deletes the configuration for a specific guild.
	 *
	 * @param guildId the ID of the guild configuration to delete
	 * @return a CompletableFuture that completes when the operation is finished
	 */
	public CompletableFuture<Void> deleteByGuildId(Long guildId) {
		return CompletableFuture.runAsync(() -> HibernateManager.getSessionFactory().inTransaction(session -> {
			BotUtilEntity entity = session.find(BotUtilEntity.class, guildId);
			if (entity != null) {
				session.remove(entity);
			}
		}), HibernateManager.DB_EXECUTOR);
	}
}
