package de.klassenserver7b.k7bot.music.utilities;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.music.lavaplayer.Queue;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistsItemsRequest;

import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Felix
 * @hidden
 * @deprecated
 */
@Deprecated
public class SpotifyConverter {

    private static final String URL_REGEX = "^(https?://(?:[^.]+\\.|)spotify\\.com)/(track|playlist)/([a-zA-Z0-9-_]+)/?(?:\\?.*|)$";
    private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);

    private String acctkn;
    private Long isoexpiration;
    private String clientId;
    public static Thread converter;
    private final Logger logger = LoggerFactory.getLogger("SpotifyConverter");

    /**
     * @deprecated use {@link de.klassenserver7b.k7bot.music.asms.SpotifyAudioSourceManager} insted
     */
    @Deprecated
    public static void interrupt() {
        if (converter != null) {
            converter.interrupt();
        }
    }

    /**
     * @return AccessToken
     * @deprecated use {@link de.klassenserver7b.k7bot.music.asms.SpotifyAudioSourceManager} insted
     */
    @Deprecated
    public String checkAccessToken() {

        if ((isoexpiration == null) || (isoexpiration <= new Date().getTime())) {
            return retrieveToken();
        }
        return this.acctkn;
    }

    /**
     * @return AccessToken
     */
    private String retrieveToken() {

        try (final CloseableHttpClient client = HttpClients.createSystem()) {

            final HttpGet httpget = new HttpGet("https://open.spotify.com/get_access_token");

            try (final CloseableHttpResponse response = client.execute(httpget)) {

                if (response.getCode() == 200) {

                    JsonObject resp = JsonParser.parseString(EntityUtils.toString(response.getEntity()))
                            .getAsJsonObject();

                    String token = resp.get("accessToken").getAsString();
                    if (token != null && !token.equalsIgnoreCase("")) {
                        isoexpiration = resp.get("accessTokenExpirationTimestampMs").getAsLong();
                        clientId = resp.get("clientId").getAsString();
                        this.acctkn = token;
                        return token;
                    }

                } else {
                    logger.debug("Couldn't request a new AccessToken -> bad statuscode");
                }
            } catch (IOException | JsonSyntaxException | ParseException e) {
                logger.error(e.getMessage(), e);
            }

        } catch (IOException e1) {
            logger.error(e1.getMessage(), e1);
        }

        return acctkn;
    }

    /**
     * @return AccessToken
     * @deprecated use {@link de.klassenserver7b.k7bot.music.asms.SpotifyAudioSourceManager} insted
     */
    @Deprecated
    public String getAccessToken() {

        logger.debug("Spotify-Accesstoken refresh requested");
        return checkAccessToken();

    }

    /**
     * @param playlistId PlaylistId
     * @param load       Message
     * @param vc         AudioChannel
     * @deprecated use {@link de.klassenserver7b.k7bot.music.asms.SpotifyAudioSourceManager} insted
     */
    @Deprecated
    public void convertPlaylist(String playlistId, Message load, AudioChannel vc) {

        Matcher m = URL_PATTERN.matcher(playlistId);

        final String id;

        if (m.matches()) {
            id = m.group(3);
        } else {
            id = playlistId;
        }

        String acctkn = checkAccessToken();

        converter = new Thread(() -> loadSpotifyData(id, acctkn, load, vc));

        converter.setName("SpotifyConversionThread");
        converter.start();

    }

    /**
     * @param playlistId PlaylistId
     * @param acctkn     AccessToken
     * @param mess       Message
     * @param vc         AudioChannel
     */
    protected void loadSpotifyData(String playlistId, String acctkn, Message mess, AudioChannel vc) {
        long delay = System.currentTimeMillis();
        final SpotifyApi spotifyapi = new SpotifyApi.Builder().setClientId(clientId).setAccessToken(acctkn).build();

        GetPlaylistsItemsRequest getplaylistitemsrequest = spotifyapi.getPlaylistsItems(playlistId).build();

        List<String> searchquery = new ArrayList<>();

        // Dreigeteiltes Abrufen der SongData für entweder Anzahl ≤100 (verwendet nur
        // erste Query); Anzahl % 100 == 0 (verwendet nur die ersten 2 Querys) und
        // Anzahl > 100 && !(Anzahl%100==0) (Verwendet alle 3 querys)
        try {

            // Abrufen wie viele Songs in Playlist und abrufen der ersten (max.100) Songs
            logger.debug("Started first Song Request");
            Paging<PlaylistTrack> playlisttracks = getplaylistitemsrequest.execute();
            logger.debug("Spotify-Request Successful");

            // berechnen wie oft angefragt werden muss, um gesamte playlist abzurufen
            int times = (playlisttracks.getTotal() / 100);

            // Laden der Items und in YTquery list packen
            PlaylistTrack[] tracks = playlisttracks.getItems();
            for (PlaylistTrack playlistTrack : tracks) {

                Track track = (Track) playlistTrack.getTrack();
                searchquery.add(track.getName() + " - " + track.getArtists()[0].getName());

            }

            // abrufen aller anderer SongPakete wenn times >= 1
            for (int i = 1; i < times; i++) {
                logger.debug("loading 100 additional Tracks; times:{}", i);
                // berechnen des Offsets
                int offset = 100 * i;

                // Definieren der neuen API Request
                getplaylistitemsrequest = spotifyapi.getPlaylistsItems(playlistId).limit(100).offset(offset).build();

                // Laden der Items und in YTquery list packen
                Paging<PlaylistTrack> pagedplaylisttracks = getplaylistitemsrequest.execute();
                logger.debug("Spotify-Request Successful");
                tracks = pagedplaylisttracks.getItems();
                for (PlaylistTrack playlistTrack : tracks) {

                    Track track = (Track) playlistTrack.getTrack();
                    searchquery.add(track.getName() + " - " + track.getArtists()[0].getName());

                }
            }

            /*
             * Wenn for ausgeführt wurde d.h. mehr als 100 playlist items und nicht alles
             * mit for abgedeckt d.h. z.B. 410 Items → nach for erst 400 abgerufen → Abruf
             * der letzten 10 Items
             */

            if (times >= 1 && (playlisttracks.getTotal() % 100) != 0) {
                logger.debug("Loading final {} tracks", playlisttracks.getTotal() % 100);
                // Definieren der neuen API Request
                int limit = playlisttracks.getTotal() % 100;
                getplaylistitemsrequest = spotifyapi.getPlaylistsItems(playlistId).limit(limit).offset(times * 100)
                        .build();

                // Laden der Items und in YTquery list packen
                playlisttracks = getplaylistitemsrequest.execute();
                logger.debug("Spotify-Request Successful");
                tracks = playlisttracks.getItems();
                for (PlaylistTrack playlistTrack : tracks) {

                    Track track = (Track) playlistTrack.getTrack();
                    searchquery.add(track.getName() + " - " + track.getArtists()[0].getName());

                }

            }

        } catch (ParseException | SpotifyWebApiException | IOException e) {
            logger.error(e.getMessage(), e);
        }

        logger.info("started Ytsearch");
        List<AudioTrack> ytquerylist = loadtYTSearchQuery(searchquery, vc);

        delay = System.currentTimeMillis() - delay;
        EmbedBuilder builder = (new EmbedBuilder()).setColor(Color.decode("#4d05e8")).setTimestamp(LocalDateTime.now())
                .setTitle(ytquerylist.size() + " tracks added to queue").setDescription(ytquerylist.size()
                        + " Spotify tracks were successful loaded!\nThis took " + delay / 1000 + " seconds.");

        mess.delete().queue();
        MusicUtil.sendEmbed(vc.getGuild().getIdLong(), builder);
    }

    /**
     * @param searchquery List of searchqueries
     * @param vc          AudioChannel
     * @return List of AudioTracks
     */
    protected List<AudioTrack> loadtYTSearchQuery(List<String> searchquery, AudioChannel vc) {

        List<AudioTrack> yttracks = new ArrayList<>();

        if (searchquery != null && !searchquery.isEmpty()) {

            logger.debug("YTSearchQuery got a valid List");

            AudioPlayerManager manager = new DefaultAudioPlayerManager();
            manager.registerSourceManager(new YoutubeAudioSourceManager());
            Queue queue = K7Bot.getInstance().getPlayerUtil().getController(vc.getGuild().getIdLong())
                    .getQueue();

            AudioLoadResultHandler handler = new AudioLoadResultHandler() {

                @Override
                public void trackLoaded(AudioTrack track) {
                    yttracks.add(track);
                    queue.addTrackToQueue(track);

                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {

                    List<AudioTrack> tracklist = playlist.getTracks();

                    if (!tracklist.isEmpty()) {

                        yttracks.add(tracklist.getFirst());
                        queue.addTrackToQueue(tracklist.getFirst());

                    }

                }

                @Override
                public void noMatches() {
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                }

            };

            searchquery.forEach(trackinfo -> {

                logger.debug(trackinfo);

                try {
                    manager.loadItem("ytsearch: " + trackinfo, handler).get();
                } catch (InterruptedException | ExecutionException e) {
                    logger.error(e.getMessage(), e);
                }

            });
        }
        return yttracks;
    }
}
