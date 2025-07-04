package de.klassenserver7b.k7bot.music.spotify;

import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.io.IOException;

public class SpotifySearchProvider {

    private final SpotifyInteractions spotifyInteract;
    private final Logger log;

    public SpotifySearchProvider(@NotNull SpotifyInteractions spotifyInteract) {
        this.spotifyInteract = spotifyInteract;
        this.log = LoggerFactory.getLogger(this.getClass());
    }

    public String searchTrackByQuery(String query) {
        try {
            Paging<Track> searchResults = spotifyInteract.getSpotifyApi().searchTracks(query).limit(1).build().execute();

            if (searchResults.getTotal() == 0) {
                return null;
            }

            return searchResults.getItems()[0].getId();

        } catch (IOException | ParseException | SpotifyWebApiException e) {
            log.warn(e.getMessage(), e);
        }
        return null;
    }

}
