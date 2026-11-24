/* (C)2026 */
package de.klassenserver7b.k7bot.audio;

import java.util.List;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.klassenserver7b.k7bot.manage.LavaLinkManager;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import dev.arbjerg.lavalink.client.AbstractAudioLoadResultHandler;
import dev.arbjerg.lavalink.client.player.*;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

public class AudioLoadResultHandler extends AbstractAudioLoadResultHandler {

	private static final Logger log = LoggerFactory.getLogger(AudioLoadResultHandler.class);

	private final GuildAudioManager guildAudioManager;
	private final String query;
	private final AudioLoadOption audioLoadOption;
	private final long userId;

	public AudioLoadResultHandler(GuildAudioManager guildAudioManager, String query, AudioLoadOption audioLoadOption,
			long userId) {
		this.guildAudioManager = guildAudioManager;
		this.query = query;
		this.audioLoadOption = audioLoadOption;
		this.userId = userId;
	}

	@Override
	public void ontrackLoaded(@NonNull TrackLoaded trackLoaded) {

		final Track track = trackLoaded.getTrack();
		setUserData(track);
		this.guildAudioManager.getTrackScheduler().loadTrack(track, audioLoadOption);

		MessageChannel channel = this.guildAudioManager.getChannel();
		if (channel != null) {
			channel.sendMessageEmbeds(EmbedUtils.getInfoEmbed(
					"Loaded track " + track.getInfo().getTitle() + " " + this.getQueueModificationMessage(), 0, userId)
					.build()).queue();
		}

	}

	@Override
	public void onPlaylistLoaded(@NonNull PlaylistLoaded playlistLoaded) {

		final List<Track> tracks = playlistLoaded.getTracks();

		setUserData(tracks);
		this.guildAudioManager.getTrackScheduler().loadPlaylist(tracks, audioLoadOption);

		MessageChannel channel = this.guildAudioManager.getChannel();
		if (channel != null) {
			channel.sendMessageEmbeds(EmbedUtils.getInfoEmbed(
					"Loaded playlist " + playlistLoaded.getInfo().getName() + " " + this.getQueueModificationMessage(),
					0, userId).build()).queue();
		}
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

		MessageChannel channel = this.guildAudioManager.getChannel();
		if (channel != null) {
			channel.sendMessageEmbeds(EmbedUtils.getInfoEmbed(
					"Loaded track " + track.getInfo().getTitle() + " " + this.getQueueModificationMessage(), 0, userId)
					.build()).queue();
		}
	}

	@Override
	public void noMatches() {
		MessageChannel channel = this.guildAudioManager.getChannel();
		if (channel != null) {
			channel.sendMessageEmbeds(
					EmbedUtils.getInfoEmbed("No matches found for query: " + this.query, 0, userId).build()).queue();
		}
		log.warn("No matches found for audio load request by user {}", userId);
	}

	@Override
	public void loadFailed(@NonNull LoadFailed loadFailed) {
		MessageChannel channel = this.guildAudioManager.getChannel();
		if (channel != null) {
			channel.sendMessageEmbeds(EmbedUtils
					.getErrorEmbed("Failed while retrieving matches for query: " + this.query, 0, userId).build())
					.queue();
		}

		log.error("Failed to load audio for user {}: {}", userId, loadFailed.getException().getMessage());
	}

	private void setUserData(Track track) {
		var userData = new LavaLinkManager.UserData(userId);
		track.setUserData(userData);
	}

	private void setUserData(List<Track> tracks) {
		tracks.forEach(this::setUserData);
	}

	private String getQueueModificationMessage() {
		return switch (audioLoadOption) {
			case APPEND -> "and appended it to queue";
			case NEXT -> "and set it as next";
			case REPLACE -> "and replaced current track";
			case REPLACE_QUEUE -> "and replaced queue";
		};
	}
}
