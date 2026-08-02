/* (C)2026 */
package de.klassenserver7b.k7bot.audio;

import java.util.List;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.klassenserver7b.k7bot.manage.LavaLinkManager;
import dev.arbjerg.lavalink.client.AbstractAudioLoadResultHandler;
import dev.arbjerg.lavalink.client.player.*;

public class AudioLoadResultHandler extends AbstractAudioLoadResultHandler {

	private static final Logger log = LoggerFactory.getLogger(AudioLoadResultHandler.class);

	private final GuildAudioManager guildAudioManager;
	private final long userId;
	private final AudioLoadOption audioLoadOption;

	public AudioLoadResultHandler(GuildAudioManager guildAudioManager, AudioLoadOption audioLoadOption, long userId) {
		this.guildAudioManager = guildAudioManager;
		this.audioLoadOption = audioLoadOption;
		this.userId = userId;
	}

	@Override
	public void ontrackLoaded(@NonNull TrackLoaded trackLoaded) {

		final Track track = trackLoaded.getTrack();

		setUserData(track);
		this.guildAudioManager.getTrackScheduler().loadTrack(track, audioLoadOption);

	}

	@Override
	public void onPlaylistLoaded(@NonNull PlaylistLoaded playlistLoaded) {

		final List<Track> tracks = playlistLoaded.getTracks();

		setUserData(tracks);
		this.guildAudioManager.getTrackScheduler().loadPlaylist(tracks, audioLoadOption);

	}

	@Override
	public void onSearchResultLoaded(@NonNull SearchResult searchResult) {
		if (searchResult.getTracks().isEmpty()) {
			noMatches();
			return;
		}

		final Track track = searchResult.getTracks().getFirst();
		setUserData(track);
		this.guildAudioManager.getTrackScheduler().loadTrack(track, audioLoadOption);

	}

	@Override
	public void noMatches() {
		log.warn("No matches found for audio load request by user {}", userId);
	}

	@Override
	public void loadFailed(@NonNull LoadFailed loadFailed) {
		log.error("Failed to load audio for user {}: {}", userId, loadFailed.getException().getMessage());
	}

	private void setUserData(Track track) {
		var userData = new LavaLinkManager.UserData(userId);
		track.setUserData(userData);
	}

	private void setUserData(List<Track> tracks) {
		tracks.forEach(this::setUserData);
	}
}
