/* (C)2026 */
package de.klassenserver7b.k7bot.database.dao;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import de.klassenserver7b.k7bot.database.HibernateManager;
import de.klassenserver7b.k7bot.database.entities.ModLogEntity;

/**
 * Data Access Object for handling ModLogEntity operations. Provides methods for
 * managing moderation logs.
 */
public class ModLogDAO {

	/**
	 * Saves a new moderation log entry.
	 *
	 * @param log the ModLogEntity to save
	 * @return a CompletableFuture that completes when the operation is finished
	 */
	public CompletableFuture<Void> saveLog(ModLogEntity log) {
		return CompletableFuture.runAsync(
				() -> HibernateManager.getSessionFactory().inTransaction(session -> session.persist(log)),
				HibernateManager.DB_EXECUTOR);
	}

	/**
	 * Retrieves moderation logs for a specific member in a guild.
	 *
	 * @param guildId  the ID of the guild
	 * @param memberId the ID of the member
	 * @return a CompletableFuture containing a list of moderation logs for the
	 *         member
	 */
	public CompletableFuture<List<ModLogEntity>> getLogsByMember(Long guildId, Long memberId) {
		return CompletableFuture.supplyAsync(() -> HibernateManager.getSessionFactory()
				.fromTransaction(session -> session
						.createQuery("FROM ModLogEntity WHERE guildId = :guildId AND memberId = :memberId",
								ModLogEntity.class)
						.setParameter("guildId", guildId).setParameter("memberId", memberId).getResultList()),
				HibernateManager.DB_EXECUTOR);
	}

	/**
	 * Retrieves moderation logs created by a specific requester (moderator) in a
	 * guild.
	 *
	 * @param guildId     the ID of the guild
	 * @param requesterId the ID of the moderator who requested the action
	 * @return a CompletableFuture containing a list of moderation logs by the
	 *         requester
	 */
	public CompletableFuture<List<ModLogEntity>> getLogsByRequester(Long guildId, Long requesterId) {
		return CompletableFuture
				.supplyAsync(
						() -> HibernateManager.getSessionFactory()
								.fromTransaction(session -> session.createQuery(
										"FROM ModLogEntity WHERE guildId = :guildId AND requesterId = :requesterId",
										ModLogEntity.class).setParameter("guildId", guildId)
										.setParameter("requesterId", requesterId).getResultList()),
						HibernateManager.DB_EXECUTOR);
	}

	/**
	 * Retrieves all moderation logs for a specific guild.
	 *
	 * @param guildId the ID of the guild
	 * @return a CompletableFuture containing a list of all moderation logs for the
	 *         guild
	 */
	@SuppressWarnings("unused")
	public CompletableFuture<List<ModLogEntity>> getLogsByGuild(Long guildId) {
		return CompletableFuture
				.supplyAsync(
						() -> HibernateManager.getSessionFactory()
								.fromTransaction(session -> session
										.createQuery("FROM ModLogEntity WHERE guildId = :guildId", ModLogEntity.class)
										.setParameter("guildId", guildId).getResultList()),
						HibernateManager.DB_EXECUTOR);
	}
}
