/* (C)2026 */
package de.klassenserver7b.k7bot.util;

import de.klassenserver7b.k7bot.K7Bot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;

public abstract class ChannelUtil {
	public static GuildMessageChannel getSystemChannel(GenericGuildEvent event) {
		return K7Bot.getInstance().getSysChannelMgr().getSysChannel(event.getGuild());
	}

	public static GuildMessageChannel getSystemChannel(Guild guild) {
		return K7Bot.getInstance().getSysChannelMgr().getSysChannel(guild);
	}
}
