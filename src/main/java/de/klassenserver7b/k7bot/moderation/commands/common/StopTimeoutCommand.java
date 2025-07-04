package de.klassenserver7b.k7bot.moderation.commands.common;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.sql.LiteSQL;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import de.klassenserver7b.k7bot.util.GenericMessageSendHandler;
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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class StopTimeoutCommand implements ServerCommand {

    private boolean isEnabled;

    @Override
    public String getHelp() {
        return "Enttimeoutet den angegebenen Nutzer.\n - kann nur von Mitgliedern mit der Berechtigung 'Mitglieder kicken' ausgeführt werden!\n - z.B. [prefix]stoptimeout @member";
    }

    @Override
    public String[] getCommandStrings() {
        return new String[]{"stoptimeout"};
    }

    @Override
    public HelpCategories getCategory() {
        return HelpCategories.MODERATION;
    }

    @Override
    public void performCommand(Member m, GuildMessageChannel channel, Message message) {
        List<Member> ment = message.getMentions().getMembers();
        try {

            channel.sendTyping().queue();

            if (m.hasPermission(Permission.MESSAGE_MANAGE)) {
                if (!ment.isEmpty()) {
                    for (Member u : ment) {
                        stopTimeout(m, u, channel);
                    }
                }
            } else {
                PermissionError.onPermissionError(m, channel);
            }
        } catch (StringIndexOutOfBoundsException e) {
            SyntaxError.oncmdSyntaxError(new GenericMessageSendHandler(channel), "stoptimeout [@user]", m);
        }
    }

    public void stopTimeout(Member requester, Member u, GuildMessageChannel channel) {

        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("**User: **").append(u.getAsMention()).append("\n");
        strBuilder.append("**Requester: **").append(requester.getEffectiveName()).append("\n");

        EmbedBuilder builder = EmbedUtils.getSuccessEmbed(strBuilder, channel.getGuild().getIdLong());

        builder.setTitle("@" + u.getEffectiveName() + " has been untimeouted");
        builder.setFooter("Requested by @" + requester.getEffectiveName());
        builder.setThumbnail(u.getUser().getEffectiveAvatarUrl());

        Guild guild = channel.getGuild();
        GuildMessageChannel system = K7Bot.getInstance().getSysChannelMgr().getSysChannel(guild);

        try {
            u.removeTimeout().queue();

            if (system != null) {

                system.sendMessageEmbeds(builder.build()).queue();

            }

            if (system != null && system.getIdLong() != channel.getIdLong()) {

                channel.sendMessageEmbeds(builder.build()).complete().delete().queueAfter(20L, TimeUnit.SECONDS);

            }

            String action = "stoptimeout";
            LiteSQL.onUpdate(
                    "INSERT INTO modlogs(guildId, memberId, requesterId, memberName, requesterName, action, reason, date) VALUES(?, ?, ?, ?, ?, ?, ?, ?);",
                    channel.getGuild().getIdLong(), u.getIdLong(), requester.getIdLong(), u.getEffectiveName(),
                    requester.getEffectiveName(), action, "null", OffsetDateTime.now());
        } catch (HierarchyException e) {
            PermissionError.onPermissionError(requester, channel);
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