/**
 *
 */
package de.klassenserver7b.k7bot.music.commands.common;

import de.klassenserver7b.k7bot.music.commands.generic.GenericPlayCommand;
import de.klassenserver7b.k7bot.music.lavaplayer.AudioLoadResult;
import de.klassenserver7b.k7bot.music.lavaplayer.MusicController;
import de.klassenserver7b.k7bot.music.utilities.AudioLoadOption;

/**
 * @author K7
 */
public class PlayCommand extends GenericPlayCommand {

    private boolean isEnabled;

    /**
     *
     */
    public PlayCommand() {
        super();
    }

    @Override
    public String getHelp() {
        return "Spielt den/die ausgewählte/-n Track / Livestream / Playlist.\n - z.B. [prefix]play [url / YouTube Suchbegriff]";
    }

    @Override
    public String[] getCommandStrings() {
        return new String[]{"play", "p"};
    }

    @Override
    protected AudioLoadResult generateAudioLoadResult(MusicController controller, String url) {
        return new AudioLoadResult(controller, url, AudioLoadOption.REPLACE);
    }

    @Override
    protected GenericPlayCommand getChildClass() {
        return this;
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
