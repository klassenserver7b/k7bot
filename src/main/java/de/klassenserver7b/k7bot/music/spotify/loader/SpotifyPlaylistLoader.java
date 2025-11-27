/**
 *
 */
package de.klassenserver7b.k7bot.music.spotify.loader;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import de.klassenserver7b.k7bot.music.spotify.SpotifyInteractions;

import java.util.function.Function;

/**
 * @author K7
 */
public interface SpotifyPlaylistLoader {

    AudioPlaylist load(SpotifyInteractions spotifyinteract, String playlistId, String selectedVideoId,
                       Function<AudioTrackInfo, AudioTrack> trackFactory);

}
