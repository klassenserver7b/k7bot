/**
 *
 */
package de.klassenserver7b.k7bot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import org.jetbrains.annotations.NotNull;

/**
 * @author Klassenserver7b
 */
public enum SupportedPlayQueries {

    URL(0, ""),

    YouTubeSearch(1, "ytsearch:"),

    SpotifyUrl(2, ""),

    LocalFile(3, ""),

    SoundCloudSearch(4, "scsearch:"),

    SpotifySearch(5, "spsearch:"),

    JellyfinSearch(6, "jfsearch:");

    private final int id;
    private final String searchSuffix;

    SupportedPlayQueries(int id, String searchSuffix) {
        this.id = id;
        this.searchSuffix = searchSuffix;
    }

    /**
     * The K7Bot id key used to represent the {@link SupportedPlayQueries}.
     *
     * @return The id key used by K7Bot for this play query.
     */

    public int getId() {
        return this.id;
    }

    /**
     * Retrieve the suffix which is used by the {@link AudioPlayerManager} to
     * identify the load method
     * {@link AudioPlayerManager#loadItem(com.sedmelluq.discord.lavaplayer.track.AudioReference, com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler)
     * AudioPlayerManager.loadItem(AudioReference, AudioLoadResultHandler)}
     *
     * @return The suffix as String
     */

    public String getSearchSuffix() {
        return this.searchSuffix;
    }

    /**
     * Static accessor for retrieving a channel type based on its K7Bot id key.
     *
     * @param id The id key of the requested PlayQuery.
     * @return The {@link SupportedPlayQueries} that is referred to by the provided
     * key. If the id key is unknown, {@link #YouTubeSearch} is returned.
     */
    @NotNull
    public static SupportedPlayQueries fromId(int id) {
        for (SupportedPlayQueries type : values()) {
            if (type.id == id)
                return type;
        }
        return YouTubeSearch;
    }

}
