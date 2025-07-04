/**
 *
 */
package de.klassenserver7b.k7bot.music.spotify.loader;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import de.klassenserver7b.k7bot.music.spotify.SpotifyInteractions;

import java.util.function.Function;

/**
 * @author K7
 */
public interface SpotifyTrackLoader {

    AudioTrack load(SpotifyInteractions spotifyinteract, String songid,
                    Function<AudioTrackInfo, AudioTrack> trackFactory);

}
