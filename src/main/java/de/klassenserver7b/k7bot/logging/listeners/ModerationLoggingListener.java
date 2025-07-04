package de.klassenserver7b.k7bot.logging.listeners;

import de.klassenserver7b.k7bot.logging.LoggingConfigDBHandler;
import de.klassenserver7b.k7bot.logging.LoggingOptions;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.automod.AutoModExecutionEvent;
import net.dv8tion.jda.api.events.automod.AutoModRuleCreateEvent;
import net.dv8tion.jda.api.events.automod.AutoModRuleDeleteEvent;
import net.dv8tion.jda.api.events.automod.AutoModRuleUpdateEvent;
import net.dv8tion.jda.api.events.guild.GuildAuditLogEntryCreateEvent;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateTimeOutEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

import static de.klassenserver7b.k7bot.util.ChannelUtil.getSystemChannel;

public class ModerationLoggingListener extends ListenerAdapter {

    public ModerationLoggingListener() {
        super();
    }

    @Override
    public void onGuildBan(GuildBanEvent event) {

        if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.BAN, event.getGuild())) {
            return;
        }

        Guild guild = event.getGuild();
        GuildMessageChannel system = getSystemChannel(event);
        User user = event.getUser();
        EmbedBuilder builder = EmbedUtils.getBuilderOf(Color.red, "**User: **\n " + user.getAsMention(),
                guild.getIdLong());
        builder.setThumbnail(user.getEffectiveAvatarUrl());
        builder.setTitle("User banned: " + user.getName());
        system.sendMessageEmbeds(builder.build()).queue();
    }

    @Override
    public void onGuildUnban(GuildUnbanEvent event) {

        if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.UNBAN, event.getGuild())) {
            return;
        }

        Guild guild = event.getGuild();

        GuildMessageChannel system = getSystemChannel(event);
        User user = event.getUser();

        EmbedBuilder builder = EmbedUtils.getBuilderOf(Color.green, "**User: **\n " + user.getAsMention(),
                guild.getIdLong());
        builder.setThumbnail(user.getEffectiveAvatarUrl());
        builder.setTitle("User unbanned: " + user.getName());
        system.sendMessageEmbeds(builder.build()).queue();
    }

    @Override
    public void onGuildMemberUpdateTimeOut(@NotNull GuildMemberUpdateTimeOutEvent event) {

        if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.TIMEOUT, event.getGuild())) {
            return;
        }

        GuildMessageChannel system = getSystemChannel(event);
        Member memb = event.getMember();

        EmbedBuilder embbuild = EmbedUtils.getDefault(event.getGuild());
        embbuild.setColor(Color.yellow);

        StringBuilder sb = new StringBuilder();

        if (event.getOldTimeOutEnd() != null && event.getNewTimeOutEnd() != null) {

            embbuild.setTitle("Timeout updated for " + memb.getEffectiveName());
            sb.append("**Old Timeout End: **<t:").append(event.getOldTimeOutEnd().toEpochSecond()).append(">");
            sb.append("\n**New Timeout End: **<t:").append(event.getNewTimeOutEnd().toEpochSecond()).append(">");

        } else if (event.getNewTimeOutEnd() != null) {

            embbuild.setTitle("Timeout added for " + memb.getEffectiveName());
            sb.append("**Timeout End: **<t:").append(event.getNewTimeOutEnd().toEpochSecond()).append(">");

        } else if (event.getOldTimeOutEnd() != null) {

            embbuild.setTitle("Timeout removed for " + memb.getEffectiveName());
            sb.append("**Old Timeout End: **<t:").append(event.getOldTimeOutEnd().toEpochSecond()).append(">");

        }

        embbuild.setDescription(sb);
        system.sendMessageEmbeds(embbuild.build()).queue();

    }

    @Override
    public void onGuildAuditLogEntryCreate(@NotNull GuildAuditLogEntryCreateEvent event) {

        if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.AUDITLOG_ENTRY_CREATE, event.getGuild())) {
            return;
        }

        GuildMessageChannel system = getSystemChannel(event);

        AuditLogEntry entry = event.getEntry();

        EmbedBuilder embbuild = EmbedUtils.getDefault(event.getGuild());
        embbuild.setTitle("Auditlog entry created");
        embbuild.setDescription("**Type: **" + entry.getType()
                + "\n**User: **" + event.getGuild().retrieveMemberById(entry.getUserIdLong()).complete().getAsMention()
                + "\n**Reason: **" + entry.getReason());
        system.sendMessageEmbeds(embbuild.build()).queue();

    }

    @Override
    public void onAutoModExecution(@NotNull AutoModExecutionEvent event) {

        if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.AUTOMOD_EXECUTED, event.getGuild())) {
            return;
        }

        GuildMessageChannel system = getSystemChannel(event.getGuild());

        EmbedBuilder embbuild = EmbedUtils.getDefault(event.getGuild());

        GuildMessageChannel channel = event.getChannel();

        embbuild.setTitle("Automod executed" + (channel != null ? " in " + channel.getAsMention() : ""));
        embbuild.setDescription("**Rule: **" + event.getGuild().retrieveAutoModRuleById(event.getRuleIdLong()).complete().getName()
                + "\n**Reason: **" + event.getMatchedContent()
                + "\n**User: **" + event.getGuild().retrieveMemberById(event.getUserIdLong()).complete().getAsMention());
        system.sendMessageEmbeds(embbuild.build()).queue();

    }

    @Override
    public void onAutoModRuleCreate(@NotNull AutoModRuleCreateEvent event) {

        if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.AUTOMOD_RULE_CREATE, event.getRule().getGuild())) {
            return;
        }

        GuildMessageChannel system = getSystemChannel(event.getRule().getGuild());
        EmbedBuilder embbuild = EmbedUtils.getDefault(event.getRule().getGuild());
        embbuild.setColor(Color.green);
        embbuild.setTitle("AutomodRule created");
        embbuild.setDescription("**Name: **" + event.getRule().getName()
                + "\n**User: **" + event.getRule().getGuild().retrieveMemberById(event.getRule().getCreatorIdLong()).complete().getAsMention()
                + "\n**Trigger: **" + event.getRule().getTriggerType());
        system.sendMessageEmbeds(embbuild.build()).queue();
    }

    @Override
    public void onAutoModRuleDelete(@NotNull AutoModRuleDeleteEvent event) {

        if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.AUTOMOD_RULE_DELETE, event.getRule().getGuild())) {
            return;
        }

        GuildMessageChannel system = getSystemChannel(event.getRule().getGuild());
        EmbedBuilder embbuild = EmbedUtils.getDefault(event.getRule().getGuild());

        embbuild.setColor(Color.red);
        embbuild.setTitle("AutomodRule deleted");
        embbuild.setDescription("**Name: **" + event.getRule().getName()
                + "\n**User: **" + event.getRule().getGuild().retrieveMemberById(event.getRule().getCreatorIdLong()).complete().getAsMention()
                + "\n**Trigger: **" + event.getRule().getTriggerType());
        system.sendMessageEmbeds(embbuild.build()).queue();

    }

    @Override
    public void onAutoModRuleUpdate(@NotNull AutoModRuleUpdateEvent event) {

        if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.AUTOMOD_RULE_UPDATE, event.getRule().getGuild())) {
            return;
        }

        GuildMessageChannel system = getSystemChannel(event.getRule().getGuild());
        EmbedBuilder embbuild = EmbedUtils.getDefault(event.getRule().getGuild());

        embbuild.setColor(Color.yellow);
        embbuild.setTitle("AutomodRule updated");
        embbuild.setDescription("**Name: **" + event.getRule().getName()
                + "\n**User: **" + event.getRule().getGuild().retrieveMemberById(event.getRule().getCreatorIdLong()).complete().getAsMention()
                + "\n**Trigger: **" + event.getRule().getTriggerType());
        system.sendMessageEmbeds(embbuild.build()).queue();

    }

}