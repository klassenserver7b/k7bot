package de.klassenserver7b.k7bot.moderation.commands.common;

import de.klassenserver7b.k7bot.HelpCategories;
import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.sql.LiteSQL;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import de.klassenserver7b.k7bot.util.GenericMessageSendHandler;
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

public class TimeoutCommand implements ServerCommand {

    private boolean isEnabled;

    @Override
    public String getHelp() {

        return "timeoutet den angegeben Nutzer für den Ausgewählten Grund.\n - kann nur von Mitgliedern mit der Berechtigung 'Nachrichten verwalten' ausgeführt werden!\n - z.B. [prefix]timeout [zeit (in minuten)] [reason] @member";
    }

    @Override
    public String[] getCommandStrings() {
        return new String[]{"timeout"};
    }

    @Override
    public HelpCategories getCategory() {
        return HelpCategories.MODERATION;
    }

    @Override
    public void performCommand(Member m, GuildMessageChannel channel, Message message) {

        List<Member> ment = message.getMentions().getMembers();
        String[] args = message.getContentRaw().replaceAll("<@(\\d+)?>", "").split(" ");
        StringBuilder grund = new StringBuilder();

        for (int i = 2; i < args.length; i++) {
            grund.append(args[i]);
        }

        grund = new StringBuilder(grund.toString().trim());

        try {

            channel.sendTyping().queue();

            if (m.hasPermission(Permission.MESSAGE_MANAGE)) {
                if (!ment.isEmpty()) {
                    for (Member u : ment) {
                        onTimeout(m, u, channel, args[1], grund.toString());
                    }
                }
            } else {
                PermissionError.onPermissionError(m, channel);
            }
        } catch (StringIndexOutOfBoundsException e) {
            SyntaxError.oncmdSyntaxError(new GenericMessageSendHandler(channel),
                    "timeout [time (in minutes)] [reason] @user", m);
        }
    }

    public void onTimeout(Member requester, Member u, GuildMessageChannel channel, String time, String grund) {

        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("**User: **").append(u.getAsMention()).append("\n");
        strBuilder.append("**Case: **").append(grund).append("\n");
        strBuilder.append("**Requester: **").append(requester.getAsMention()).append("\n");

        Guild guild = channel.getGuild();
        GuildMessageChannel system = K7Bot.getInstance().getSysChannelMgr().getSysChannel(guild);

        EmbedBuilder builder = EmbedUtils.getErrorEmbed(strBuilder, channel.getGuild().getIdLong());

        builder.setFooter("Requested by @" + requester.getEffectiveName());
        builder.setThumbnail(u.getUser().getEffectiveAvatarUrl());

        try {
            u.timeoutFor(Long.parseLong(time), TimeUnit.MINUTES).queue();
            builder.setTitle(
                    "@" + u.getEffectiveName() + " has been timeouted for " + Long.parseLong(time) + " minutes");

            if (system != null) {

                system.sendMessageEmbeds(builder.build()).queue();

            }

            if (system == null || system.getIdLong() != channel.getIdLong()) {

                channel.sendMessageEmbeds(builder.build()).complete().delete().queueAfter(20L, TimeUnit.SECONDS);

            }

            builder.setTitle("You have been timeouted for " + Long.parseLong(time) + " minutes");

            u.getUser().openPrivateChannel().queue((ch) -> ch.sendMessageEmbeds(builder.build()).queue());

            String action = "timeout";
            LiteSQL.onUpdate(
                    "INSERT INTO modlogs(guildId, memberId, requesterId, memberName, requesterName, action, reason, date) VALUES(?, ?, ?, ?, ?, ?, ?, ?);",
                    channel.getGuild().getIdLong(), u.getIdLong(), requester.getIdLong(), u.getEffectiveName(),
                    requester.getEffectiveName(), action, grund, OffsetDateTime.now());
        } catch (HierarchyException e) {

            PermissionError.onPermissionError(requester, channel);

        } catch (NumberFormatException e) {

            SyntaxError.oncmdSyntaxError(new GenericMessageSendHandler(channel),
                    "timeout [time (in minutes)] [reason] @user", requester);

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