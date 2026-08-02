/* (C)2026 */
package de.klassenserver7b.k7bot.database.dao;

import java.util.concurrent.CompletableFuture;

import de.klassenserver7b.k7bot.database.HibernateManager;
import de.klassenserver7b.k7bot.database.entities.MessageLogsEntity;

/**
 * Data Access Object for handling MessageLogsEntity operations. Provides
 * methods for storing and retrieving logged messages.
 */
public class MessageLogsDAO {
	/**
	 * Retrieves a logged message by its ID.
	 *
	 * @param messageId the ID of the message to retrieve
	 * @return a CompletableFuture containing the MessageLogsEntity, or null if not
	 *         found
	 */
	public CompletableFuture<MessageLogsEntity> getLog(Long messageId) {
		return CompletableFuture.supplyAsync(
				() -> HibernateManager.getSessionFactory()
						.fromTransaction(session -> session.find(MessageLogsEntity.class, messageId)),
				HibernateManager.DB_EXECUTOR);
	}

	/**
	 * Deletes all logged messages older than the specified timestamp.
	 *
	 * @param timestamp the timestamp threshold; messages older than this will be
	 *                  deleted
	 * @return a CompletableFuture that completes when the operation is finished
	 */
	public CompletableFuture<Void> deleteOlderThan(Long timestamp) {
		return CompletableFuture
				.runAsync(() -> HibernateManager.getSessionFactory()
						.inTransaction(session -> session
								.createMutationQuery("DELETE FROM MessageLogsEntity WHERE timestamp < :ts")
								.setParameter("ts", timestamp).executeUpdate()),
						HibernateManager.DB_EXECUTOR);
	}

	/**
	 * Inserts a new logged message.
	 *
	 * @param messageId   the ID of the message
	 * @param guildId     the ID of the guild where the message was sent
	 * @param timestamp   the timestamp of the message
	 * @param authorId    the ID of the message author
	 * @param messageText the content of the message
	 * @return a CompletableFuture that completes when the operation is finished
	 */
	public CompletableFuture<Void> insertLog(Long messageId, Long guildId, Long timestamp, Long authorId,
			String messageText) {
		return CompletableFuture
				.runAsync(
						() -> HibernateManager.getSessionFactory()
								.inTransaction(session -> session.persist(
										new MessageLogsEntity(messageId, guildId, timestamp, authorId, messageText))),
						HibernateManager.DB_EXECUTOR);
	}
}
