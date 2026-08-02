/* (C)2026 */
package de.klassenserver7b.k7bot.database.dao;

import java.util.concurrent.CompletableFuture;

import de.klassenserver7b.k7bot.database.HibernateManager;
import de.klassenserver7b.k7bot.database.entities.CommandLogEntity;

/**
 * Data Access Object for handling CommandLogEntity operations. Provides methods
 * for inserting logs of executed commands.
 */
public class CommandLogDAO {

	/**
	 * Inserts a log entry for a command execution.
	 *
	 * @param command   the name or alias of the executed command
	 * @param guildId   the ID of the guild where the command was executed
	 * @param userId    the ID of the user who executed the command
	 * @param timestamp the timestamp of when the command was executed
	 * @return a CompletableFuture that completes when the operation is finished
	 */
	public CompletableFuture<Void> insertLog(String command, Long guildId, Long userId, Long timestamp) {
		return CompletableFuture.runAsync(() -> HibernateManager.getSessionFactory().inTransaction(session -> {
			CommandLogEntity entity = new CommandLogEntity(command, guildId, userId, timestamp);
			session.persist(entity);
		}), HibernateManager.DB_EXECUTOR);
	}
}
