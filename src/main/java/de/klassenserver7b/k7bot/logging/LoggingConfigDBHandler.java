/* (C)2026 */
package de.klassenserver7b.k7bot.logging;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.klassenserver7b.k7bot.database.dao.LoggingConfigDAO;
import net.dv8tion.jda.api.entities.Guild;

/**
 *
 */
public abstract class LoggingConfigDBHandler {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(LoggingConfigDBHandler.class);
	private static final LoggingConfigDAO dao = new LoggingConfigDAO();

	@SuppressWarnings("unused")
	public static CompletableFuture<Void> insertGuild(long guildId) {
		return dao.insertGuild(guildId);
	}

	@SuppressWarnings("UnusedReturnValue")
	public static CompletableFuture<Void> enableOption(LoggingOptions option, long guildId) {
		return dao.enableOption(option.getId(), guildId);
	}

	@SuppressWarnings("UnusedReturnValue")
	public static CompletableFuture<Void> disableOption(LoggingOptions option, long guildId) {
		return dao.disableOption(option.getId(), guildId);
	}

	public static boolean isOptionDisabled(LoggingOptions option, Guild guild) {
		return isOptionDisabled(option, guild.getIdLong());
	}

	public static boolean isOptionDisabled(LoggingOptions option, long guildId) {
		return dao.isOptionDisabled(option.getId(), guildId).join();
	}

	/**
	 * Toggles the key and returns the new state of the {@link LoggingOptions};
	 *
	 * @param option  the option to toggle
	 * @param guildId the guild to toggle the option for
	 * @return the new state of the option
	 */
	@SuppressWarnings("UnusedReturnValue")
	public static boolean toggleOption(LoggingOptions option, long guildId) {
		if (LoggingOptions.UNKNOWN == option) {
			return false;
		}

		if (!isOptionDisabled(option, guildId)) {
			disableOption(option, guildId);
			return false;
		} else {
			enableOption(option, guildId);
			return true;
		}
	}
}
