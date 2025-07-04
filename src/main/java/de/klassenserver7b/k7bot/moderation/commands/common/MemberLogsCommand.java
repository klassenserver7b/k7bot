package de.klassenserver7b.k7bot.moderation.commands.common;

import de.klassenserver7b.k7bot.util.HelpCategories;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.moderation.commands.generic.GenericUserLogsCommand;
import de.klassenserver7b.k7bot.sql.LiteSQL;
import de.klassenserver7b.k7bot.util.EmbedUtils;
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

public class MemberLogsCommand extends GenericUserLogsCommand implements ServerCommand {

    private boolean isEnabled;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public String getHelp() {
        return "Zeigt die Logs zu einem Mitglied.\n - kann nur von Mitgliedern mit der Berechtigung 'Mitglieder kicken' ausgeführt werden!\n - z.B. [prefix]modlogs @member";
    }

    @Override
    public String[] getCommandStrings() {
        return new String[]{"memberlogs"};
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
            long membid = memb.getIdLong();

            try (ResultSet set = LiteSQL.onQuery(
                    "SELECT requesterName, action, reason, date FROM modlogs  WHERE guildId = AND memberId = ?",
                    guildid, membid)) {

                ArrayList<String> requName = new ArrayList<>();
                ArrayList<String> action = new ArrayList<>();
                ArrayList<String> reason = new ArrayList<>();
                ArrayList<String> date = new ArrayList<>();

                for (int i = 1; i < 51 && set.next(); i++) {
                    requName.add(set.getString("requesterName"));
                    action.add(set.getString("action"));
                    reason.add(set.getString("reason"));
                    date.add(set.getString("date"));
                }

                if (!requName.isEmpty()) {
                    for (int j = 0; j < requName.size(); j++) {

                        StringBuilder strbuilder = new StringBuilder();
                        strbuilder.append("user: @").append(mentionedMembers.getFirst().getEffectiveName());
                        strbuilder.append("\n");
                        strbuilder.append("action: ").append(action.get(j));
                        strbuilder.append("\n");
                        strbuilder.append("moderator: ").append(requName.get(j));
                        strbuilder.append("\n");
                        strbuilder.append("reason: ").append(reason.get(j));
                        strbuilder.append("\n");
                        strbuilder.append("date: ").append(date.get(j));
                        strbuilder.append("\n");

                        EmbedBuilder embed = EmbedUtils.getBuilderOf(Color.orange, strbuilder,
                                channel.getGuild().getIdLong());

                        embed.setTitle("Memberlogs for @" + memb.getEffectiveName());
                        embed.setFooter("requested by @" + m.getEffectiveName());
                        embed.setThumbnail(memb.getUser().getEffectiveAvatarUrl());

                        channel.sendMessageEmbeds(embed.build()).queue();
                    }
                } else {

                    channel.sendMessage("This user hasn't a log!").complete().delete().queueAfter(20L,
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
