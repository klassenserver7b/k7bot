/* (C)2026 */
package de.klassenserver7b.k7bot.database.dao;

import java.util.concurrent.CompletableFuture;

import de.klassenserver7b.k7bot.database.HibernateManager;
import de.klassenserver7b.k7bot.database.entities.LoggingConfigEntity;
import net.dv8tion.jda.api.utils.data.DataArray;

/**
 * Data Access Object for handling LoggingConfigEntity operations. Provides
 * methods for managing logging configurations for guilds.
 */
@SuppressWarnings({ "unused", "UnusedReturnValue" })
public class LoggingConfigDAO {

	/**
	 * Inserts default logging configuration for a guild if it doesn't already
	 * exist.
	 *
	 * @param guildId the ID of the guild
	 * @return a CompletableFuture that completes when the operation is finished
	 */
	public CompletableFuture<Void> insertGuild(long guildId) {
		return CompletableFuture.runAsync(() -> HibernateManager.getSessionFactory().inTransaction(session -> {
			LoggingConfigEntity entity = session.find(LoggingConfigEntity.class, guildId);
			if (entity == null) {
				entity = new LoggingConfigEntity();
				entity.setGuildId(guildId);
				entity.setOptionJson("[]");
				session.persist(entity);
			}
		}), HibernateManager.DB_EXECUTOR);
	}

	/**
	 * Checks whether a specific logging option is disabled for a guild.
	 *
	 * @param optionId the ID of the logging option
	 * @param guildId  the ID of the guild
	 * @return a CompletableFuture that resolves to true if the option is disabled,
	 *         false otherwise
	 */
	public CompletableFuture<Boolean> isOptionDisabled(int optionId, long guildId) {
		insertGuild(guildId);
		return CompletableFuture.supplyAsync(() -> HibernateManager.getSessionFactory().fromTransaction(session -> {
			LoggingConfigEntity entity = session.find(LoggingConfigEntity.class, guildId);
			if (entity == null) {
				return true;
			}
			DataArray array = DataArray.fromJson(entity.getOptionJson());
			for (int i = 0; i < array.length(); i++) {
				if (array.getInt(i) == optionId) {
					return false; // Found -> it's enabled -> so not disabled
				}
			}
			return true; // Not found -> disabled
		}), HibernateManager.DB_EXECUTOR);
	}

	/**
	 * Enables a specific logging option for a guild.
	 *
	 * @param optionId the ID of the logging option to enable
	 * @param guildId  the ID of the guild
	 * @return a CompletableFuture that completes when the operation is finished
	 */
	public CompletableFuture<Void> enableOption(int optionId, long guildId) {
		insertGuild(guildId);
		return CompletableFuture.runAsync(() -> HibernateManager.getSessionFactory().inTransaction(session -> {
			LoggingConfigEntity entity = session.find(LoggingConfigEntity.class, guildId);
			if (entity == null) {
				entity = new LoggingConfigEntity();
				entity.setGuildId(guildId);
				entity.setOptionJson("[]");
				session.persist(entity);
			}
			DataArray array = DataArray.fromJson(entity.getOptionJson());
			boolean found = false;
			for (int i = 0; i < array.length(); i++) {
				if (array.getInt(i) == optionId) {
					found = true;
					break;
				}
			}
			if (!found) {
				array.add(optionId);
				entity.setOptionJson(array.toString());
			}
		}), HibernateManager.DB_EXECUTOR);
	}

	/**
	 * Disables a specific logging option for a guild.
	 *
	 * @param optionId the ID of the logging option to disable
	 * @param guildId  the ID of the guild
	 * @return a CompletableFuture that completes when the operation is finished
	 */
	public CompletableFuture<Void> disableOption(int optionId, long guildId) {
		insertGuild(guildId);
		return CompletableFuture.runAsync(() -> HibernateManager.getSessionFactory().inTransaction(session -> {
			LoggingConfigEntity entity = session.find(LoggingConfigEntity.class, guildId);
			if (entity == null) {
				entity = new LoggingConfigEntity();
				entity.setGuildId(guildId);
				entity.setOptionJson("[]");
				session.persist(entity);
			}
			DataArray array = DataArray.fromJson(entity.getOptionJson());
			DataArray newArray = DataArray.empty();
			for (int i = 0; i < array.length(); i++) {
				if (array.getInt(i) != optionId) {
					newArray.add(array.getInt(i));
				}
			}
			entity.setOptionJson(newArray.toString());
		}), HibernateManager.DB_EXECUTOR);
	}
}
