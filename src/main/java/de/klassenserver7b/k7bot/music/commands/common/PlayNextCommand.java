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
public class PlayNextCommand extends GenericPlayCommand {

    private boolean isEnabled;

    /**
     *
     */
    public PlayNextCommand() {
        super();
    }

    @Override
    public String getHelp() {
        return "Lädt den/die ausgewählte/-n Track / Livestream / Playlist und fügt ihn/sie als nächste/-n in die Queue ein.\n - z.B. [prefix]playnext [url / YouTube Suchbegriff]";
    }

    @Override
    public String[] getCommandStrings() {
        return new String[]{"playnext", "pn"};
    }

    @Override
    protected AudioLoadResult generateAudioLoadResult(MusicController controller, String url) {
        return new AudioLoadResult(controller, url, AudioLoadOption.NEXT);
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
