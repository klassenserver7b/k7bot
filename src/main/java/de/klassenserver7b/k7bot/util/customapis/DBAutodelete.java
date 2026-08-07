/* (C)2026 */
package de.klassenserver7b.k7bot.util.customapis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.klassenserver7b.k7bot.database.dao.MessageLogsDAO;
import de.klassenserver7b.k7bot.util.InternalStatusCodes;
import de.klassenserver7b.k7bot.util.customapis.types.LoopedEvent;

/**
 *
 */
public class DBAutodelete implements LoopedEvent {

	private final Logger log;

	/**
	 *
	 */
	public DBAutodelete() {
		log = LoggerFactory.getLogger(getClass());
	}

	@Override
	public InternalStatusCodes checkForUpdates() {

		Long minDate = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 2; // 7 days

		new MessageLogsDAO().deleteOlderThan(minDate);
		log.info("Removed lines from messagelogs older than {}", minDate);

		return InternalStatusCodes.SUCCESS;
	}

	@Override
	public boolean isAvailable() {
		return true;
	}

	@Override
	public void shutdown() {
		// NOTHING to do here

	}

	@Override
	public boolean restart() {
		// NOTHING to do here
		return true;
	}

	@Override
	public String getIdentifier() {
		return "db_autodelete";
	}
}
