/**
 *
 */
package de.klassenserver7b.k7bot.threads;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import de.klassenserver7b.k7bot.K7Bot;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.io.CloseMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author K7
 */
@SuppressWarnings("BusyWait")
public class SpotifyTokenRefresher implements AutoCloseable {

    private ScheduledFuture<?> refreshTask;
    private final Logger log;
    private long lifetimeMs;
    private static SpotifyTokenRefresher INSTANCE;
    private CloseableHttpClient httpclient;

    /**
     *
     */
    private SpotifyTokenRefresher() {

        INSTANCE = this;
        log = LoggerFactory.getLogger(this.getClass());
        httpclient = HttpClients.createSystem();
        start();

    }

    public void start() {

        if (K7Bot.getInstance().isInExit()) {
            return;
        }

        refreshToken();

        refreshTask = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            refreshToken();
            log.debug("spotify_authcode_refresh");
        }, 0, lifetimeMs - 500, TimeUnit.MILLISECONDS);
    }

    public void restart() {

        refreshTask.cancel(true);
        this.start();
        log.info("Fetchthread restarted");

    }

    public void close() {
        refreshTask.cancel(true);
        httpclient.close(CloseMode.IMMEDIATE);
    }

    /**
     *
     */
    public void refreshToken() {

        String url = "https://open.spotify.com/get_access_token";


        final HttpGet httpget = new HttpGet(url);
        httpget.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        try {
            final String response = httpclient.execute(httpget, new BasicHttpClientResponseHandler());
            JsonElement elem = JsonParser.parseString(response);

            K7Bot.getInstance().getSpotifyinteractions().getSpotifyApi()
                    .setAccessToken(elem.getAsJsonObject().get("accessToken").getAsString());

            lifetimeMs = elem.getAsJsonObject().get("accessTokenExpirationTimestampMs").getAsLong() - System.currentTimeMillis();

            log.debug("Token refreshed at " + new Date().toString());

        } catch (IOException | JsonSyntaxException e) {
            log.error(e.getMessage(), e);
        }

    }

    public static SpotifyTokenRefresher getINSTANCE() {
        if (INSTANCE != null) {
            return INSTANCE;
        }

        return INSTANCE = new SpotifyTokenRefresher();
    }
}
