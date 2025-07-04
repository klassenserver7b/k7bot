package de.klassenserver7b.k7bot.music.commands.common;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import de.klassenserver7b.k7bot.util.HelpCategories;
import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.music.lavaplayer.MusicController;
import de.klassenserver7b.k7bot.music.lavaplayer.Queue;
import de.klassenserver7b.k7bot.music.utilities.MusicUtil;
import de.klassenserver7b.k7bot.music.utilities.SongJson;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class CurrentTrackInfoCommand implements ServerCommand {

    private boolean isEnabled;

    @Override
    public String getHelp() {
        return "Zeigt die Info zum aktuellen Track.";
    }

    @Override
    public String[] getCommandStrings() {
        return new String[]{"nowplaying", "np"};
    }

    @Override
    public HelpCategories getCategory() {
        return HelpCategories.MUSIC;
    }

    @Override
    public void performCommand(Member m, GuildMessageChannel channel, Message message) {

        MusicController controller = K7Bot.getInstance().getPlayerUtil()
                .getController(channel.getGuild().getIdLong());
        AudioPlayer player = controller.getPlayer();

        EmbedBuilder builder = EmbedUtils.getDefault(channel.getGuild().getIdLong());
        MusicUtil.updateChannel(channel);

        if (player.getPlayingTrack() == null) {

            builder.setDescription("Der Bot spielt gerade keinen Song");
            MusicUtil.sendEmbed(channel.getGuild().getIdLong(), builder);

        }

        Queue queue = controller.getQueue();
        SongJson data = queue.getCurrentSongData();

        AudioTrack track = player.getPlayingTrack();
        AudioTrackInfo tinfo = track.getInfo();

        String title = tinfo.title;
        String author = tinfo.author;

        if (data != null && data.isDiscogsValidated()) {
            title = data.getTitle();
            author = data.getAuthorString();
        }

        builder.setFooter("Requested by @" + m.getEffectiveName());
        builder.setTitle("Es läuft: " + title);
        builder.addField("URL: ", tinfo.uri, false);
        builder.addField("Autor: ", author, false);
        long sec = track.getPosition() / 1000L;
        long min = sec / 60L;
        long h = min / 60L;
        min %= 60L;
        sec %= 60L;
        long sekunden = track.getDuration() / 1000L;
        long minuten = sekunden / 60L;
        long stunden = minuten / 60L;
        minuten %= 60L;
        sekunden %= 60L;
        builder.addField("Länge: ",
                tinfo.isStream ? "LiveStream"
                        : (((h > 0L) ? (h + "h ") : "")
                        + ((min > 0L) ? (min + "min ") : "") + sec + "s" + " / "
                        + ((stunden > 0L) ? (stunden + "h ") : "")
                        + ((minuten > 0L) ? (minuten + "min ") : "") + sekunden + "s"),
                true);
        MusicUtil.sendEmbed(channel.getGuild().getIdLong(), builder);
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