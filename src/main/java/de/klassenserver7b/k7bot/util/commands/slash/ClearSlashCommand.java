package de.klassenserver7b.k7bot.util.commands.slash;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.types.TopLevelSlashCommand;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import de.klassenserver7b.k7bot.util.MessageClearUtil;
import de.klassenserver7b.k7bot.util.errorhandler.PermissionError;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class ClearSlashCommand implements TopLevelSlashCommand {
    @Override
    public void performSlashCommand(SlashCommandInteraction event) {

        if (event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {

            OptionMapping amountOption = event.getOption("amount");

            InteractionHook hook = event.deferReply(true).complete();
            int amount;
            amount = (int) amountOption.getAsLong();

            if (amount > 200) {
                hook.sendMessage("Aufgrund von Zugriffslimitierungen, kann ich nicht mehr als 200 Nachrichten löschen!")
                        .queue();
                return;
            }

            MessageClearUtil.onclear(amount - 1, event.getChannel().asGuildMessageChannel());

            hook.sendMessage(amount + " messages deleted.").queue();

            EmbedBuilder builder = EmbedUtils
                    .getBuilderOf(Color.orange,
                            amount + " messages deleted!\n\n" + "**Channel: **\n" + "#"
                                    + event.getChannel().asGuildMessageChannel().getName(),
                            event.getGuild().getIdLong());

            builder.setFooter("requested by @" + event.getMember().getEffectiveName());
            GuildMessageChannel system = K7Bot.getInstance().getSysChannelMgr()
                    .getSysChannel(event.getGuild());

            if (system != null) {
                system.sendMessageEmbeds(builder.build()).queue();
            }

        } else {

            PermissionError.onPermissionError(event.getMember(), event.getChannel().asGuildMessageChannel());
        }
    }

    @Override
    public @NotNull SlashCommandData getCommandData() {
        return Commands.slash("clear", "Löscht die ausgewählte Anzahl an Nachrichten.")
                .addOptions(
                        new OptionData(OptionType.INTEGER, "amount", "Wie viele Nachrichten sollen gelöscht werden?")
                                .setRequired(true))
                .setContexts(InteractionContextType.GUILD)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE));
    }
}