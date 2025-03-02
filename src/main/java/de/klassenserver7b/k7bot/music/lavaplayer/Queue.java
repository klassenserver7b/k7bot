package de.klassenserver7b.k7bot.music.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.music.utilities.SongDataUtils;
import de.klassenserver7b.k7bot.music.utilities.SongJson;
import de.klassenserver7b.k7bot.sql.LiteSQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Queue {
    private boolean islooped = false;
    private List<AudioTrack> looplist;
    private List<AudioTrack> queuelist;
    private MusicController controller;
    private SongJson currentSong;
    private final SongDataUtils songutils;
    private final Logger log;

    public Queue(MusicController controller) {
        setController(controller);
        setQueuelist(new ArrayList<>());
        this.songutils = new SongDataUtils();
        this.currentSong = null;
        this.log = LoggerFactory.getLogger(this.getClass());
    }

    public boolean isQueueListEmpty() {
        return this.queuelist.isEmpty();
    }

    public boolean skip(int amount) {
        if (amount < 1) {
            return false;
        }

        if (amount > this.queuelist.size()) {
            clearQueue();
            return next(this.controller.getPlayer().getPlayingTrack());
        }

        queuelist.subList(0, amount).clear();

        return true;
    }

    public boolean next(AudioTrack currentTrack) {

        TrackScheduler.next = true;

        AudioPlayer player = this.controller.getPlayer();

        AudioTrack track;

        if (this.queuelist.isEmpty()) {

            if (!this.islooped) {
                return false;
            }

            if (this.looplist.isEmpty()) {

                track = currentTrack.makeClone();
                logNewTrack(track);
                player.playTrack(track);
                return true;

            } else {
                this.queuelist = this.looplist;
            }

        }

        track = this.queuelist.removeFirst();
        logNewTrack(track);
        player.playTrack(track);

        return true;
    }

    public void logNewTrack(AudioTrack track) {

        try {

            String datetimestring = LocalDateTime.now().format(DateTimeFormatter.ofPattern("uuuuMMddHHmmss"));
            long datetime = Long.parseLong(datetimestring);

            if (track instanceof YoutubeAudioTrack) {
                SongJson data = songutils.parseYtTitle(track.getInfo().title, track.getInfo().author);
                this.currentSong = data;
                if (this.currentSong != null) {
                    LiteSQL.onUpdate(
                            "INSERT INTO musiclogs(songname, songauthor, guildId, timestamp) VALUES(?, ?, ?, ?);",
                            data.getTitle(), data.getAuthorString(), controller.getGuild().getIdLong(), datetime);
                } else {
                    LiteSQL.onUpdate(
                            "INSERT INTO musiclogs(songname, songauthor, guildId, timestamp) VALUES(?, ?, ?, ?);",
                            track.getInfo().title, track.getInfo().author, controller.getGuild().getIdLong(), datetime);
                }
            } else {

                this.currentSong = null;

                LiteSQL.onUpdate("INSERT INTO musiclogs(songname, songauthor, guildId, timestamp) VALUES(?, ?, ?, ?);",
                        track.getInfo().title, track.getInfo().author, controller.getGuild().getIdLong(), datetime);

            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    public void replaceTrack(AudioTrack track) {

        TrackScheduler.next = true;
        queuelist.addFirst(track);
        next(track);
        TrackScheduler.next = false;

    }

    public void replacePlaylist(AudioPlaylist playlist) {

        TrackScheduler.next = true;
        List<AudioTrack> pl = playlist.getTracks();
        pl.addAll(queuelist);

        queuelist = pl;

        next(playlist.getTracks().getFirst());
        TrackScheduler.next = false;

    }

    public void addPlaylistToQueue(AudioPlaylist playlist) {

        queuelist.addAll(playlist.getTracks());
        sheduleNextTrack(queuelist.getFirst());

    }

    public void addTrackToQueue(AudioTrack track) {

        this.queuelist.add(track);
        sheduleNextTrack(track);
    }

    public void setNextPlaylist(AudioPlaylist playlist) {

        queuelist.addAll(0, playlist.getTracks());
        sheduleNextTrack(queuelist.getFirst());

    }

    public void setNextTrack(AudioTrack track) {
        this.queuelist.addFirst(track);
        sheduleNextTrack(track);
    }

    public void sheduleNextTrack(AudioTrack track) {
        if (this.controller.getPlayer().getPlayingTrack() == null) {
            K7Bot.getInstance().getMainLogger()
                    .debug("Queue - setNextTrack: playing track = null -> next({}})", queuelist.getFirst().getInfo().title);
            next(track);
        }
    }

    public void loop() {
        this.islooped = true;
        this.looplist = this.queuelist;

        if (this.queuelist.size() > 1) {
            this.looplist.add(this.controller.getPlayer().getPlayingTrack());
        }

    }

    public void unLoop() {
        this.islooped = false;

        if (this.looplist != null && !this.looplist.isEmpty()) {
            this.looplist.clear();
        }
    }

    public void clearQueue() {

        if (this.queuelist != null) {

            this.queuelist.clear();

        }
    }

    public MusicController getController() {
        return this.controller;
    }

    public void setController(MusicController controller) {
        this.controller = controller;
    }

    public List<AudioTrack> getQueuelist() {
        return this.queuelist;
    }

    public void setQueuelist(List<AudioTrack> queuelist) {
        this.queuelist = queuelist;
    }

    @SuppressWarnings("unused")
    public void setLooplist(List<AudioTrack> looplist) {
        this.looplist = looplist;
    }

    public void shuffle() {
        Collections.shuffle(this.queuelist);
    }

    public boolean isLooped() {
        return islooped;
    }

    @SuppressWarnings("unused")
    public SongDataUtils getSongDataUtils() {
        return songutils;
    }

    public SongJson getCurrentSongData() {
        return this.currentSong;
    }
}
