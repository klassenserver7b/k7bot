package de.klassenserver7b.k7bot.util.commands.slash;

import de.klassenserver7b.k7bot.commands.types.TopLevelSlashCommand;
import de.klassenserver7b.k7bot.sql.LiteSQL;
import de.klassenserver7b.k7bot.util.errorhandler.PermissionError;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

public class ReactRolesSlashCommand implements TopLevelSlashCommand {

    @Override
    public void performSlashCommand(SlashCommandInteraction event) {

        InteractionHook hook = event.deferReply(true).complete();
        Member m = event.getMember();

        if (m.hasPermission(Permission.MANAGE_ROLES)) {

            OptionMapping channel = event.getOption("channel");
            OptionMapping messageid = event.getOption("messageid");
            OptionMapping emoteop = event.getOption("emoteid-oder-utfemote");
            OptionMapping roleop = event.getOption("role");

            GuildMessageChannel tc = channel.getAsChannel().asGuildMessageChannel();
            Role role = roleop.getAsRole();
            long MessageId = messageid.getAsLong();

            Emoji emote = Emoji.fromFormatted(emoteop.getAsString());

            tc.addReactionById(MessageId, emote).queue();

            LiteSQL.onUpdate(
                    "INSERT INTO reactroles(guildid, channelid, messageid, emote, roleid) VALUES(?, ?, ?, ?, ?);",
                    tc.getGuild().getIdLong(), tc.getIdLong(), MessageId, emote.getFormatted(), role.getIdLong());

            hook.sendMessage("Reactrole was successfull set for Message: " + MessageId).queue();

        } else {
            PermissionError.onPermissionError(m, event.getChannel().asGuildMessageChannel());
        }

    }

    @Override
    public @NotNull SlashCommandData getCommandData() {
        return Commands.slash("reactrole", "Erstellt eine Reactionrole mit den übermittelten Parametern")
                .addOption(OptionType.CHANNEL, "channel", "Der Channel in dem die Message ist", true)
                .addOption(OptionType.STRING, "messageid",
                        "Die MessageId der Message an die die Reaction angefügt werden soll", true)
                .addOption(OptionType.STRING, "emoteid-oder-utfemote",
                        "Die EmoteId des Emotes bzw. das UTF8 Emoji auf das die Rolle registriert werden soll", true)
                .addOption(OptionType.ROLE, "role",
                        "Die Rolle die zugewiesen werden soll -  stelle sicher: Rechte und Rolle des Bots > Rechte der Rolle",
                        true);
    }

}
