package de.klassenserver7b.k7bot.audio;

import de.klassenserver7b.k7bot.manage.LavaLinkManager;
import dev.arbjerg.lavalink.client.AbstractAudioLoadResultHandler;
import dev.arbjerg.lavalink.client.player.*;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class AudioLoadResultHandler extends AbstractAudioLoadResultHandler {

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

        final Track track = searchResult.getTracks().getFirst();
        setUserData(track);
        this.guildAudioManager.getTrackScheduler().loadTrack(track, audioLoadOption);

    }

    @Override
    public void noMatches() {
        //TODO
    }

    @Override
    public void loadFailed(@NonNull LoadFailed loadFailed) {
        //TODO
    }

    private void setUserData(Track track) {
        var userData = new LavaLinkManager.UserData(userId);
        track.setUserData(userData);
    }

    private void setUserData(List<Track> tracks) {
        tracks.forEach(track -> setUserData(track));
    }
}
