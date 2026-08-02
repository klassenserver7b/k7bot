/* (C)2026 */
package de.klassenserver7b.k7bot.database.dao;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import de.klassenserver7b.k7bot.database.HibernateManager;
import de.klassenserver7b.k7bot.database.entities.ReactRolesEntity;
import de.klassenserver7b.k7bot.database.entities.ReactRolesId;

/**
 * Data Access Object for handling ReactRolesEntity operations. Provides methods
 * for managing reaction roles.
 */
public class ReactRolesDAO {
	/**
	 * Adds a new reaction role configuration.
	 *
	 * @param guildId   the ID of the guild
	 * @param channelId the ID of the channel where the message is
	 * @param messageId the ID of the message to react to
	 * @param emote     the emote (emoji) used for the reaction
	 * @param roleId    the ID of the role to assign when reacted
	 * @return a CompletableFuture that completes when the operation is finished
	 */
	public CompletableFuture<Void> addRole(Long guildId, Long channelId, Long messageId, String emote, Long roleId) {
		return CompletableFuture
				.runAsync(
						() -> HibernateManager.getSessionFactory()
								.inTransaction(session -> session
										.persist(new ReactRolesEntity(guildId, channelId, messageId, emote, roleId))),
						HibernateManager.DB_EXECUTOR);
	}

	/**
	 * Deletes all reaction roles associated with a specific guild.
	 *
	 * @param guildId the ID of the guild
	 * @return a CompletableFuture that completes when the operation is finished
	 */
	@SuppressWarnings("UnusedReturnValue")
	public CompletableFuture<Void> deleteByGuildId(Long guildId) {
		return CompletableFuture
				.runAsync(
						() -> HibernateManager.getSessionFactory()
								.inTransaction(session -> session
										.createMutationQuery("DELETE FROM ReactRolesEntity WHERE guildId = :guildId")
										.setParameter("guildId", guildId).executeUpdate()),
						HibernateManager.DB_EXECUTOR);
	}

	/**
	 * Deletes all reaction roles associated with a specific channel.
	 *
	 * @param channelId the ID of the channel
	 * @return a CompletableFuture that completes when the operation is finished
	 */
	@SuppressWarnings("UnusedReturnValue")
	public CompletableFuture<Void> deleteByChannelId(Long channelId) {
		return CompletableFuture.runAsync(() -> HibernateManager.getSessionFactory()
				.inTransaction(session -> session
						.createMutationQuery("DELETE FROM ReactRolesEntity WHERE channelId = :channelId")
						.setParameter("channelId", channelId).executeUpdate()),
				HibernateManager.DB_EXECUTOR);
	}

	/**
	 * Deletes all reaction roles associated with a specific role.
	 *
	 * @param roleId the ID of the role
	 * @return a CompletableFuture that completes when the operation is finished
	 */
	@SuppressWarnings("UnusedReturnValue")
	public CompletableFuture<Void> deleteByRoleId(Long roleId) {
		return CompletableFuture
				.runAsync(() -> HibernateManager.getSessionFactory()
						.inTransaction(session -> session
								.createMutationQuery("DELETE FROM ReactRolesEntity WHERE roleId = :roleId")
								.setParameter("roleId", roleId).executeUpdate()),
						HibernateManager.DB_EXECUTOR);
	}

	/**
	 * Retrieves all reaction roles configured for a specific message.
	 *
	 * @param messageId the ID of the message
	 * @return a CompletableFuture containing a list of reaction roles for the
	 *         message
	 */
	@SuppressWarnings("unused")
	public CompletableFuture<List<ReactRolesEntity>> getRolesByMessage(Long messageId) {
		return CompletableFuture.supplyAsync(() -> HibernateManager.getSessionFactory()
				.fromTransaction(session -> session
						.createQuery("FROM ReactRolesEntity WHERE messageId = :messageId", ReactRolesEntity.class)
						.setParameter("messageId", messageId).getResultList()),
				HibernateManager.DB_EXECUTOR);
	}

	/**
	 * Retrieves a specific reaction role configuration based on the message and
	 * emote.
	 *
	 * @param guildId   the ID of the guild
	 * @param channelId the ID of the channel
	 * @param messageId the ID of the message
	 * @param emote     the emote used for the reaction
	 * @return a CompletableFuture containing the reaction role entity, or null if
	 *         not found
	 */
	public CompletableFuture<ReactRolesEntity> getRole(Long guildId, Long channelId, Long messageId, String emote) {
		return CompletableFuture
				.supplyAsync(
						() -> HibernateManager.getSessionFactory()
								.fromTransaction(session -> session.find(ReactRolesEntity.class,
										new ReactRolesId(guildId, channelId, messageId, emote))),
						HibernateManager.DB_EXECUTOR);
	}

	/**
	 * Retrieves all configured reaction roles across all guilds.
	 *
	 * @return a CompletableFuture containing a list of all reaction role
	 *         configurations
	 */
	public CompletableFuture<List<ReactRolesEntity>> getAllRoles() {
		return CompletableFuture
				.supplyAsync(
						() -> HibernateManager.getSessionFactory()
								.fromTransaction(session -> session
										.createQuery("FROM ReactRolesEntity", ReactRolesEntity.class).getResultList()),
						HibernateManager.DB_EXECUTOR);
	}
}
