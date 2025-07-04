package de.klassenserver7b.k7bot.util.commands.common;

import de.klassenserver7b.k7bot.util.HelpCategories;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.sql.LiteSQL;
import de.klassenserver7b.k7bot.util.GenericMessageSendHandler;
import de.klassenserver7b.k7bot.util.errorhandler.PermissionError;
import de.klassenserver7b.k7bot.util.errorhandler.SyntaxError;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ReactRolesCommand implements ServerCommand {

    private boolean isEnabled;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public String getHelp() {
        return "Erstellt eine Reactionrole für die ausgewählte Nachricht mit dem ausgewählten Emote.\n - z.B. [prefix]reactrole #channel [messageId] :emote: @role";
    }

    @Override
    public String[] getCommandStrings() {
        return new String[]{"reactrole"};
    }

    @Override
    public HelpCategories getCategory() {
        return HelpCategories.TOOLS;
    }

    @Override
    public void performCommand(Member m, GuildMessageChannel channel, Message message) {

        if (m.hasPermission(Permission.MANAGE_ROLES)) {
            String[] args = message.getContentDisplay().split(" ");

            if (args.length == 5) {

                List<GuildMessageChannel> channels = message.getMentions().getChannels(GuildMessageChannel.class);
                List<Role> roles = message.getMentions().getRoles();
                List<CustomEmoji> emotes = message.getMentions().getCustomEmojis();

                if (!channels.isEmpty() && !roles.isEmpty()) {
                    GuildMessageChannel tc = channels.getFirst();
                    Role role = roles.getFirst();
                    String MessageIdString = args[2];

                    try {
                        long MessageId = Long.parseLong(MessageIdString);

                        if (!emotes.isEmpty()) {
                            CustomEmoji emote = emotes.getFirst();

                            tc.addReactionById(MessageId, emote).queue();

                            LiteSQL.onUpdate(
                                    "INSERT INTO reactroles(guildid, channelid, messageid, emote, roleid) VALUES(?, ?, ?, ?, ?);",
                                    channel.getGuild().getIdLong(), tc.getIdLong(), MessageId, emote.getIdLong(),
                                    role.getIdLong());
                        } else {
                            String utfemote = args[3];
                            tc.addReactionById(MessageId, Emoji.fromUnicode(utfemote)).queue();

                            LiteSQL.onUpdate(
                                    "INSERT INTO reactroles(guildid, channelid, messageid, emote, roleid) VALUES(?, ?, ?, ?, ?);",
                                    channel.getGuild().getIdLong(), tc.getIdLong(), MessageId, utfemote,
                                    role.getIdLong());
                        }
                    } catch (NumberFormatException e) {
                        log.error(e.getMessage(), e);
                    }
                }
            } else {

                SyntaxError.oncmdSyntaxError(new GenericMessageSendHandler(channel),
                        "reactrole [#channel] [messageId] [:emote:] [@role]", m);
            }
        } else {
            PermissionError.onPermissionError(m, channel);
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