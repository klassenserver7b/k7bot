package de.klassenserver7b.k7bot.music.commands.common;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.klassenserver7b.k7bot.HelpCategories;
import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.music.lavaplayer.MusicController;
import de.klassenserver7b.k7bot.music.lavaplayer.Queue;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class QueuelistCommand implements ServerCommand {

    private boolean isEnabled;

    @Override
    public String getHelp() {
        return "Zeigt die aktuelle Queuelist an.";
    }

    @Override
    public String[] getCommandStrings() {
        return new String[]{"queuelist", "ql"};
    }

    @Override
    public HelpCategories getCategory() {
        return HelpCategories.MUSIC;
    }

    @Override
    public void performCommand(Member m, GuildMessageChannel channel, Message message) {

        MusicController contr = K7Bot.getInstance().getPlayerUtil()
                .getController(channel.getGuild().getIdLong());
        Queue queue = contr.getQueue();
        List<AudioTrack> queuelist = queue.getQueuelist();

        if (!queuelist.isEmpty()) {

            StringBuilder strbuild = new StringBuilder();

            for (AudioTrack t : queuelist) {
                String content = "- " + t.getInfo().author + " - " + t.getInfo().title.replaceAll("\\|", "-") + "\n";

                if (!((strbuild.toString().length() + content.length()) >= 4000)) {
                    strbuild.append(content);
                } else {

                    strbuild.append("... \n...");
                    break;

                }

            }

            EmbedBuilder builder = EmbedUtils.getBuilderOf(Color.decode("#14cdc8"), strbuild,
                    channel.getGuild().getIdLong());

            builder.setFooter("Requested by @" + m.getEffectiveName());
            builder.setTitle("Queue for Guild: " + channel.getGuild().getName() + " (" + queuelist.size() + " entrys)");
            builder.setThumbnail("https://openclipart.org/image/1200px/211805");

            channel.sendMessageEmbeds(builder.build()).queue();

        } else {

            EmbedBuilder build = EmbedUtils.getErrorEmbed("The Queue for this guild is empty!",
                    channel.getGuild().getIdLong());

            channel.sendMessageEmbeds(build.build()).complete().delete().queueAfter(15, TimeUnit.SECONDS);

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
