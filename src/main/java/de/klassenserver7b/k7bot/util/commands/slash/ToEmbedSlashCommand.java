package de.klassenserver7b.k7bot.util.commands.slash;

import de.klassenserver7b.k7bot.commands.types.TopLevelSlashCommand;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import de.klassenserver7b.k7bot.util.errorhandler.PermissionError;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class ToEmbedSlashCommand implements TopLevelSlashCommand {

    @Override
    public void performSlashCommand(SlashCommandInteraction event) {

        if (event.getMember().hasPermission(Permission.MESSAGE_SEND)) {

            OptionMapping title = event.getOption("title");
            OptionMapping description = event.getOption("description");
            OptionMapping coloropt = event.getOption("color");

            InteractionHook hook = event.deferReply().complete();

            EmbedBuilder builder = EmbedUtils.getBuilderOf(description.getAsString().replaceAll("<br>", "\n"), event.getGuild().getIdLong());

            builder.setTitle(title.getAsString());
            builder.setFooter("requested by @" + event.getMember().getEffectiveName());

            if (coloropt != null) {

                String colortxt;
                colortxt = coloropt.getAsString();

                if (!colortxt.startsWith("#")) {
                    colortxt = "#" + colortxt;
                }

                Color color = Color.decode(colortxt);
                builder.setColor(color);

            }

            hook.sendMessageEmbeds(builder.build()).queue();

        } else {

            PermissionError.onPermissionError(event.getMember(), event.getChannel().asGuildMessageChannel());
        }

    }

    @Override
    public @NotNull SlashCommandData getCommandData() {
        return Commands.slash("toembed", "Erstellt einen Embed mit den gegebenen Parametern")
                .addOption(OptionType.STRING, "title", "Welchen Titel soll der Embed haben?", true)
                .addOption(OptionType.STRING, "description", "Welchen Inhalt soll der Embed haben?", true)
                .addOption(OptionType.STRING, "color", "Die Farbe des Embeds als hexadezimale Zahl");
    }

}
