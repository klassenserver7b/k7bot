/**
 *
 */
package de.klassenserver7b.k7bot.music.spotify;

import com.sedmelluq.discord.lavaplayer.container.wav.WavAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.local.LocalSeekableInputStream;
import com.sedmelluq.discord.lavaplayer.tools.io.SeekableInputStream;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.DelegatedAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;
import de.klassenserver7b.k7bot.music.asms.SpotifyAudioSourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * @author K7
 */
public class SpotifyAudioTrack extends DelegatedAudioTrack {

    private final SpotifyAudioSourceManager sasm;
    private final Logger log;

    /**
     *
     */
    public SpotifyAudioTrack(AudioTrackInfo trackInfo, SpotifyAudioSourceManager sasm) {
        super(trackInfo);
        this.sasm = sasm;
        this.log = LoggerFactory.getLogger(getClass());
    }

    @Override
    public void process(LocalAudioTrackExecutor executor) throws Exception {

        File decr = downloadTrack(super.getIdentifier());
        decr.deleteOnExit();

        log.info("Downloaded spotifytrack {} to {}", super.getIdentifier(), decr.getAbsolutePath());
        try (SeekableInputStream stream = new LocalSeekableInputStream(decr)) {
            new WavAudioTrack(trackInfo, stream).process(executor);
        }

        //noinspection ResultOfMethodCallIgnored
        decr.delete();

    }

    protected File downloadTrack(String identifier) throws IOException, InterruptedException {

        log.info("Tempdir: {}", sasm.getTempdir().getAbsolutePath());

        String audioFileExtension = "wav";

        String pathstr = new File("resources/spotify-dl").getAbsolutePath();

        if (System.getProperty("os.name").contains("windows")) {
            pathstr += ".exe";
        }

        int exitCode = new ProcessBuilder()
                .command(
                        pathstr, "-d", sasm.getTempdir().getAbsolutePath(), "-n", identifier, "--auth", "file", "--credentials-file", "./resources", "-f", audioFileExtension, "https://open.spotify.com/track/" + identifier).redirectError(ProcessBuilder.Redirect.to(new File("logs/spotify_errors.log")))
                .inheritIO().start().waitFor();

        if (exitCode != 0) {
            log.error("Spotify-DL exited with code {}", exitCode);
            return null;
        }

        return new File(sasm.getTempdir().getAbsolutePath() + "/" + String.join(".", identifier, "tmp", audioFileExtension));

    }
}
