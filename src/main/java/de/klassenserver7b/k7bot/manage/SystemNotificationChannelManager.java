/* (C)2026 */
package de.klassenserver7b.k7bot.manage;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.database.dao.BotUtilDAO;
import de.klassenserver7b.k7bot.database.entities.BotUtilEntity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class SystemNotificationChannelManager {

	@SuppressWarnings("unused")
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * Puts the given {@link GuildMessageChannel SystemChannel} into the Hashmap
	 * keyed by his {@link Guild}.
	 *
	 * @param channel <br>
	 *                The {@link GuildMessageChannel SystemChannel} wich u want to
	 *                use in this {@link Guild}.
	 */
	public void insertChannel(GuildMessageChannel channel) {
		Guild guild = channel.getGuild();
		new BotUtilDAO().updateSysChannelId(guild.getIdLong(), channel.getIdLong());
	}

	/**
	 * @param guild <br>
	 *              The {@link Guild} for which you want the SystemChannel.
	 * @return The {@link GuildMessageChannel SystemChannel} for the Guild or
	 *         {@code null} if no channel is listed.
	 */
	public @Nullable GuildMessageChannel getSysChannel(@NotNull Guild guild) {
		BotUtilEntity entity = new BotUtilDAO().get(guild.getIdLong()).join();
		if (entity != null && entity.getSyschannelId() != null) {
			GuildChannel channel = guild.getGuildChannelById(entity.getSyschannelId());
			if (channel instanceof GuildMessageChannel sysChannel) {
				return sysChannel;
			}
		}
		return guild.getSystemChannel();
	}

	/**
	 * @param guildId <br>
	 *                The Id of the {@link Guild} for which you want the
	 *                SystemChannel.
	 * @return The {@link GuildMessageChannel SystemChannel} for the Guild or
	 *         {@code null} if no channel is listed.
	 */
	@SuppressWarnings("unused")
	public @Nullable GuildMessageChannel getSysChannel(@NotNull Long guildId) throws NullPointerException {
		Guild guild = K7Bot.getInstance().getShardManager().getGuildById(guildId);
		if (guild != null) {
			return getSysChannel(guild);
		}
		return null;
	}
}
