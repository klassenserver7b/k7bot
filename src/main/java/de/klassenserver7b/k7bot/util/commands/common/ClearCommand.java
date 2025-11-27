package de.klassenserver7b.k7bot.util.commands.common;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import de.klassenserver7b.k7bot.util.HelpCategories;
import de.klassenserver7b.k7bot.util.MessageClearUtil;
import de.klassenserver7b.k7bot.util.errorhandler.PermissionError;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class ClearCommand implements ServerCommand {

    private boolean isEnabled;

    @Override
    public String getHelp() {
        return "Löscht die angegebene Anzahl an Nachrichten.\n - z.B. [prefix]clear 50";
    }

    @Override
    public String[] getCommandStrings() {
        return new String[]{"clear"};
    }

    @Override
    public HelpCategories getCategory() {
        return HelpCategories.TOOLS;
    }

    @Override
    public void performCommand(Member m, GuildMessageChannel channel, Message message) {

        if (m.hasPermission(channel, Permission.MESSAGE_MANAGE)) {

            String[] args = message.getContentStripped().split(" ");

            if (args.length == 2) {

                int amount = Integer.parseInt(args[1]);

                MessageClearUtil.onclear(amount, channel);

                GuildMessageChannel system = K7Bot.getInstance().getSysChannelMgr()
                        .getSysChannel(channel.getGuild());

                EmbedBuilder builder = EmbedUtils.getBuilderOf(Color.orange,
                        amount + " messages deleted!\n\n" + "**Channel: **\n" + "#" + channel.getName(),
                        channel.getGuild().getIdLong());
                builder.setFooter("requested by @" + m.getEffectiveName());

                if (system != null) {

                    system.sendMessageEmbeds(builder.build()).queue();

                }

                if (system != null && system.getIdLong() != channel.getIdLong()) {

                    channel.sendMessage(amount + " messages deleted.").complete().delete().queueAfter(3L,
                            TimeUnit.SECONDS);

                }
            }

        } else {

            PermissionError.onPermissionError(m, channel);

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
