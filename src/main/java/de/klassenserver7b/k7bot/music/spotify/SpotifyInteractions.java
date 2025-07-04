/**
 *
 */
package de.klassenserver7b.k7bot.music.spotify;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.threads.SpotifyTokenRefresher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.SpotifyApi;

/**
 * @author K7
 */
public class SpotifyInteractions {

    private boolean apienabled;
    private SpotifyApi spotifyApi;
    public SpotifyTokenRefresher tokenRefresher;
    private final Logger log;

    public SpotifyInteractions() {
        this.log = LoggerFactory.getLogger(this.getClass());
        apienabled = false;
    }

    public void initialize() {

        this.spotifyApi = new SpotifyApi.Builder().setClientId(K7Bot.getInstance().getPropertiesManager().getProperty("spotify-client-id"))
                .setClientSecret(K7Bot.getInstance().getPropertiesManager().getProperty("spotify-client-secret"))
                .build();
        this.apienabled = true;

        startfetchcycle();
    }

    /**
     *
     */
    public void startfetchcycle() {
        this.tokenRefresher = new SpotifyTokenRefresher();
        if (!this.tokenRefresher.start()) {
            this.apienabled = false;
            log.warn("Spotify token refresh failed - API disabled");
        }
    }

    public void shutdown() {
        tokenRefresher.close();
    }

    public void restart() {
        tokenRefresher.restart();
    }

    public boolean isApienabled() {
        return apienabled;
    }

    public SpotifyApi getSpotifyApi() {
        return spotifyApi;
    }

}