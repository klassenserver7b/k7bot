package de.klassenserver7b.k7bot.commands.common;

import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import de.klassenserver7b.k7bot.util.HelpCategories;
import de.klassenserver7b.k7bot.util.errorhandler.PermissionError;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author K7
 */
public class MemberInfoCommand implements ServerCommand {

    private boolean isEnabled;

    @Override
    public String getHelp() {
        return "Zeigt die Informationen zum angegebenen User.\n - z.B. [prefix]memberinfo [@USER]";
    }

    @Override
    public String[] getCommandStrings() {
        return new String[]{"memberinfo"};
    }

    @Override
    public HelpCategories getCategory() {
        return HelpCategories.MODERATION;
    }

    @Override
    public void performCommand(Member caller, GuildMessageChannel channel, Message message) {

        channel.sendTyping().queue();
        List<Member> mentions = message.getMentions().getMembers();

        if (caller.hasPermission(Permission.KICK_MEMBERS, Permission.NICKNAME_MANAGE, Permission.NICKNAME_CHANGE)) {
            for (Member mention : mentions) {
                onInfo(caller, mention, channel);
            }
        } else {
            PermissionError.onPermissionError(caller, channel);
        }
    }

    public void onInfo(Member requester, Member u, GuildMessageChannel channel) {

        StringBuilder description = new StringBuilder()
                .append("**User: **").append(u.getAsMention()).append("\n")
                .append("**ClientId: **").append(u.getId()).append("\n")
                .append("**TimeJoined: **").append(u.getTimeJoined()).append("\n")
                .append("**TimeCreated: **").append(u.getTimeCreated()).append("\n")
                .append("**Nickname: **").append(u.getNickname()).append("\n")
                .append("**Status: **").append(u.getOnlineStatus()).append("\n")
                .append("**Is Owner: **").append(u.isOwner()).append("\n")
                .append("**Permissions: **").append(u.getPermissions()).append("\n\n");

        description.append("**Roles: **\n")
                .append(u.getRoles().stream()
                        .map(Role::getAsMention)
                        .collect(Collectors.joining(" ")))
                .append("\n");

        EmbedBuilder builder = EmbedUtils.getBuilderOf(Color.red, description,
                channel.getGuild().getIdLong());
        builder.setTitle("Info zu " + u.getEffectiveName());
        builder.setFooter("Requested by @" + requester.getEffectiveName());
        builder.setThumbnail(u.getUser().getEffectiveAvatarUrl());

        channel.sendMessageEmbeds(builder.build())
                .queue(msg -> msg.delete().queueAfter(20L, TimeUnit.SECONDS));
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
