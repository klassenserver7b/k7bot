/* (C)2026 */
package de.klassenserver7b.k7bot.commands.common.moderation;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.database.dao.ModLogDAO;
import de.klassenserver7b.k7bot.database.entities.ModLogEntity;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import de.klassenserver7b.k7bot.util.HelpCategories;
import de.klassenserver7b.k7bot.util.errorhandler.PermissionError;
import de.klassenserver7b.k7bot.util.errorhandler.SyntaxError;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.exceptions.HierarchyException;

public class BanCommand implements ServerCommand {

	@Override
	public String getHelp() {
		return """
				Bannt den ausgewählten Nutzer vom Server und übermitelt den angegebenen Grund.
				 - kann nur von Personen mit der Berechtigung 'Mitglieder bannen' ausgeführt\
				 werden!
				 - z.B. [prefix]ban [@USER] [reason]""";
	}

	@Override
	public String[] getCommandStrings() {
		return new String[] { "ban" };
	}

	@Override
	public HelpCategories getCategory() {
		return HelpCategories.MODERATION;
	}

	@Override
	public void performCommand(Member m, GuildMessageChannel channel, Message message) {
		List<Member> ment = message.getMentions().getMembers();
		try {

			String[] args = message.getContentDisplay().replaceAll("<@(\\d+)?>", "").split(" ");
			StringBuilder grund = new StringBuilder();

			for (int i = 1; i < args.length; i++) {
				grund.append(args[i]).append(" ");
			}

			grund = new StringBuilder(grund.toString().trim());

			channel.sendTyping().queue();

			if (m.hasPermission(Permission.BAN_MEMBERS)) {
				if (!ment.isEmpty()) {
					for (Member u : ment) {
						onBan(m, u, channel, grund.toString());
					}
				}
			} else {
				PermissionError.onPermissionError(channel, m);
			}
		} catch (StringIndexOutOfBoundsException e) {
			SyntaxError.onCmdSyntaxError(channel, m, "ban [@user] [reason]");
		}
	}

	@SuppressWarnings("DuplicatedCode")
	public void onBan(Member requester, Member u, GuildMessageChannel channel, String grund) {

		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("**User: **").append(u.getUser().getAsMention()).append("\n");
		strBuilder.append("**Case: **").append(grund).append("\n");
		strBuilder.append("**Requester: **").append(requester.getEffectiveName()).append("\n");

		EmbedBuilder builder = EmbedUtils.getErrorEmbed(strBuilder, channel.getGuild().getIdLong());

		builder.setThumbnail(u.getUser().getEffectiveAvatarUrl());
		builder.setTitle("@" + u.getEffectiveName() + " was banned");

		GuildMessageChannel system = K7Bot.getInstance().getSysChannelMgr().getSysChannel(channel.getGuild());

		try {
			u.ban(7, TimeUnit.DAYS).reason(grund).queue();

			if (system != null) {

				system.sendMessageEmbeds(builder.build()).queue();
			}

			if (system == null || system.getIdLong() != channel.getIdLong()) {

				channel.sendMessageEmbeds(builder.build()).complete().delete().queueAfter(20L, TimeUnit.SECONDS);
			}

			String action = "ban";
			ModLogEntity log = new ModLogEntity(channel.getGuild().getIdLong(), u.getIdLong(), requester.getIdLong(),
					u.getEffectiveName(), requester.getEffectiveName(), action, grund, OffsetDateTime.now().toString());
			new ModLogDAO().saveLog(log);
		} catch (HierarchyException e) {
			PermissionError.onPermissionError(channel, requester);
		}
	}

}
