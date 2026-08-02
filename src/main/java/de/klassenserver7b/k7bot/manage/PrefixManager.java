/* (C)2026 */
package de.klassenserver7b.k7bot.manage;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.database.dao.BotUtilDAO;
import de.klassenserver7b.k7bot.database.entities.BotUtilEntity;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * @author Klassenserver7b
 */
public class PrefixManager {

	private final HashMap<Long, String> prefixl;
	private final Logger log;

	public PrefixManager() {
		this.prefixl = new HashMap<>();
		log = LoggerFactory.getLogger(this.getClass());
		reload();
	}

	protected void reload() {
		try {
			java.util.List<BotUtilEntity> list = new BotUtilDAO().getAll().get();
			for (BotUtilEntity entity : list) {
				long guildid = entity.getGuildId();
				String prefix = entity.getPrefix();
				if (guildid == 0)
					continue;
				prefixl.put(guildid, prefix);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		K7Bot.getInstance().getShardManager().getShards().forEach(jda -> {
			for (Guild g : jda.getGuilds()) {

				if (!prefixl.containsKey(g.getIdLong())) {
					try {
						setInternalPrefix(g.getIdLong(), "-");
					} catch (IllegalArgumentException e) {
						log.warn(e.getMessage(), e);
					}
				}
			}
		});
	}

	/**
	 * @param guildid   the guildid to set the prefix for
	 * @param newprefix the new prefix
	 * @throws IllegalArgumentException if newprefix is null or empty
	 */
	protected void setInternalPrefix(long guildid, @SuppressWarnings("SameParameterValue") String newprefix)
			throws IllegalArgumentException {

		if (newprefix == null || newprefix.isBlank()) {
			throw new IllegalArgumentException("can't use a empty prefix - guildid: " + guildid,
					new Throwable().fillInStackTrace());
		}

		applyPrefix(guildid, newprefix);
	}

	/**
	 * @param guildid   the guildid to set the prefix for
	 * @param newprefix the new prefix
	 * @throws IllegalArgumentException if newprefix is null or empty
	 */
	public void setPrefix(long guildid, String newprefix) throws IllegalArgumentException {

		reload();

		if (newprefix == null || newprefix.isBlank()) {
			throw new IllegalArgumentException("can't use a empty prefix", new Throwable().fillInStackTrace());
		}

		applyPrefix(guildid, newprefix);
	}

	protected void applyPrefix(long guildid, String prefix) {
		new BotUtilDAO().updatePrefix(guildid, prefix);
		prefixl.put(guildid, prefix);
	}

	@SuppressWarnings("unused")
	public String getPrefix(Guild guild) {
		return this.prefixl.get(guild.getIdLong());
	}

	public String getPrefix(Long guildid) {
		return this.prefixl.putIfAbsent(guildid, "-");
	}
}
