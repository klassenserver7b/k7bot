package de.klassenserver7b.k7bot.music.commands.common;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.klassenserver7b.k7bot.util.HelpCategories;
import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.music.lavaplayer.MusicController;
import de.klassenserver7b.k7bot.music.lavaplayer.Queue;
import de.klassenserver7b.k7bot.music.utilities.MusicUtil;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import de.klassenserver7b.k7bot.util.GenericMessageSendHandler;
import de.klassenserver7b.k7bot.util.errorhandler.SyntaxError;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.util.concurrent.TimeUnit;

public class SkipCommand implements ServerCommand {

    private boolean isEnabled;

    @Override
    public String getHelp() {
        return "Überspringt den aktuellen Song.";
    }

    @Override
    public String[] getCommandStrings() {
        return new String[]{"skip", "s"};
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

        String[] args = message.getContentDisplay().split(" ");
        long guildid = channel.getGuild().getIdLong();
        MusicController controller = K7Bot.getInstance().getPlayerUtil().getController(guildid);
        Queue queue = controller.getQueue();
        AudioTrack lastTrack = controller.getPlayer().getPlayingTrack();

        if (args.length == 1) {
            queue.next(lastTrack);
        } else {

            int amount;

            try {

                amount = Integer.parseInt(args[1]);

            } catch (NumberFormatException e) {
                SyntaxError.oncmdSyntaxError(new GenericMessageSendHandler(channel), "skip [int]", m);
                return;
            }

            queue.skip(amount);
            queue.next(lastTrack);

            EmbedBuilder builder = EmbedUtils.getDefault(channel.getGuild().getIdLong());
            builder.setFooter("Requested by @" + m.getEffectiveName());
            builder.setTitle(Integer.parseInt(args[1]) + " tracks skipped");

            channel.sendMessageEmbeds(builder.build()).complete().delete().queueAfter(10L, TimeUnit.SECONDS);


        }
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