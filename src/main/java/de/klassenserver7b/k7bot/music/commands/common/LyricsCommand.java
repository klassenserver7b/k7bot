package de.klassenserver7b.k7bot.music.commands.common;

import com.jagrosh.jlyrics.Lyrics;
import com.jagrosh.jlyrics.LyricsClient;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import de.klassenserver7b.k7bot.util.HelpCategories;
import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.music.lavaplayer.MusicController;
import de.klassenserver7b.k7bot.music.lavaplayer.Queue;
import de.klassenserver7b.k7bot.music.utilities.MusicUtil;
import de.klassenserver7b.k7bot.music.utilities.SongJson;
import de.klassenserver7b.k7bot.music.utilities.gla.GLACustomSongSearch;
import de.klassenserver7b.k7bot.music.utilities.gla.GLACustomSongSearch.Hit;
import de.klassenserver7b.k7bot.music.utilities.gla.GLAWrapper;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import de.klassenserver7b.k7bot.util.GenericMessageSendHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class LyricsCommand implements ServerCommand {

    private boolean isEnabled;

    private final Logger log;

    @Override
    public String getHelp() {
        return "Sendet die Lyrics des aktuell gespielten Songs in den aktuellen channel.";
    }

    @Override
    public String[] getCommandStrings() {
        return new String[]{"lyrics"};
    }

    @Override
    public HelpCategories getCategory() {
        return HelpCategories.MUSIC;
    }

    public LyricsCommand() {
        log = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    public void performCommand(Member m, GuildMessageChannel channel, Message message) {

        if (MusicUtil.failsConditions(new GenericMessageSendHandler(channel), m)
                || !MusicUtil.isPlayingSong(channel, m)) {
            return;
        }

        AudioChannel vc = MusicUtil.getMembVcConnection(m);

        MusicController controller = K7Bot.getInstance().getPlayerUtil()
                .getController(vc.getGuild().getIdLong());
        Queue queue = controller.getQueue();
        SongJson data = queue.getCurrentSongData();

        String query;
        if (data != null) {
            query = data.getTitle() + " " + data.getAuthorString();
        } else {
            AudioTrackInfo info = queue.getController().getPlayer().getPlayingTrack().getInfo();
            query = info.title + " " + info.author;
        }

        log.info("Searching Lyrics Querry: {}", query);

        try {

            GLACustomSongSearch genius = getGeniusLyrics(query);

            if (genius != null) {
                sendGeniusEmbed(genius, channel, m);
                return;
            }

            LyricsClient lapi = K7Bot.getInstance().getLyricsAPI();

            Lyrics lyrics = lapi.getLyrics(query).get();

            if (lyrics != null) {
                sendJLyricsEmbed(lyrics, channel, m);
                return;
            }

            sendErrorEmbed(channel);

        } catch (IOException | InterruptedException | ExecutionException e) {
            sendErrorEmbed(channel);
        }

    }

    private void sendErrorEmbed(GuildMessageChannel c) {

        EmbedBuilder build = EmbedUtils.getErrorEmbed("Couldn't find the song you searched for",
                c.getGuild().getIdLong());

        c.sendMessageEmbeds(build.build()).complete().delete().queueAfter(15, TimeUnit.SECONDS);

    }

    private void sendGeniusEmbed(GLACustomSongSearch data, GuildMessageChannel c, Member m) {

        c.sendTyping().queue();

        if (data.getHits().isEmpty()) {
            sendErrorEmbed(c);
            return;
        }

        Hit hit = data.getHits().getFirst();

        EmbedBuilder builder = EmbedUtils.getBuilderOf(Color.decode("#14cdc8"), hit.fetchLyrics(),
                c.getGuild().getIdLong());

        builder.setThumbnail(hit.getThumbnailUrl());
        builder.setTitle("Lyrics of " + hit.getTitle() + " from " + hit.getArtist().getName());
        builder.setFooter("Requested by @" + m.getEffectiveName() + " | Lyrics by Genius");

        c.sendMessageEmbeds(builder.build()).queue();

    }

    private void sendJLyricsEmbed(Lyrics data, GuildMessageChannel c, Member m) {

        c.sendTyping().queue();

        EmbedBuilder builder = EmbedUtils.getBuilderOf(Color.decode("#14cdc8"), data.getContent(),
                c.getGuild().getIdLong());

        builder.setTitle("Lyrics of " + data.getTitle() + " from " + data.getAuthor());
        builder.setFooter("Requested by @" + m.getEffectiveName() + " | Lyrics by " + data.getSource());

        builder.setDescription(data.getContent());

        c.sendMessageEmbeds(builder.build()).queue();

    }

    private GLACustomSongSearch getGeniusLyrics(String query) throws IOException {

        GLAWrapper lapi = K7Bot.getInstance().getLyricsAPIold();

        return lapi.search(query);

    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public void disableCommand() {
        isEnabled = false;
    }

    @Override
    public void enableCommand() {
        isEnabled = true;
    }

}
