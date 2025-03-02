package de.klassenserver7b.k7bot.music.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.sql.LiteSQL;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MusicController {
    private final Guild guild;
    private final AudioPlayer player;
    private final Queue queue;
    private final Logger log;

    private interface TrackOperation {
        void execute(AudioTrack track);
    }

    public MusicController(Guild guild) {
        this.log = LoggerFactory.getLogger(this.getClass());
        this.guild = guild;
        this.player = K7Bot.getInstance().getAudioPlayerManager().createPlayer();
        this.queue = new Queue(this);

        this.guild.getAudioManager().setSendingHandler(new AudioPlayerSendHandler(this.player));
        this.player.addListener(new TrackScheduler());
        loadVolume();
    }

    private void loadVolume() {

        try (ResultSet set = LiteSQL.onQuery("SELECT volume FROM musicutil WHERE guildId = ?", guild.getIdLong())) {
            if (set.next()) {
                try {
                    int volume = set.getInt("volume");
                    this.player.setVolume(volume);
                } catch (SQLException e) {
                    log.error(e.getMessage(), e);
                }
            } else {
                LiteSQL.onUpdate("INSERT OR REPLACE INTO musicutil(guildId) VALUES(?)", guild.getIdLong());
                this.player.setVolume(10);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }

    }

    public void forward(int duration) {
        forPlayingTrack(track -> track.setPosition(track.getPosition() + duration));
    }

    public void back(int duration) {
        forPlayingTrack(track -> track.setPosition(Math.max(0, track.getPosition() - duration)));
    }

    public void seek(long position) {
        forPlayingTrack(track -> track.setPosition(position));
    }

    private void forPlayingTrack(TrackOperation operation) {
        AudioTrack track = player.getPlayingTrack();

        if (track != null) {
            operation.execute(track);
        }
    }

    public Guild getGuild() {
        return this.guild;
    }

    public AudioPlayer getPlayer() {
        return this.player;
    }

    public Queue getQueue() {
        return this.queue;
    }
}