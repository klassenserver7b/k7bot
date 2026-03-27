package de.klassenserver7b.k7bot.audio;

import de.klassenserver7b.k7bot.K7Bot;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.LavalinkPlayer;

import java.util.Optional;

public class GuildAudioManager {

    private final TrackScheduler trackScheduler = new TrackScheduler(this);
    private final long guildId;

    public GuildAudioManager(long guildId) {
        this.guildId = guildId;
    }

    public void stop() {
        this.trackScheduler.queue.clear();


        this.getPlayer().ifPresent(
                (player) -> player.setPaused(false)
                        .setTrack(null)
                        .subscribe()
        );
    }

    public Link getLink() {
        return K7Bot.getInstance().getLavalinkClient().getOrCreateLink(this.guildId);
    }

    public Optional<LavalinkPlayer> getPlayer() {
        return Optional.ofNullable(this.getLink().getCachedPlayer());
    }

    public LavalinkPlayer getOrCreatePlayer() {
        return this.getLink().getPlayer().block();
    }

    public TrackScheduler getTrackScheduler() {
        return trackScheduler;
    }
}
