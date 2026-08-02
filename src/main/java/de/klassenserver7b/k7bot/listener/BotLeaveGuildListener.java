/* (C)2026 */
package de.klassenserver7b.k7bot.listener;

import de.klassenserver7b.k7bot.database.dao.BotUtilDAO;
import de.klassenserver7b.k7bot.database.dao.ReactRolesDAO;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.UnavailableGuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class BotLeaveGuildListener extends ListenerAdapter {

	@Override
	public void onGuildLeave(@NotNull GuildLeaveEvent event) {
		long guildid = event.getGuild().getIdLong();

		new BotUtilDAO().deleteByGuildId(guildid);
		new ReactRolesDAO().deleteByGuildId(guildid);
	}

	@Override
	public void onUnavailableGuildLeave(@NotNull UnavailableGuildLeaveEvent event) {
		long guildid = event.getGuildIdLong();

		new BotUtilDAO().deleteByGuildId(guildid);
		new ReactRolesDAO().deleteByGuildId(guildid);
	}
}
