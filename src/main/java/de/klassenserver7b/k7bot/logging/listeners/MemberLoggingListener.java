package de.klassenserver7b.k7bot.logging.listeners;

import de.klassenserver7b.k7bot.logging.LoggingConfigDBHandler;
import de.klassenserver7b.k7bot.logging.LoggingOptions;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.stream.Collectors;

import static de.klassenserver7b.k7bot.util.ChannelUtil.getSystemChannel;

public class MemberLoggingListener extends ListenerAdapter {

    public MemberLoggingListener() {
        super();
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {

        if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.MEMBER_JOIN, event.getGuild())) {
            return;
        }

        GuildMessageChannel system = getSystemChannel(event);
        Member memb = event.getMember();

        EmbedBuilder embbuild = EmbedUtils.getDefault(event.getGuild());
        embbuild.setColor(Color.green);

        embbuild.setTitle("@" + memb.getEffectiveName() + " joined :thumbsup:");
        embbuild.setDescription(memb.getAsMention() + " joined");
        embbuild.setThumbnail(memb.getUser().getEffectiveAvatarUrl());

        system.sendMessageEmbeds(embbuild.build()).queue();
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {

        if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.MEMBER_LEAVE, event.getGuild())) {
            return;
        }

        GuildMessageChannel system = getSystemChannel(event);

        User usr = event.getUser();

        EmbedBuilder embbuild = EmbedUtils.getDefault(event.getGuild());
        embbuild.setColor(Color.red);

        embbuild.setTitle("@" + usr.getName() + " leaved :sob:");
        embbuild.setDescription(usr.getAsMention() + " known as " + usr.getEffectiveName() + " leaved");
        embbuild.setThumbnail(usr.getEffectiveAvatarUrl());

        system.sendMessageEmbeds(embbuild.build()).queue();
    }

    @Override
    public void onGuildMemberUpdateNickname(@Nonnull GuildMemberUpdateNicknameEvent event) {

        if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.MEMBER_UPDATE_NICKNAME, event.getGuild())) {
            return;
        }

        GuildMessageChannel system = getSystemChannel(event);

        Member memb = event.getMember();

        EmbedBuilder embbuild = EmbedUtils.getDefault();
        embbuild.setColor(Color.yellow);

        embbuild.setThumbnail(memb.getUser().getEffectiveAvatarUrl());
        embbuild.setTitle(memb.getEffectiveName() + "'s nickname changed");
        embbuild.setDescription(
                "**Old Nickname: **" + event.getOldNickname() + "\n**New Nickname: **" + event.getNewNickname());

        system.sendMessageEmbeds(embbuild.build()).queue();

    }

    @Override
    public void onGuildMemberRoleAdd(@Nonnull GuildMemberRoleAddEvent event) {

        if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.MEMBER_ROLE_ADD, event.getGuild())) {
            return;
        }

        GuildMessageChannel system = getSystemChannel(event);

        Member memb = event.getMember();

        EmbedBuilder embbuild = EmbedUtils.getDefault();
        embbuild.setColor(Color.yellow);

        embbuild.setTitle(memb.getEffectiveName() + "'s roles changed");
        embbuild.setDescription(memb.getAsMention() + "\n**Added Roles: **"
                + event.getRoles().stream().map(Role::getName).collect(Collectors.joining(", ")));

        system.sendMessageEmbeds(embbuild.build()).queue();

    }

    @Override
    public void onGuildMemberRoleRemove(@Nonnull GuildMemberRoleRemoveEvent event) {

        if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.MEMBER_ROLE_REMOVE, event.getGuild())) {
            return;
        }

        GuildMessageChannel system = getSystemChannel(event);

        Member memb = event.getMember();

        EmbedBuilder embbuild = EmbedUtils.getDefault();
        embbuild.setColor(Color.yellow);

        embbuild.setTitle(memb.getEffectiveName() + "'s roles changed");
        embbuild.setDescription(memb.getAsMention() + "\n**Removed Roles: **"
                + event.getRoles().stream().map(Role::getName).collect(Collectors.joining(", ")));

        system.sendMessageEmbeds(embbuild.build()).queue();

    }

}
