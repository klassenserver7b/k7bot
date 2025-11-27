package de.klassenserver7b.k7bot.music.commands.common;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.music.utilities.MusicUtil;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import de.klassenserver7b.k7bot.util.GenericMessageSendHandler;
import de.klassenserver7b.k7bot.util.HelpCategories;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.awt.*;

public class UnLoopCommand implements ServerCommand {

    private boolean isEnabled;

    @Override
    public String getHelp() {
        return "entloopt die aktuelle Queuelist";
    }

    @Override
    public String[] getCommandStrings() {
        return new String[]{"unloop"};
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

        assert vc != null;
        unLoop(vc.getGuild().getIdLong());
        channel.sendMessageEmbeds(EmbedUtils
                        .getBuilderOf(Color.decode("#4d05e8"), "Queue unlooped!", channel.getGuild().getIdLong()).build())
                .queue();

    }

    public static void unLoop(long guildId) {

        K7Bot.getInstance().getPlayerUtil().getController(guildId).getQueue().unLoop();

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
