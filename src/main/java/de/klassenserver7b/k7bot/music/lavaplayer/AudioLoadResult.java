package de.klassenserver7b.k7bot.music.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.music.utilities.AudioLoadOption;
import de.klassenserver7b.k7bot.music.utilities.MusicUtil;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.util.List;

public class AudioLoadResult implements AudioLoadResultHandler {

    private final String uri;
    private final MusicController controller;
    private AudioLoadOption loadoption;

    public AudioLoadResult(MusicController controller, String uri, AudioLoadOption loadoption) {
        this.uri = uri;
        this.controller = controller;
        this.loadoption = loadoption;
    }

    @Override
    public void trackLoaded(AudioTrack track) {

        Queue queue = this.controller.getQueue();
        K7Bot.getInstance().getMainLogger().info("Bot AudioLoadResult loaded a single track");
        addTrackToQueue(queue, track);

        EmbedBuilder builder = EmbedUtils.getBuilderOf(Color.decode("#4D05E8"), controller.getGuild().getIdLong())
                .setTitle("1 track added to queue");

        MusicUtil.sendEmbed(this.controller.getGuild().getIdLong(), builder);

    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {

        Queue queue = this.controller.getQueue();

        K7Bot.getInstance().getMainLogger().info("Bot AudioLoadResult loaded a playlist");

        if (playlist.getTracks().isEmpty()) {
            noMatches();
            return;
        }

        List<AudioTrack> playlistTracks = playlist.getTracks();

        if (this.uri.startsWith("ytsearch: ") || this.uri.startsWith("scsearch: ")) {
            K7Bot.getInstance().getMainLogger().debug("url starts with ytsearch:");

            // search liefert Liste an vorgeschlagenen Videos - nur das erste wird zur
            // Queue hinzugefügt
            AudioTrack track = playlistTracks.getFirst();
            addTrackToQueue(queue, track);
            return;
        }

        if (playlist.getTracks().size() == 1) {
            addTrackToQueue(queue, playlistTracks.getFirst());
        } else {
            addPlaylistToQueue(queue, playlist);
        }

        int added = playlist.getTracks().size();


        EmbedBuilder builder = EmbedUtils.getBuilderOf(Color.decode("#4D05E8"), controller.getGuild().getIdLong())
                .setTitle(added + " track" + (added > 1 ? 's' : "") + " added to queue");

        MusicUtil.sendEmbed(this.controller.getGuild().getIdLong(), builder);

    }

    private void addPlaylistToQueue(Queue queue, AudioPlaylist playlist) {

        switch (loadoption) {
            case APPEND -> queue.addPlaylistToQueue(playlist);
            case NEXT -> queue.setNextPlaylist(playlist);
            case REPLACE -> queue.replacePlaylist(playlist);
            case REPLACE_QUEUE -> {
                queue.clearQueue();
                queue.replacePlaylist(playlist);
            }
        }
    }

    private void addTrackToQueue(Queue queue, AudioTrack track) {

        switch (loadoption) {
            case APPEND -> queue.addTrackToQueue(track);
            case NEXT -> queue.setNextTrack(track);
            case REPLACE -> queue.replaceTrack(track);
            case REPLACE_QUEUE -> {
                queue.clearQueue();
                queue.replaceTrack(track);
            }
        }

    }

    @Override
    public void noMatches() {
        K7Bot.getInstance().getMainLogger()
                .info("Bot AudioLoadResult couldn't find a matching audio track (uri={})", uri);
        EmbedBuilder builder = EmbedUtils.getErrorEmbed("Couldn't find the Song you Searched for! :sob:",
                controller.getGuild().getIdLong());
        MusicUtil.sendEmbed(this.controller.getGuild().getIdLong(), builder);
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        K7Bot.getInstance().getMainLogger().info(" - error: {}", exception.getLocalizedMessage());
        EmbedBuilder builder = EmbedUtils.getErrorEmbed(exception.getMessage(), controller.getGuild().getIdLong());
        MusicUtil.sendEmbed(this.controller.getGuild().getIdLong(), builder);
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * @return the controller
     */
    public MusicController getController() {
        return controller;
    }

    /**
     * @return the loadoption
     */
    public AudioLoadOption getLoadoption() {
        return loadoption;
    }

    /**
     * @param loadoption the loadoption to set
     */
    public void setLoadoption(AudioLoadOption loadoption) {
        this.loadoption = loadoption;
    }
}