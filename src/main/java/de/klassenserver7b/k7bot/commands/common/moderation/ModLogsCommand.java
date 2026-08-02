/* (C)2026 */
package de.klassenserver7b.k7bot.commands.common.moderation;

import de.klassenserver7b.k7bot.commands.generic.moderation.GenericMemberLogsCommand;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.database.dao.ModLogDAO;
import de.klassenserver7b.k7bot.database.entities.ModLogEntity;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import de.klassenserver7b.k7bot.util.HelpCategories;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ModLogsCommand extends GenericMemberLogsCommand implements ServerCommand {

	private boolean isEnabled;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public String getHelp() {
		return """
				Zeigt die Logs zu einem Moderator.
				 - kann nur von Mitgliedern mit der Berechtigung 'Mitglieder kicken'\
				 ausgeführt werden!
				 - z.B. [prefix]modlogs @moderator""";
	}

	@Override
	public String[] getCommandStrings() {
		return new String[] { "modlogs" };
	}

	@Override
	public HelpCategories getCategory() {
		return HelpCategories.MODERATION;
	}

	@Override
	public void performCommand(Member m, GuildMessageChannel channel, Message message) {

		if (MembFailsPermissions(m, channel)) {
			return;
		}

		List<Member> mentionedMembers;
		try {
			mentionedMembers = getMembersFromMessage(channel, message, m);
		} catch (IllegalArgumentException e) {
			return;
		}

		long guildid = channel.getGuild().getIdLong();

		for (Member memb : mentionedMembers) {

			long reqid = memb.getIdLong();

			new ModLogDAO().getLogsByRequester(guildid, reqid).thenAccept(logs -> {
				if (logs != null && !logs.isEmpty()) {
					for (int j = 0; j < Math.min(logs.size(), 50); j++) {
						ModLogEntity logEntry = logs.get(j);
						StringBuilder strbuilder = new StringBuilder();
						strbuilder.append("moderator: ").append(memb.getEffectiveName());
						strbuilder.append("\n");
						strbuilder.append("action: ").append(logEntry.getAction());
						strbuilder.append("\n");
						strbuilder.append("user: @").append(
								logEntry.getMemberName() != null ? logEntry.getMemberName() : logEntry.getMemberId());
						strbuilder.append("\n");
						strbuilder.append("reason: ").append(logEntry.getReason());
						strbuilder.append("\n");
						strbuilder.append("date: ").append(logEntry.getDate());
						strbuilder.append("\n");

						EmbedBuilder embed = EmbedUtils.getBuilderOf(Color.orange, strbuilder,
								channel.getGuild().getIdLong());

						embed.setTitle("Modlogs for @" + memb.getEffectiveName());
						embed.setFooter("requested by @" + m.getEffectiveName());
						embed.setThumbnail(memb.getUser().getEffectiveAvatarUrl());

						channel.sendMessageEmbeds(embed.build()).queue();
					}
				} else {
					channel.sendMessage("This moderator hasn't a log!")
							.queue(msg -> msg.delete().queueAfter(20L, TimeUnit.SECONDS));
				}
			}).exceptionally(e -> {
				log.error(e.getMessage(), e);
				return null;
			});
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
