/* (C)2026 */
package de.klassenserver7b.k7bot.database.dao;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import de.klassenserver7b.k7bot.database.HibernateManager;
import de.klassenserver7b.k7bot.database.entities.UserReactsEntity;
import de.klassenserver7b.k7bot.database.entities.UserReactsId;

/**
 * Data Access Object for handling UserReactsEntity operations. Provides methods
 * for tracking user reactions on messages.
 */
public class UserReactsDAO {
	/**
	 * Checks if a user has already reacted to a message with a specific emote.
	 *
	 * @param userId    the ID of the user
	 * @param guildId   the ID of the guild
	 * @param messageId the ID of the message
	 * @param emote     the emote (emoji) used
	 * @return a CompletableFuture resolving to true if the user has reacted, false
	 *         otherwise
	 */
	@SuppressWarnings("unused")
	public CompletableFuture<Boolean> hasReacted(Long userId, Long guildId, Long messageId, String emote) {
		return CompletableFuture.supplyAsync(() -> HibernateManager.getSessionFactory()
				.fromTransaction(session -> session.createQuery(
						"SELECT 1 FROM UserReactsEntity WHERE userId = :userId AND guildId = :guildId AND messageId = :messageId AND emote = :emote",
						Integer.class).setParameter("userId", userId).setParameter("guildId", guildId)
						.setParameter("messageId", messageId).setParameter("emote", emote).uniqueResultOptional()
						.isPresent()),
				HibernateManager.DB_EXECUTOR);
	}

	/**
	 * Inserts a record indicating that a user reacted to a message with a specific
	 * emote.
	 *
	 * @param userId    the ID of the user
	 * @param guildId   the ID of the guild
	 * @param messageId the ID of the message
	 * @param emote     the emote used
	 * @return a CompletableFuture that completes when the operation is finished
	 */
	public CompletableFuture<Void> insertReaction(Long userId, Long guildId, Long messageId, String emote) {
		return CompletableFuture.runAsync(
				() -> HibernateManager.getSessionFactory().inTransaction(
						session -> session.persist(new UserReactsEntity(userId, guildId, messageId, emote))),
				HibernateManager.DB_EXECUTOR);
	}

	/**
	 * Deletes a user's reaction record for a specific message and emote.
	 *
	 * @param userId    the ID of the user
	 * @param guildId   the ID of the guild
	 * @param messageId the ID of the message
	 * @param emote     the emote used
	 * @return a CompletableFuture that completes when the operation is finished
	 */
	public CompletableFuture<Void> deleteReaction(Long userId, Long guildId, Long messageId, String emote) {
		return CompletableFuture.runAsync(() -> HibernateManager.getSessionFactory().inTransaction(session -> {
			UserReactsEntity entity = session.find(UserReactsEntity.class,
					new UserReactsId(userId, guildId, messageId, emote));
			if (entity != null) {
				session.remove(entity);
			}
		}), HibernateManager.DB_EXECUTOR);
	}

	/**
	 * Retrieves all users who reacted to a specific message with a given emote.
	 *
	 * @param messageId the ID of the message
	 * @param emote     the emote used
	 * @return a CompletableFuture containing a list of user IDs who reacted
	 */
	public CompletableFuture<List<Long>> getUsersByReaction(Long messageId, String emote) {
		return CompletableFuture
				.supplyAsync(() -> HibernateManager.getSessionFactory()
						.fromTransaction(session -> session.createQuery(
								"SELECT userId FROM UserReactsEntity WHERE messageId = :messageId AND emote = :emote",
								Long.class).setParameter("messageId", messageId).setParameter("emote", emote)
								.getResultList()),
						HibernateManager.DB_EXECUTOR);
	}
}
