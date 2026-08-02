/* (C)2026 */
package de.klassenserver7b.k7bot.database.dao;

import de.klassenserver7b.k7bot.database.HibernateManager;
import de.klassenserver7b.k7bot.database.entities.CreatedPrivateVcsEntity;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Data Access Object for handling CreatedPrivateVcsEntity operations. Provides
 * methods for managing dynamically created private voice channels.
 */
public class CreatedPrivateVcsDAO {
	/**
	 * Adds a newly created private voice channel.
	 *
	 * @param guildId   the ID of the guild where the channel was created
	 * @param channelId the ID of the created channel
	 * @return a CompletableFuture that completes when the operation is finished
	 */
	public CompletableFuture<Void> addChannel(Long guildId, Long channelId) {
		return CompletableFuture.runAsync(
				() -> HibernateManager.getSessionFactory()
						.inTransaction(session -> session.persist(new CreatedPrivateVcsEntity(guildId, channelId))),
				HibernateManager.DB_EXECUTOR);
	}

	/**
	 * Removes a created private voice channel by its ID.
	 *
	 * @param channelId the ID of the channel to remove
	 * @return a CompletableFuture that completes when the operation is finished
	 */
	public CompletableFuture<Void> removeChannel(Long channelId) {
		return CompletableFuture.runAsync(() -> HibernateManager.getSessionFactory().inTransaction(session -> {
			CreatedPrivateVcsEntity entity = session.find(CreatedPrivateVcsEntity.class, channelId);
			if (entity != null) {
				session.remove(entity);
			}
		}), HibernateManager.DB_EXECUTOR);
	}

	/**
	 * Retrieves a list of all created private voice channel IDs.
	 *
	 * @return a CompletableFuture containing a list of all private voice channel
	 *         IDs
	 */
	public CompletableFuture<List<Long>> getAllChannelIds() {
		return CompletableFuture.supplyAsync(
				() -> HibernateManager.getSessionFactory().fromTransaction(session -> session
						.createQuery("SELECT channelId FROM CreatedPrivateVcsEntity", Long.class).getResultList()),
				HibernateManager.DB_EXECUTOR);
	}
}
