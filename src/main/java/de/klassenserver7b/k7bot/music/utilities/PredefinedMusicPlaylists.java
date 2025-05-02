/**
 *
 */
package de.klassenserver7b.k7bot.music.utilities;

import org.jetbrains.annotations.NotNull;

/**
 * @author Klassenserver7b
 */
public enum PredefinedMusicPlaylists {

    YTCharts(0, "https://music.youtube.com/playlist?list=PL0sHkSjKd2rpxgOMD-vlUlIDqvQ5ChYJh"),

    HARDSTYLE(1, "https://open.spotify.com/playlist/7onTLV8yHFSBPLBE7QfAMd"),

    ServerMix(2, "https://open.spotify.com/playlist/3QyqgTHQLvEVKEqRbSCLbV"),

    Nightcore(3, "https://www.youtube.com/playlist?list=PLAzC6gV-_NVM6uLLprtddVl4My07_CHtL");

    private final int id;
    private final String url;

    PredefinedMusicPlaylists(int id, String url) {

        this.id = id;
        this.url = url;

    }

    /**
     * The K7Bot id key used to represent the {@link PredefinedMusicPlaylists}.
     *
     * @return The id as int
     */
    public int getId() {
        return this.id;
    }

    /**
     * Used to retrieve the url of the playlist
     *
     * @return The Url as String
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Static accessor for retrieving a PredefinedMusicPlaylist based on its K7Bot
     * id key.
     *
     * @param id The id key of the requested MusicPlaylist.
     * @return The {@link PredefinedMusicPlaylists} that is referred to by the
     * provided key. If the id key is unknown, {@link #YTCharts} is
     * returned.
     */
    @NotNull
    public static PredefinedMusicPlaylists fromId(int id) {
        for (PredefinedMusicPlaylists type : values()) {
            if (type.id == id)
                return type;
        }
        return YTCharts;
    }

}
