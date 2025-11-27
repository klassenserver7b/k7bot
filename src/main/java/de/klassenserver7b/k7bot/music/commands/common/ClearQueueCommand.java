package de.klassenserver7b.k7bot.music.commands.common;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.music.lavaplayer.MusicController;
import de.klassenserver7b.k7bot.music.lavaplayer.Queue;
import de.klassenserver7b.k7bot.music.utilities.MusicUtil;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import de.klassenserver7b.k7bot.util.GenericMessageSendHandler;
import de.klassenserver7b.k7bot.util.HelpCategories;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class ClearQueueCommand implements ServerCommand {

    private boolean isEnabled;

    @Override
    public String getHelp() {
        return "Löscht die aktuelle Queuelist.";
    }

    @Override
    public String[] getCommandStrings() {
        return new String[]{"clearqueue", "cq"};
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

        long guildid = channel.getGuild().getIdLong();
        MusicController controller = K7Bot.getInstance().getPlayerUtil().getController(guildid);
        MusicUtil.updateChannel(channel);
        Queue queue = controller.getQueue();
        queue.clearQueue();

        MusicUtil.sendEmbed(guildid, EmbedUtils.getSuccessEmbed("Queue cleared"));
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
