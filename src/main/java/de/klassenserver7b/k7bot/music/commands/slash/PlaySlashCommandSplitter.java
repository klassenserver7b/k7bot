/**
 *
 */
package de.klassenserver7b.k7bot.music.commands.slash;

import de.klassenserver7b.k7bot.commands.types.SubSlashCommand;
import de.klassenserver7b.k7bot.commands.types.TopLevelSlashCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * @author K7
 *
 */
public class PlaySlashCommandSplitter implements TopLevelSlashCommand {

    private final ArrayList<SubSlashCommand> subcommands;
    private static final String name = "play";
    private static final String description = "Plays the submitted Track / Livestream / Playlist";

    /**
     *
     */
    public PlaySlashCommandSplitter() {
        subcommands = new ArrayList<>();
        subcommands.add(new PlaySlashCommand());
        subcommands.add(new PlayNextSlashCommand());
        subcommands.add(new AddToQueueSlashCommand());
        subcommands.add(new PlayPredefinedSlashCommand());

    }

    @NotNull
    @Override
    public SlashCommandData getCommandData() {

        ArrayList<SubcommandData> subdata = new ArrayList<>();

        for (SubSlashCommand subslash : subcommands) {
            subdata.add(subslash.getSubCommandData());
        }

        return Commands.slash(name, description).addSubcommands(subdata)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.VOICE_CONNECT))
                .setDescriptionLocalization(DiscordLocale.GERMAN,
                        "Spielt den/die ausgewählte/-n Track / Livestream / Playlist")
                .setContexts(InteractionContextType.GUILD);
    }

    @Override
    public void performSlashCommand(SlashCommandInteraction event) {

        String strippedpath = event.getFullCommandName().substring(name.length()).replaceAll(" ", "");

        for (SubSlashCommand subslash : subcommands) {
            if (subslash.getSubPath().equalsIgnoreCase(strippedpath)) {
                subslash.performSlashCommand(event);
            }
        }
    }

}
