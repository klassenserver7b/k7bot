package de.klassenserver7b.k7bot.audio;

import dev.arbjerg.lavalink.client.player.LavalinkPlayer;
import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.protocol.v4.Message;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class TrackScheduler {
    private final GuildAudioManager guildMusicManager;
    public final Deque<Track> queue = new LinkedList<>();

    public TrackScheduler(GuildAudioManager guildMusicManager) {
        this.guildMusicManager = guildMusicManager;
    }

    public void loadTrack(Track track, AudioLoadOption alo) {

        switch (alo) {
            case AudioLoadOption.APPEND -> {
                enqueue(track);
            }
            case AudioLoadOption.NEXT -> {
                enqueueNext(track);
            }
            case AudioLoadOption.REPLACE_QUEUE -> {
                clearQueue();
                enqueue(track);
            }
            case AudioLoadOption.REPLACE -> {
                enqueueNext(track);
                startTrack(queue.poll());
            }
        }

    }

    public void loadPlaylist(List<Track> tracks, AudioLoadOption alo) {

        switch (alo) {
            case AudioLoadOption.APPEND -> {
                enqueuePlaylist(tracks);
            }
            case AudioLoadOption.NEXT -> {
                enqueueNextPlaylist(tracks);
            }
            case AudioLoadOption.REPLACE_QUEUE -> {
                clearQueue();
                enqueuePlaylist(tracks);
            }
            case AudioLoadOption.REPLACE -> {
                enqueuePlaylist(tracks);
                startTrack(queue.poll());
            }
        }

    }

    private void enqueueNext(Track track) {
        LavalinkPlayer player = this.guildMusicManager.getOrCreatePlayer();
        if (player.getTrack() == null) {
            this.startTrack(track);
        } else {
            this.queue.addFirst(track);
        }

    }

    private void enqueue(Track track) {
        LavalinkPlayer player = this.guildMusicManager.getOrCreatePlayer();
        if (player.getTrack() == null) {
            this.startTrack(track);
        } else {
            this.queue.offer(track);
        }
    }

    private void enqueueNextPlaylist(List<Track> tracks) {
        tracks.forEach(this.queue::addFirst);

        LavalinkPlayer player = this.guildMusicManager.getOrCreatePlayer();

        if (player.getTrack() == null) {
            this.startTrack(this.queue.poll());
        }

    }

    private void enqueuePlaylist(List<Track> tracks) {
        this.queue.addAll(tracks);

        LavalinkPlayer player = this.guildMusicManager.getOrCreatePlayer();

        if (player.getTrack() == null) {
            this.startTrack(this.queue.poll());
        }
    }

    private void clearQueue() {
        this.queue.clear();
    }

    public void onTrackStart(Track track) {
        // Your homework: Send a message to the channel somehow, have fun!
        System.out.println("Track started: " + track.getInfo().getTitle());
    }

    public void onTrackEnd(Track lastTrack, Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason endReason) {
        if (endReason.getMayStartNext()) {
            final var nextTrack = this.queue.poll();

            if (nextTrack != null) {
                this.startTrack(nextTrack);
            }
        }
    }

    private void startTrack(Track track) {
        this.guildMusicManager.getLink().createOrUpdatePlayer()
                .setTrack(track)
                .setVolume(35)
                .subscribe();
    }
}
