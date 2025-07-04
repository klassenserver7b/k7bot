package de.klassenserver7b.k7bot.music.commands.common;

import de.klassenserver7b.k7bot.util.HelpCategories;
import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.music.lavaplayer.MusicController;
import de.klassenserver7b.k7bot.music.utilities.MusicUtil;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import de.klassenserver7b.k7bot.util.GenericMessageSendHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.awt.*;

public class ShuffleCommand implements ServerCommand {

    private boolean isEnabled;

    @Override
    public String getHelp() {
        return "Spielt die aktuelle Playlist in zufälliger Reihenfolge.";
    }

    @Override
    public String[] getCommandStrings() {
        return new String[]{"shuffle", "random"};
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

        controller.getQueue().shuffle();
        EmbedBuilder builder = EmbedUtils.getBuilderOf(Color.decode("#A537FD"), "playlist shuffled",
                channel.getGuild().getIdLong());

        channel.sendMessageEmbeds(builder.build()).complete().addReaction(Emoji.fromUnicode("U+1F500")).queue();
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