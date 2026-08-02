/* (C)2026 */
package de.klassenserver7b.k7bot.database.dao;

import de.klassenserver7b.k7bot.database.HibernateManager;
import de.klassenserver7b.k7bot.database.entities.SlashCommandLogEntity;

import java.util.concurrent.CompletableFuture;

/**
 * Data Access Object for handling SlashCommandLogEntity operations. Provides
 * methods for logging executed slash commands.
 */
public class SlashCommandLogDAO {

	/**
	 * Inserts a log entry for a slash command execution.
	 *
	 * @param command       the name of the slash command
	 * @param guildId       the ID of the guild where the command was executed
	 * @param userId        the ID of the user who executed the command
	 * @param timestamp     the timestamp of when the command was executed
	 * @param commandstring the full string representation of the executed command
	 *                      with options
	 * @return a CompletableFuture that completes when the operation is finished
	 */
	public CompletableFuture<Void> insertLog(String command, Long guildId, Long userId, Long timestamp,
			String commandstring) {
		return CompletableFuture.runAsync(() -> HibernateManager.getSessionFactory().inTransaction(session -> {
			SlashCommandLogEntity entity = new SlashCommandLogEntity(command, guildId, userId, timestamp,
					commandstring);
			session.persist(entity);
		}), HibernateManager.DB_EXECUTOR);
	}
}
