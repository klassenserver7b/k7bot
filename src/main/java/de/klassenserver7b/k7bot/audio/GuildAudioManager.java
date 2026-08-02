/* (C)2026 */
package de.klassenserver7b.k7bot.audio;

import java.util.Optional;

import de.klassenserver7b.k7bot.K7Bot;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.LavalinkPlayer;

public class GuildAudioManager {

	private final TrackScheduler trackScheduler = new TrackScheduler(this);
	private final long guildId;
	private long channelId = -1;

	public GuildAudioManager(long guildId) {
		this.guildId = guildId;
	}

	public long getGuildId() {
		return guildId;
	}

	public void setChannelId(long channelId) {
		this.channelId = channelId;
	}

	public long getChannelId() {
		return channelId;
	}

	public void stop() {
		this.trackScheduler.queue.clear();

		this.getPlayer().ifPresent((player) -> player.setPaused(false).setTrack(null).subscribe(_ -> {
		}, e -> System.err.println("Lavalink operation failed: " + e.getMessage())));
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
