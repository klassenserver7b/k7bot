/**
 *
 */
package de.klassenserver7b.k7bot.music.commands.slash;

import de.klassenserver7b.k7bot.commands.types.SubSlashCommand;
import de.klassenserver7b.k7bot.music.SupportedPlayQueries;
import de.klassenserver7b.k7bot.music.commands.generic.GenericPlayCommand;
import de.klassenserver7b.k7bot.music.lavaplayer.AudioLoadResult;
import de.klassenserver7b.k7bot.music.lavaplayer.MusicController;
import de.klassenserver7b.k7bot.music.utilities.AudioLoadOption;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * @author K7
 *
 */
public class PlaySlashCommand extends GenericPlayCommand implements SubSlashCommand {

    @NotNull
    @Override
    public SubcommandData getSubCommandData() {
        ArrayList<Choice> targets = new ArrayList<>();
        for (SupportedPlayQueries q : SupportedPlayQueries.values()) {
            targets.add(new Choice(q.toString(), q.getId()));
        }
        OptionData target = new OptionData(OptionType.INTEGER, "target", "from where the song should be loaded")
                .addChoices(targets).setRequired(true);

        OptionData url = new OptionData(OptionType.STRING, "url", "The url/search query for the selected target")
                .setRequired(true);

        return new SubcommandData("immediately", "plays the selected AudioItem replacing the whole queue")
                .addOptions(target, url);
    }

    @Override
    public String getSubPath() {
        return "immediately";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected AudioLoadResult generateAudioLoadResult(MusicController controller, String url) {
        return new AudioLoadResult(controller, url, AudioLoadOption.REPLACE);
    }

    @Override
    protected GenericPlayCommand getChildClass() {
        return this;
    }

}
