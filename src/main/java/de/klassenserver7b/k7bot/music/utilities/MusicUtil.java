package de.klassenserver7b.k7bot.music.utilities;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.music.lavaplayer.MusicController;
import de.klassenserver7b.k7bot.music.spotify.SpotifyAudioTrack;
import de.klassenserver7b.k7bot.sql.LiteSQL;
import de.klassenserver7b.k7bot.util.GenericMessageSendHandler;
import dev.lavalink.youtube.track.YoutubeAudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.hc.core5.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Image;
import se.michaelthelin.spotify.model_objects.specification.Track;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author Klassenserver7b
 */
public class MusicUtil {

    private static final Logger log = LoggerFactory.getLogger(MusicUtil.class);

    /**
     * @param channel the channel to update
     */
    public static void updateChannel(GuildMessageChannel channel) {

        LiteSQL.onUpdate("INSERT OR REPLACE INTO musicutil(guildId, channelId) VALUES(?, ?);",
                channel.getGuild().getIdLong(), channel.getIdLong());

    }

    /**
     * @param hook the InteractionHook to get the channel from
     */
    public static void updateChannel(InteractionHook hook) {

        GuildMessageChannel channel = (GuildMessageChannel) hook.getInteraction().getMessageChannel();

        updateChannel(channel);
    }

