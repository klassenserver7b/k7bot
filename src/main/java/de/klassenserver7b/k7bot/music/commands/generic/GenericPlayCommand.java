/**
 *
 */
package de.klassenserver7b.k7bot.music.commands.generic;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import de.klassenserver7b.k7bot.util.HelpCategories;
import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.music.SupportedPlayQueries;
import de.klassenserver7b.k7bot.music.asms.ExtendedLocalAudioSourceManager;
import de.klassenserver7b.k7bot.music.asms.SpotifyAudioSourceManager;
import de.klassenserver7b.k7bot.music.lavaplayer.AudioLoadResult;
import de.klassenserver7b.k7bot.music.lavaplayer.MusicController;
import de.klassenserver7b.k7bot.music.utilities.AudioLoadOption;
import de.klassenserver7b.k7bot.music.utilities.MusicUtil;
import de.klassenserver7b.k7bot.util.GenericMessageSendHandler;
import de.klassenserver7b.k7bot.util.errorhandler.SyntaxError;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.managers.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author K7
 */
public abstract class GenericPlayCommand implements ServerCommand {

    private final Logger log;
    private final AudioPlayerManager apm;
    private final String SUPPORTED_AUDIO_FORMATS = "(mp4|mp3|wav|ogg|m4a)";

    /**
     *
     */
    public GenericPlayCommand() {
        this.log = LoggerFactory.getLogger(this.getClass());

        apm = new DefaultAudioPlayerManager();
        apm.registerSourceManager(new SpotifyAudioSourceManager());
        apm.registerSourceManager(new YoutubeAudioSourceManager());
        apm.registerSourceManager(new ExtendedLocalAudioSourceManager());
        AudioSourceManagers.registerRemoteSources(apm);
    }

    public void performSlashCommand(SlashCommandInteraction event) {

        InteractionHook hook = event.deferReply(true).complete();
        Member m = event.getMember();

        if (event.getOptions().isEmpty()) {
            assert m != null;
            SyntaxError.oncmdSyntaxError(new GenericMessageSendHandler(hook), getHelp(), m);
            return;
        }

        assert m != null;
        AudioChannel vc = MusicUtil.getMembVcConnection(m);

        if (membFailsInternalChecks(m, vc, new GenericMessageSendHandler(hook))) {
            return;
        }

        assert vc != null;

        MusicUtil.updateChannel(hook);
        MusicController controller = K7Bot.getInstance().getPlayerUtil()
                .getController(vc.getGuild().getIdLong());

        playQueriedItem(SupportedPlayQueries.fromId(Objects.requireNonNull(event.getOption("target")).getAsInt()), vc,
                Objects.requireNonNull(event.getOption("url")).getAsString(), controller);

        hook.sendMessage("Successfully Loaded").queue();

    }

    @Override
    public void performCommand(Member m, GuildMessageChannel channel, Message message) {

        String[] args = message.getContentDisplay().split(" ");

        if (args.length <= 1 && message.getAttachments().isEmpty()) {
            SyntaxError.oncmdSyntaxError(new GenericMessageSendHandler(channel), getHelp(), m);
            return;
        }

        AudioChannel vc = MusicUtil.getMembVcConnection(m);

        if (membFailsInternalChecks(m, vc, new GenericMessageSendHandler(channel))) {
            return;
        }
        assert vc != null;

        MusicUtil.updateChannel(channel);
        MusicController controller = K7Bot.getInstance().getPlayerUtil()
                .getController(vc.getGuild().getIdLong());

        StringBuilder strBuilder = new StringBuilder();

        if (!message.getAttachments().isEmpty()) {
            int status = loadAttachments(message.getAttachments(), controller);

            if (status != 0) {
                channel.sendMessage(
                                "Invalid file attached to this message! - allowed are ." + SUPPORTED_AUDIO_FORMATS + " files")
                        .complete().delete().queueAfter(10L, TimeUnit.SECONDS);
            }

            return;
        }

        for (int i = 1; i < args.length; i++) {
            strBuilder.append(args[i]);
            strBuilder.append(" ");
        }

        String url = strBuilder.toString().trim();

        loadURL(url, controller, vc.getName());

    }

    /**
     * @param querytype  the type of the query
     * @param channel    the audio channel
     * @param query      the query
     * @param controller the music controller
     */
    private void playQueriedItem(SupportedPlayQueries querytype, AudioChannel channel, String query,
                                 MusicController controller) {

        String suffix = querytype.getSearchSuffix();
        String url = suffix + " " + query;
        url = url.trim();

        loadURL(url, controller, channel.getName());

    }

    protected int loadURL(String url, MusicController controller, String vcname) {
        url = formatQuerry(url);

        log.info("Bot startet searching a track: no current track -> new Track(channelName = {}, url = {})", vcname, url);

        try {
            apm.loadItem(url, generateAudioLoadResult(controller, url));
            return 0;
        } catch (FriendlyException e) {
            log.error(e.getMessage(), e);
            return 1;
        }
    }

    protected int loadAttachments(List<Attachment> attachments, MusicController controller) {

        boolean err = false;
        for (int i = 0; i < attachments.size(); i++) {

            try (Attachment song = attachments.get(i)) {
                String fileExtension = song.getFileExtension();
                if (fileExtension == null || !fileExtension.matches(SUPPORTED_AUDIO_FORMATS)) {
                    err = true;
                    continue;
                }

                AudioLoadResult alr = generateAudioLoadResult(controller, song.getProxyUrl());

                if (i != 0) {
                    alr.setLoadoption(AudioLoadOption.APPEND);
                }

                apm.loadItem(song.getProxyUrl(), alr);
            }
        }

        if (err) {
            return 1;
        }
        return 0;

    }

    protected boolean membFailsInternalChecks(Member m, AudioChannel vc, GenericMessageSendHandler sendHandler) {

        if (MusicUtil.membFailsDefaultConditions(sendHandler, m)) {
            return true;
        }

        AudioManager manager = vc.getGuild().getAudioManager();

        if (!manager.isConnected()) {
            manager.openAudioConnection(vc);
        }

        return false;
    }

    private String formatQuerry(String q) {

        String url = q.strip();

        if (url.matches("ytsearch: .*|scsearch: .*|spsearch: .*|jfsearch: .*|http(s)?://.*|/run/media/data/.*")) {
            return url;
        }
        return "ytsearch: " + url;
    }

    @Override
    public HelpCategories getCategory() {
        return HelpCategories.MUSIC;
    }

    @Override
    public String[] getCommandStrings() {
        return null;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void disableCommand() {
        // Nothing to do here
    }

    @Override
    public void enableCommand() {
        // Nothing to do here
    }

    @Override
    abstract public String getHelp();

    abstract protected AudioLoadResult generateAudioLoadResult(MusicController controller, String url);

    abstract protected GenericPlayCommand getChildClass();

}
