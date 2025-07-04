package de.klassenserver7b.k7bot.moderation.commands.common;

import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.moderation.commands.generic.GenericUserLogsCommand;
import de.klassenserver7b.k7bot.sql.LiteSQL;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import de.klassenserver7b.k7bot.util.HelpCategories;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ModLogsCommand extends GenericUserLogsCommand implements ServerCommand {

    private boolean isEnabled;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public String getHelp() {
        return "Zeigt die Logs zu einem Moderator.\n - kann nur von Mitgliedern mit der Berechtigung 'Mitglieder kicken' ausgeführt werden!\n - z.B. [prefix]modlogs @moderator";
    }

    @Override
    public String[] getCommandStrings() {
        return new String[]{"modlogs"};
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

            try (ResultSet set = LiteSQL.onQuery(
                    "SELECT memberName, action, reason, date FROM modlogs  WHERE guildId = ? AND requesterId = ?",
                    guildid, reqid)) {

                ArrayList<String> membName = new ArrayList<>();
                ArrayList<String> action = new ArrayList<>();
                ArrayList<String> reason = new ArrayList<>();
                ArrayList<String> date = new ArrayList<>();

                for (int i = 1; i < 51 && set.next(); i++) {
                    membName.add(set.getString("memberName"));
                    action.add(set.getString("action"));
                    reason.add(set.getString("reason"));
                    date.add(set.getString("date"));
                }

                if (!membName.isEmpty()) {
                    for (int j = 0; j < membName.size(); j++) {

                        StringBuilder strbuilder = new StringBuilder();
                        strbuilder.append("moderator: ").append(mentionedMembers.getFirst().getEffectiveName());
                        strbuilder.append("\n");
                        strbuilder.append("action: ").append(action.get(j));
                        strbuilder.append("\n");
                        strbuilder.append("user: @").append(mentionedMembers.getFirst().getEffectiveName());
                        strbuilder.append("\n");
                        strbuilder.append("reason: ").append(reason.get(j));
                        strbuilder.append("\n");
                        strbuilder.append("date: ").append(date.get(j));
                        strbuilder.append("\n");

                        EmbedBuilder embed = EmbedUtils.getBuilderOf(Color.orange, strbuilder,
                                channel.getGuild().getIdLong());

                        embed.setTitle("Modlogs for @" + memb.getEffectiveName());
                        embed.setFooter("requested by @" + m.getEffectiveName());
                        embed.setThumbnail(memb.getUser().getEffectiveAvatarUrl());

                        channel.sendMessageEmbeds(embed.build()).queue();
                    }
                } else {

                    channel.sendMessage("This moderator hasn't a log!").complete().delete().queueAfter(20L,
                            TimeUnit.SECONDS);
                }
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
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
