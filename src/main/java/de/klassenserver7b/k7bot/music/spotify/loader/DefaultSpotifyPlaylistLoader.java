/**
 *
 */
package de.klassenserver7b.k7bot.music.spotify.loader;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import de.klassenserver7b.k7bot.music.asms.SpotifyAudioSourceManager;
import de.klassenserver7b.k7bot.music.spotify.SpotifyInteractions;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistsItemsRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Function;

/**
 * @author K7
 */
public class DefaultSpotifyPlaylistLoader implements SpotifyPlaylistLoader {

    private final Logger log;

    /**
     *
     */
    public DefaultSpotifyPlaylistLoader() {
        log = LoggerFactory.getLogger(this.getClass());
    }

    /**
     *
     */
    @Override
    public AudioPlaylist load(SpotifyInteractions spotifyinteract, String playlistId, String selectedVideoId,
                              Function<AudioTrackInfo, AudioTrack> trackFactory) {

        SpotifyApi spotifyapi = spotifyinteract.getSpotifyApi();

        String playlistname = getPlaylistname(playlistId, spotifyapi);
        ArrayList<Track> tracks = loadTracks(playlistId, spotifyapi);

        ArrayList<AudioTrack> resolvedtracks = new ArrayList<>();

        for (Track t : tracks) {

            String artist = SpotifyAudioSourceManager.getArtistString(t.getArtists());

            AudioTrackInfo info = new AudioTrackInfo(t.getName(), artist, t.getDurationMs(), t.getId(), false,
                    "https://open.spotify.com/track/" + t.getId());

            resolvedtracks.add(trackFactory.apply(info));

        }

        System.out.println(tracks.size());

        return new BasicAudioPlaylist(playlistname, resolvedtracks, resolvedtracks.getFirst(), false);
    }

    private String getPlaylistname(String playlistId, SpotifyApi sapi) {

        GetPlaylistRequest getplaylistrequest = sapi.getPlaylist(playlistId).build();

        try {

            Playlist playlist = getplaylistrequest.execute();

            return playlist.getName();

        } catch (ParseException | SpotifyWebApiException | IOException e) {
            log.error(e.getMessage(), e);
        }

        return "";

    }

    private ArrayList<Track> loadTracks(String playlistId, SpotifyApi sapi) {

        GetPlaylistsItemsRequest getplaylistitemsrequest = sapi.getPlaylistsItems(playlistId).build();

        ArrayList<Track> ptracks = new ArrayList<>();

        // Zweigeteiltges Abrufen der SongData für entweder Anzahl ≤100 (verwendet nur
        // erste Query); Anzahl % 100 == 0

        try {

            // Abrufen wie viele Songs in Playlist und abrufen der ersten (max.100) Songs
            Paging<PlaylistTrack> playlisttracks = getplaylistitemsrequest.execute();

            // berechnen wie oft angefragt werden muss, um gesamte playlist abzurufen
            int times = (playlisttracks.getTotal() / 100);

            // Laden der Items und in YTquery list packen
            PlaylistTrack[] tracks = playlisttracks.getItems();
            for (PlaylistTrack playlistTrack : tracks) {

                Track track = (Track) playlistTrack.getItem();
                ptracks.add(track);

            }

            // abrufen aller anderer SongPakete wenn times >= 1
            for (int i = 1; i <= times; i++) {

                // berechnen des Offsets
                int offset = 100 * i;
                int limit = 100;

                int diff = playlisttracks.getTotal() - offset;

                if (diff < 100 && diff > 0) {
                    limit = diff;
                }

                // Definieren der neuen API Request
                getplaylistitemsrequest = sapi.getPlaylistsItems(playlistId).limit(limit).offset(offset).build();

                // Laden der Items und in YTquery list packen
                Paging<PlaylistTrack> pagedplaylisttracks = getplaylistitemsrequest.execute();
                tracks = pagedplaylisttracks.getItems();
                for (PlaylistTrack playlistTrack : tracks) {

                    Track track = (Track) playlistTrack.getItem();
                    ptracks.add(track);

                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return ptracks;
    }
}
