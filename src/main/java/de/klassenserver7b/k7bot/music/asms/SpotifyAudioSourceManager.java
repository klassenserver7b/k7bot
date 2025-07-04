/**
 *
 */
package de.klassenserver7b.k7bot.music.asms;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpConfigurable;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterfaceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.music.spotify.SpotifyAudioTrack;
import de.klassenserver7b.k7bot.music.spotify.SpotifyInteractions;
import de.klassenserver7b.k7bot.music.spotify.SpotifySearchProvider;
import de.klassenserver7b.k7bot.music.spotify.loader.DefaultSpotifyPlaylistLoader;
import de.klassenserver7b.k7bot.music.spotify.loader.DefaultSpotifyTrackLoader;
import de.klassenserver7b.k7bot.music.spotify.loader.SpotifyPlaylistLoader;
import de.klassenserver7b.k7bot.music.spotify.loader.SpotifyTrackLoader;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @author Klassenserver7b
 */
public class SpotifyAudioSourceManager implements AudioSourceManager, HttpConfigurable {

    /**
     *
     */
    private static final String URL_REGEX = "^(https?://(?:[^.]+\\.|)spotify\\.com)/(?:intl-.{2}/)?(track|playlist)/([a-zA-Z0-9-_]+)/?(?:\\?.*|)$";
    private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);

    private final HttpInterfaceManager combinedHttpConfiguration;
    private final SpotifyPlaylistLoader playlistLoader;
    private final SpotifyTrackLoader trackLoader;
    private final Logger log;
    private final SpotifySearchProvider searchProvider;

    private File tempdir;

    private final SpotifyInteractions spotifyInteract;

    /**
     * @param combinedHttpConfiguration the http configuration to use
     * @param playlistLoader            the playlist loader to use
     * @param spotifyInteract           the spotify interactions to use
     */
    public SpotifyAudioSourceManager(HttpInterfaceManager combinedHttpConfiguration,
                                     SpotifyPlaylistLoader playlistLoader, SpotifyTrackLoader trackLoader,
                                     @NotNull SpotifyInteractions spotifyInteract) {

        log = LoggerFactory.getLogger(this.getClass());
        this.combinedHttpConfiguration = combinedHttpConfiguration;
        this.playlistLoader = playlistLoader;
        this.trackLoader = trackLoader;
        this.spotifyInteract = spotifyInteract;
        this.searchProvider = new SpotifySearchProvider(spotifyInteract);

        try {

            this.tempdir = Files.createTempDirectory("k7bot_spotify_" + System.currentTimeMillis()).toFile();

        } catch (IOException e) {

            this.tempdir = new File(".cache");

            // Delete old cache
            if (tempdir.exists()) {
                try (Stream<Path> tree = Files.walk(tempdir.toPath())) {
                    tree.sorted(Comparator.reverseOrder()).forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e1) {
                            log.error(e1.getMessage(), e1);
                        }
                    });
                } catch (IOException ex) {
                    log.error("Could not delete old tempdir: {}", tempdir.getAbsolutePath());
                }
            }

            // Create new cache
            if (!tempdir.mkdirs()) {
                log.error("Could not create tempdir: {}", tempdir.getAbsolutePath());
            }

        }

    }

    /**
     *
     */
    public SpotifyAudioSourceManager() {

        this(HttpClientTools.createDefaultThreadLocalManager(), new DefaultSpotifyPlaylistLoader(),
                new DefaultSpotifyTrackLoader(), K7Bot.getInstance().getSpotifyinteractions());

    }

    /**
     *
     */
    @Override
    public void configureRequests(Function<RequestConfig, RequestConfig> configurator) {
        combinedHttpConfiguration.configureRequests(configurator);
    }

    /**
     *
     */
    @Override
    public void configureBuilder(Consumer<HttpClientBuilder> configurator) {
        combinedHttpConfiguration.configureBuilder(configurator);
    }

    /**
     *
     */
    @Override
    public String getSourceName() {
        return "spotify";
    }

    /**
     *
     */
    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {

        if (reference == null || reference.identifier == null || !spotifyInteract.isApienabled()) {
            return null;
        }

        String url = reference.identifier;

        if (url.startsWith("spsearch: ")) {
            String trackId = searchProvider.searchTrackByQuery(url.substring(10));

            if (trackId == null) {
                return null;
            } else {
                return loadTrack(trackId);
            }

        }

        Matcher matcher = URL_PATTERN.matcher(url);

        if (matcher.matches()) {
            String type = matcher.group(2);
            String id = matcher.group(3);

            log.debug("Try SpotifyLoad on trackid: '{}' with url: '{}'", id, reference.identifier);

            switch (type) {
                case "track" -> {
                    return loadTrack(id);
                }
                case "playlist" -> {
                    return loadPlaylist(id);
                }

            }
        }

        return null;
    }

    /**
     * @param trackid the track id
     * @return the audio item
     */
    private AudioItem loadTrack(String trackid) {

        Function<AudioTrackInfo, AudioTrack> trackfactory = SpotifyAudioSourceManager.this::buildTrackFromInfo;

        return trackLoader.load(spotifyInteract, trackid, trackfactory);

    }

    /**
     * @param playlistid the playlist id
     * @return the audio item
     */
    private AudioItem loadPlaylist(String playlistid) {

        assert spotifyInteract.isApienabled();

        Function<AudioTrackInfo, AudioTrack> trackfactory = SpotifyAudioSourceManager.this::buildTrackFromInfo;

        return playlistLoader.load(spotifyInteract, playlistid, null, trackfactory);
    }

    /**
     * @param info the track info
     * @return the audio track
     */
    private SpotifyAudioTrack buildTrackFromInfo(AudioTrackInfo info) {
        return new SpotifyAudioTrack(info, this);
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return true;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) {
        // No custom values that need saving
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) {
        return new SpotifyAudioTrack(trackInfo, this);
    }

    @Override
    public void shutdown() {
        spotifyInteract.shutdown();
        try {
            combinedHttpConfiguration.close();
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }
    }

    public static String getArtistString(ArtistSimplified[] artists) {
        StringBuilder artist = new StringBuilder();

        for (ArtistSimplified a : artists) {
            artist.append(", ").append(a.getName());
        }

        artist = new StringBuilder(artist.substring(2));

        return artist.toString();
    }

    public File getTempdir() {
        return tempdir;
    }

}
