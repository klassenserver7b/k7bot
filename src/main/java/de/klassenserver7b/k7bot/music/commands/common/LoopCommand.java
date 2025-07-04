package de.klassenserver7b.k7bot.music.commands.common;

import de.klassenserver7b.k7bot.util.HelpCategories;
import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.music.lavaplayer.MusicController;
import de.klassenserver7b.k7bot.music.utilities.MusicUtil;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import de.klassenserver7b.k7bot.util.GenericMessageSendHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class LoopCommand implements ServerCommand {

    private boolean isEnabled;

    @Override
    public String getHelp() {
        return "loopt die aktuelle Queuelist";
    }

    @Override
    public String[] getCommandStrings() {
        return new String[]{"loop"};
    }

    @Override
    public HelpCategories getCategory() {
        return HelpCategories.MUSIC;
    }

    @Override
    public void performCommand(Member m, GuildMessageChannel channel, Message message) {

        if (MusicUtil.failsConditions(new GenericMessageSendHandler(channel), m)) {
            return;
        }

        AudioChannel vc = MusicUtil.getMembVcConnection(m);

        MusicController controller = K7Bot.getInstance().getPlayerUtil()
                .getController(vc.getGuild().getIdLong());
        if (controller.getPlayer().getPlayingTrack() != null || !(controller.getQueue().getQueuelist().isEmpty())) {

            onLoop(controller);
            channel.sendMessageEmbeds(EmbedUtils
                            .getBuilderOf(Color.decode("#4d05e8"), "Queue looped!", channel.getGuild().getIdLong()).build())
                    .queue();

        } else {

            channel.sendMessageEmbeds(EmbedUtils.getErrorEmbed("There isn't a song/playlist to loop!").build()).queue();

        }

    }

    public static void onLoop(@NotNull MusicController controller) {

        controller.getQueue().loop();

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
