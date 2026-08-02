/* (C)2026 */
package de.klassenserver7b.k7bot.database.dao;

import de.klassenserver7b.k7bot.database.HibernateManager;
import de.klassenserver7b.k7bot.database.entities.MemeChannelsEntity;

import java.util.concurrent.CompletableFuture;

/**
 * Data Access Object for handling MemeChannelsEntity operations. Provides
 * methods for managing channels designated for memes.
 */
public class MemeChannelDAO {
	/**
	 * Adds a channel to the list of meme channels.
	 *
	 * @param channelId the ID of the channel to add
	 * @return a CompletableFuture that completes when the operation is finished
	 */
	public CompletableFuture<Void> addChannel(Long channelId) {
		return CompletableFuture.runAsync(() -> HibernateManager.getSessionFactory().inTransaction(session -> {
			if (session.find(MemeChannelsEntity.class, channelId) == null) {
				session.persist(new MemeChannelsEntity(channelId));
			}
		}), HibernateManager.DB_EXECUTOR);
	}

	/**
	 * Removes a channel from the list of meme channels.
	 *
	 * @param channelId the ID of the channel to remove
	 * @return a CompletableFuture that completes when the operation is finished
	 */
	public CompletableFuture<Void> removeChannel(Long channelId) {
		return CompletableFuture.runAsync(() -> HibernateManager.getSessionFactory().inTransaction(session -> {
			MemeChannelsEntity entity = session.find(MemeChannelsEntity.class, channelId);
			if (entity != null)
				session.remove(entity);
		}), HibernateManager.DB_EXECUTOR);
	}

	/**
	 * Checks if a specific channel is designated as a meme channel.
	 *
	 * @param channelId the ID of the channel to check
	 * @return a CompletableFuture resolving to true if the channel is a meme
	 *         channel, false otherwise
	 */
	public CompletableFuture<Boolean> isMemeChannel(Long channelId) {
		return CompletableFuture.supplyAsync(
				() -> HibernateManager.getSessionFactory()
						.fromTransaction(session -> session.find(MemeChannelsEntity.class, channelId) != null),
				HibernateManager.DB_EXECUTOR);
	}
}
