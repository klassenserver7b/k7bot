package de.klassenserver7b.k7bot.music.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.music.utilities.MusicUtil;
import de.klassenserver7b.k7bot.music.utilities.SongJson;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.managers.AudioManager;

public class TrackScheduler extends AudioEventAdapter {

    public static boolean next = false;

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {

        TrackScheduler.next = false;

        long guildid = K7Bot.getInstance().getPlayerUtil().getGuildbyPlayerHash(player.hashCode());

        MusicController controller = K7Bot.getInstance().getPlayerUtil().getController(guildid);
        Queue queue = controller.getQueue();

        AudioTrackInfo info = track.getInfo();

        SongJson jsinfo = null;
        if (track instanceof YoutubeAudioTrack) {
            jsinfo = queue.getCurrentSongData();
        }

        String author = (jsinfo == null ? info.author : jsinfo.getAuthorString());
        String title = (jsinfo == null ? info.title : jsinfo.getTitle());

        EmbedBuilder builder = EmbedUtils.getSuccessEmbed("Jetzt läuft: " + title);

        long sekunden = info.length / 1000L;
        long minuten = sekunden / 60L;
        long stunden = minuten / 60L;
        minuten %= 60L;
        sekunden %= 60L;

        String url = info.uri;

        if (!(track instanceof LocalAudioTrack)) {
            builder.addField("Name", "[" + author + " - " + title + "](" + url.replaceAll(".api_key=.*", "") + ")", false);
        } else {
            builder.addField("Name", author + " - " + title, false);
        }
        builder.addField("Länge: ",
                info.isStream ? "LiveStream"
                        : (((stunden > 0L) ? (stunden + "h ") : "") + ((minuten > 0L) ? (minuten + "min ") : "")
                        + sekunden + "s"),
                true);

        MusicUtil.sendIconEmbed(guildid, builder, track);


    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {

        long guildid = K7Bot.getInstance().getPlayerUtil().getGuildbyPlayerHash(player.hashCode());
        Guild guild = K7Bot.getInstance().getShardManager().getGuildById(guildid);
        MusicController controller = K7Bot.getInstance().getPlayerUtil().getController(guildid);
        Queue queue = controller.getQueue();


        if (guild == null) {
            return;
        }
        AudioManager manager = guild.getAudioManager();

        if (endReason.mayStartNext) {

            if (queue.next(track) || player.getPlayingTrack() != null) {
                return;
            }

            player.stopTrack();
            queue.clearQueue();
            manager.closeAudioConnection();

        } else {

            if (queue.isQueueListEmpty() && !next && !queue.isLooped()) {

                player.stopTrack();
                manager.closeAudioConnection();

            }

        }

    }

}