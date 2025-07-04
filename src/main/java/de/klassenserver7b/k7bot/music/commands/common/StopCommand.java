package de.klassenserver7b.k7bot.music.commands.common;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import de.klassenserver7b.k7bot.util.HelpCategories;
import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.music.lavaplayer.MusicController;
import de.klassenserver7b.k7bot.music.utilities.MusicUtil;
import de.klassenserver7b.k7bot.util.GenericMessageSendHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.Objects;

public class StopCommand implements ServerCommand {

    private boolean isEnabled;

    @Override
    public String getHelp() {
        return "Stoppt den aktuellen Track und der Bot verlässt den VoiceChannel.";
    }

    @Override
    public String[] getCommandStrings() {
        return new String[]{"stop"};
    }

    @Override
    public HelpCategories getCategory() {
        return HelpCategories.MUSIC;
    }

    @Override
    public void performCommand(Member m, GuildMessageChannel channel, Message message) {

        if (MusicUtil.membFailsDefaultConditions(new GenericMessageSendHandler(channel), m)
                && !channel.getGuild().getAudioManager().isConnected()) {
            return;
        }

        AudioChannel vc = MusicUtil.getMembVcConnection(m);

        MusicController controller = K7Bot.getInstance().getPlayerUtil()
                .getController(Objects.requireNonNull(vc).getGuild().getIdLong());
        AudioManager manager = vc.getGuild().getAudioManager();
        AudioPlayer player = controller.getPlayer();

        MusicUtil.updateChannel(channel);
        player.stopTrack();
        manager.closeAudioConnection();

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
