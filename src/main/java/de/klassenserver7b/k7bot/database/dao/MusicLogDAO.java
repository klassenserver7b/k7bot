/* (C)2026 */
package de.klassenserver7b.k7bot.database.dao;

import java.util.concurrent.CompletableFuture;

import de.klassenserver7b.k7bot.database.HibernateManager;
import de.klassenserver7b.k7bot.database.entities.MusicLogsEntity;

/**
 * Data Access Object for handling MusicLogsEntity operations. Provides methods
 * for logging played music tracks.
 */
@SuppressWarnings({ "unused", "UnusedReturnValue" })
public class MusicLogDAO {
	/**
	 * Inserts a log entry for a played song.
	 *
	 * @param songname   the name of the song
	 * @param songauthor the author or artist of the song
	 * @param guildId    the ID of the guild where the song was played
	 * @param timestamp  the timestamp of when the song was played
	 * @return a CompletableFuture that completes when the operation is finished
	 */
	public CompletableFuture<Void> insertLog(String songname, String songauthor, Long guildId, Long timestamp) {
		return CompletableFuture
				.runAsync(
						() -> HibernateManager.getSessionFactory()
								.inTransaction(session -> session
										.persist(new MusicLogsEntity(songname, songauthor, guildId, timestamp))),
						HibernateManager.DB_EXECUTOR);
	}
}
