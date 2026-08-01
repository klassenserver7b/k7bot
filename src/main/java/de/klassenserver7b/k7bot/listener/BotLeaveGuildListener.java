/* (C)2026 */
package de.klassenserver7b.k7bot.listener;

import de.klassenserver7b.k7bot.K7Bot;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.UnavailableGuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class BotLeaveGuildListener extends ListenerAdapter {

	@Override
	public void onGuildLeave(@NotNull GuildLeaveEvent event) {
		long guildid = event.getGuild().getIdLong();

		K7Bot.getInstance().getDb().update("DELETE FROM musicutil WHERE guildId = ?;", guildid);
		K7Bot.getInstance().getDb().update("DELETE FROM botutil WHERE guildId = ?;", guildid);
		K7Bot.getInstance().getDb().update("DELETE FROM reactroles WHERE guildId = ?;", guildid);
	}

	@Override
	public void onUnavailableGuildLeave(@NotNull UnavailableGuildLeaveEvent event) {
		long guildid = event.getGuildIdLong();

		K7Bot.getInstance().getDb().update("DELETE FROM musicutil WHERE guildId = ?;", guildid);
		K7Bot.getInstance().getDb().update("DELETE FROM botutil WHERE guildId = ?;", guildid);
		K7Bot.getInstance().getDb().update("DELETE FROM reactroles WHERE guildId = ?;", guildid);
	}
}