    /**
     * @param guildid the guildid to get the channel from
     * @param builder the EmbedBuilder to send
     */
    public static void sendIconEmbed(long guildid, EmbedBuilder builder, AudioTrack track) {

        GuildMessageChannel channel = getMessageChannelByGuildId(guildid);
        if (channel == null) {
            return;
        }

        File f = setIcons(track);

        if (f != null) {

            try (FileUpload up = FileUpload.fromData(f, "thumbnail.jpg")) {

                builder.setImage("attachment://thumbnail.png");
                channel.sendFiles(up).setEmbeds(builder.build()).queue();
                up.close();
                return;

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        channel.sendMessageEmbeds(builder.build()).queue();

    }

    /**
     * @param guildid the guildid to get the channel from
     * @param builder the EmbedBuilder to send
     */
    public static void sendEmbed(long guildid, EmbedBuilder builder) {

        GuildMessageChannel channel = getMessageChannelByGuildId(guildid);
        if (channel == null) {
            return;
        }
        channel.sendMessageEmbeds(builder.build()).queue();
    }

    protected static GuildMessageChannel getMessageChannelByGuildId(long guildid) {

        GuildMessageChannel channel = null;

        try (ResultSet set = LiteSQL.onQuery("SELECT channelId FROM musicutil WHERE guildId = ?", guildid)) {

            assert set != null;
            if (!set.next()) {
                return null;
            }
            long channelid = set.getLong("channelId");
            Guild guild;

            if ((guild = K7Bot.getInstance().getShardManager().getGuildById(guildid)) == null) {
                return null;
            }

            GuildChannel gchan = guild.getGuildChannelById(channelid);

            if (gchan instanceof GuildMessageChannel) {
                channel = (GuildMessageChannel) gchan;
            }

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        return channel;
    }

    /**
     * @param m the member to check
     * @return true if the member is connected to a VoiceChannel
     */
    public static boolean membNotVcConnected(@Nonnull Member m) {

        GuildVoiceState state;
        if ((state = m.getVoiceState()) != null)
            return state.getChannel() == null;
        return true;
    }

    /**
     * @param m the member to get the AudioChannel from
     * @return the AudioChannel the member is connected to
     */
    public static AudioChannel getMembVcConnection(@Nonnull Member m) {

        GuildVoiceState state;
        if ((state = m.getVoiceState()) != null) {
            AudioChannel vc;
            if ((vc = state.getChannel()) != null) {
                return vc;
            }
        }
        return null;
    }

    /**
     * @param ac the AudioChannel to check
     * @return 0 if not connected, 1 if connected, 2 if not connected to any channel
     */
    public static int isConnectedtoChannel(AudioChannel ac) {
        AudioManager audioman = ac.getGuild().getAudioManager();

        if (audioman.getConnectedChannel() == null) {
            return 2;
        }

        if ((ac.getIdLong() == audioman.getConnectedChannel().getIdLong())) {
            return 1;
        }

        return 0;
    }

    /**
     * @param sendHandler the sendHandler to send the message
     * @param m           the member to check
     * @return true if the member is connected to the music playing VoiceChannel
     */
    public static boolean membFailsDefaultConditions(GenericMessageSendHandler sendHandler, @Nonnull Member m) {

        if (MusicUtil.membNotVcConnected(m)) {
            sendHandler.sendMessage("You are not in a voicechannel" + m.getAsMention()).complete().delete()
                    .queueAfter(10L, TimeUnit.SECONDS);
            return true;
        }

        return hasInvalidChannelConnection(sendHandler, m, true);
    }

    /**
     * @param sendHandler the sendHandler to send the message
     * @param m           the member to check
     * @return true if the member isn't connected to the right channel
     */
    public static boolean failsConditions(@Nonnull GenericMessageSendHandler sendHandler, @Nonnull Member m) {

        if (MusicUtil.membNotVcConnected(m)) {
            sendHandler.sendMessage("You are not in a voicechannel" + m.getAsMention()).complete().delete()
                    .queueAfter(10L, TimeUnit.SECONDS);
            return true;
        }

        return hasInvalidChannelConnection(sendHandler, m, false);
    }

    protected static boolean hasInvalidChannelConnection(GenericMessageSendHandler sendHandler, Member m,
                                                         boolean checkBeforePlay) {

        AudioChannel vc = MusicUtil.getMembVcConnection(m);

        switch (MusicUtil.isConnectedtoChannel(Objects.requireNonNull(vc))) {
            case 0: {
                sendHandler.sendMessage("You are not connected to the music playing VoiceChannel" + m.getAsMention())
                        .complete().delete().queueAfter(10L, TimeUnit.SECONDS);
                return true;
            }
            case 2: {

                if (checkBeforePlay) {
                    return false;
                }

                sendHandler.sendMessage("The Bot isn't playing a Song use -p [Song] to play one" + m.getAsMention())
                        .complete().delete().queueAfter(10L, TimeUnit.SECONDS);
                return true;
            }
        }

        return false;

    }

    /**
     * @param m the member to check
     * @return true if the member is connected to the music playing VoiceChannel
     */
    public static boolean failsConditions(@Nonnull Member m) {

        if (MusicUtil.membNotVcConnected(m)) {
            return false;
        }

        AudioChannel vc = MusicUtil.getMembVcConnection(m);

        return switch (MusicUtil.isConnectedtoChannel(Objects.requireNonNull(vc))) {
            case 0, 2 -> false;
            default -> true;
        };

    }

    /**
     * @param c the channel to check
     * @param m the member to check
     * @return true if the bot is playing a song
     */
    public static boolean isPlayingSong(GuildMessageChannel c, Member m) {

        MusicController controller = K7Bot.getInstance().getPlayerUtil()
                .getController(c.getGuild().getIdLong());

        if (controller == null) {
            c.sendMessage("The Bot isn't playing a Song use -p [Song] to play one" + m.getAsMention()).complete()
                    .delete().queueAfter(10L, TimeUnit.SECONDS);
            return false;
        }

        AudioPlayer player = controller.getPlayer();

        if (player.getPlayingTrack() == null) {
            c.sendMessage("The Bot isn't playing a Song use -p [Song] to play one" + m.getAsMention()).complete()
                    .delete().queueAfter(10L, TimeUnit.SECONDS);
            return false;
        }

        return true;

    }

    private static File setIcons(AudioTrack track) {

        if (track instanceof YoutubeAudioTrack) {
            return loadYTIcon(track.getIdentifier());
        }

        if (track instanceof SpotifyAudioTrack) {
            return loadSpotifyIcon(track.getIdentifier());
        }
        return null;

    }

    private static File loadSpotifyIcon(String songid) {

        final HttpGet httpget = new HttpGet("https://open.spotify.com/get_access_token");

        try (final CloseableHttpClient client = HttpClients.createSystem();
             final CloseableHttpResponse response = client.execute(httpget)) {

            if (response.getStatusLine().getStatusCode() != 200) {
                return null;
            }

            JsonObject resp = JsonParser.parseString(EntityUtils.toString(response.getEntity())).getAsJsonObject();

            String token = resp.get("accessToken").getAsString();

            SpotifyApi api = SpotifyApi.builder().setAccessToken(token).build();

            Track t = api.getTrack(songid).build().execute();

            Image[] images = t.getAlbum().getImages();

            Image img = images[0];

            for (Image imgs : images) {
                if (imgs.getHeight() > img.getHeight()) {
                    img = imgs;
                }
            }

            File tempfile = File.createTempFile("spotifyicon_" + new Date().getTime(), ".tmp");

            try (InputStream uin = new URI(img.getUrl()).toURL().openStream();
                 FileOutputStream fout = new FileOutputStream(tempfile)) {
                
                uin.transferTo(fout);
                uin.close();
                fout.close();

                return tempfile;
            }

        } catch (IOException | ParseException | SpotifyWebApiException | URISyntaxException e) {
            log.error(e.getMessage(), e);
            return null;
        }

    }

    private static File loadYTIcon(String videoId) {

        File tempfile;
        try {
            tempfile = File.createTempFile("spotifyicon_" + new Date().getTime(), ".tmp");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }

        try (InputStream file = (new URI("https://img.youtube.com/vi/" + videoId + "/maxresdefault.jpg")).toURL()
                .openStream()) {

            try (FileOutputStream fout = new FileOutputStream(tempfile)) {

                file.transferTo(fout);
                file.close();
                fout.close();

                return tempfile;
            }

        } catch (IOException | URISyntaxException e) {

            log.warn("No maxresdefault.jpg available");

            try (InputStream file = (new URI("https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg")).toURL()
                    .openStream()) {

                try (FileOutputStream fout = new FileOutputStream(tempfile)) {

                    file.transferTo(fout);
                    file.close();
                    fout.close();

                    return tempfile;
                }

            } catch (IOException | URISyntaxException e1) {
                log.error(e.getMessage(), e);
            }

        }

        return null;
    }
}