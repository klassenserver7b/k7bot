/**
 *
 */
package de.klassenserver7b.k7bot.threads;

import de.klassenserver7b.k7bot.K7Bot;
import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.io.CloseMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author K7
 */
@SuppressWarnings("BusyWait")
public class SpotifyTokenRefresher implements AutoCloseable {

    private ScheduledFuture<?> refreshTask;
    private final Logger log;
    private long lifetime = 3600; // in seconds
    private CloseableHttpClient httpclient;
    private ScheduledExecutorService scheduler;

    /**
     *
     */
    public SpotifyTokenRefresher() {

        log = LoggerFactory.getLogger(this.getClass());
        httpclient = HttpClients.createSystem();
        scheduler = Executors.newSingleThreadScheduledExecutor();

    }

    public boolean start() {

        if (K7Bot.getInstance().isInExit()) {
            return false;
        }

        try {
            // First, refresh the token immediately to get the initial lifetime
            if (!refreshToken()) {
                log.error("Initial token refresh failed!");
                return false;
            }

            // Now schedule periodic refreshes with the known lifetime
            long refreshPeriod = Math.max(lifetime - 5, 60); // Ensure it's at least 60 seconds
            refreshTask = scheduler.scheduleAtFixedRate(new TokenRefreshRunnable(), refreshPeriod, refreshPeriod, TimeUnit.SECONDS);
            log.info("Spotify token refresh scheduled every {} seconds", refreshPeriod);
            return true;
        } catch (Exception e) {
            log.error("Spotify token refresh setup failed! {}", e.getMessage(), e);
            return false;
        }
    }

    public void restart() {

        if (refreshTask != null) {
            refreshTask.cancel(true);
        }
        this.start();
        log.info("Fetchthread restarted");
    }

    public void close() {
        if (refreshTask != null) {
            refreshTask.cancel(true);
        }
        if (scheduler != null) {
            scheduler.shutdown();
        }
        if (httpclient != null) {
            httpclient.close(CloseMode.IMMEDIATE);
        }
    }

    /**
     *
     */
    public boolean refreshToken() {

        final SpotifyApi spotifyApi = K7Bot.getInstance().getSpotifyinteractions().getSpotifyApi();

        try {
            final ClientCredentials clientCredentials = spotifyApi.clientCredentials().build().execute();
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());

            lifetime = clientCredentials.getExpiresIn();
            log.debug("Token refreshed at " + new Date());

            return true;

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            log.error("Spotify token refresh failed! {}", e.getMessage(), e);
            return false;
        }

    }

    class TokenRefreshRunnable implements Runnable {
        @Override
        public void run() {
            try {
                refreshToken();
                log.debug("spotify_authcode_refresh");
            } catch (Exception e) {
                log.error("Error during scheduled token refresh", e);
            }
        }
    }
}
