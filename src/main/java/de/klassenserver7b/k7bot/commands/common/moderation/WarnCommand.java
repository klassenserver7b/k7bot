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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.exceptions.HierarchyException;

public class WarnCommand implements ServerCommand {

	private boolean isEnabled;

	@Override
	public String getHelp() {

		return """
				Verwarnt den angegebenen Nutzer und übermitelt den angegebenen Grund.
				 - kann nur von Personen mit der Berechtigung 'Mitglieder kicken' ausgeführt\
				 werden!
				 - z.B. [prefix]warn @K7Bot [reason]""";
	}

	@Override
	public String[] getCommandStrings() {
		return new String[] { "warn" };
	}

	@Override
	public HelpCategories getCategory() {
		return HelpCategories.MODERATION;
	}

	@Override
	public void performCommand(Member m, GuildMessageChannel channel, Message message) {

		List<Member> ment = message.getMentions().getMembers();
		try {
			if (!ment.isEmpty()) {
				String[] args = message.getContentRaw().replaceAll("<@(\\d+)?>", "").split(" ");
				StringBuilder grund = new StringBuilder();

				for (int i = 1; i < args.length; i++) {
					grund.append(args[i]);
				}

				channel.sendTyping().queue();

				if (m.hasPermission(Permission.KICK_MEMBERS)) {
					if (!ment.isEmpty()) {
						for (Member u : ment) {
							onWarn(m, u, channel, grund.toString());
						}
					}
				} else {
					PermissionError.onPermissionError(channel, m);
				}
			} else {
				SyntaxError.onCmdSyntaxError(channel, m, "warn [@user] [reason]");
			}
		} catch (StringIndexOutOfBoundsException e) {
			SyntaxError.onCmdSyntaxError(channel, m, "warn [@user] [reason]");
		}
	}

	public void onWarn(Member requester, Member u, GuildMessageChannel channel, String grund) {

		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("**User: **").append(u.getUser().getAsMention()).append("\n");
		strBuilder.append("**Case: **").append(grund).append("\n");
		strBuilder.append("**Requester: **").append(requester.getEffectiveName()).append("\n");
		strBuilder.append("**Server: **").append(channel.getGuild().getName()).append("\n");

		EmbedBuilder builder = EmbedUtils.getErrorEmbed(strBuilder, channel.getGuild().getIdLong());

		builder.setTitle("Warning logged for @" + u.getEffectiveName());
		builder.setFooter("Requested by @" + requester.getEffectiveName());
		builder.setThumbnail(u.getUser().getEffectiveAvatarUrl());

		Guild guild = channel.getGuild();
		GuildMessageChannel system = K7Bot.getInstance().getSysChannelMgr().getSysChannel(guild);

		try {

			if (system != null) {

				system.sendMessageEmbeds(builder.build()).queue();
			}

			if (system == null || system.getIdLong() != channel.getIdLong()) {

				channel.sendMessageEmbeds(builder.build()).complete().delete().queueAfter(20L, TimeUnit.SECONDS);
			}

			u.getUser().openPrivateChannel().queue((ch) -> ch.sendMessageEmbeds(builder.build()).queue());

			String action = "warn";

			ModLogEntity log = new ModLogEntity(channel.getGuild().getIdLong(), u.getIdLong(), requester.getIdLong(),
					u.getEffectiveName(), requester.getEffectiveName(), action, grund, OffsetDateTime.now().toString());
			new ModLogDAO().saveLog(log);
		} catch (HierarchyException e) {
			PermissionError.onPermissionError(channel, requester);
		}
	}

	@Override
	public boolean isEnabled() {
		return isEnabled;
	}

	@Override
	public void disableCommand() {
		isEnabled = false;
	}

	@Override
	public void enableCommand() {
		isEnabled = true;
	}
}
