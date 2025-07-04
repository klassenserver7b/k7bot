/**
 *
 */
package de.klassenserver7b.k7bot.util;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.sql.SQLManager;

/**
 * @author K7
 */
public class RestartUtil {

    public static void restart() {
        K7Bot INSTANCE = K7Bot.getInstance();

        INSTANCE.getPlayerUtil().stopAllTracks();

        SQLManager.onCreate();

        INSTANCE.getSpotifyinteractions().restart();
        INSTANCE.restartLoop();

    }

}
