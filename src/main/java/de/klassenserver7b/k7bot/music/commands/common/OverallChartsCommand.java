package de.klassenserver7b.k7bot.music.commands.common;

import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.music.commands.generic.GenericChartsCommand;
import de.klassenserver7b.k7bot.music.utilities.ChartList;
import de.klassenserver7b.k7bot.util.GenericMessageSendHandler;
import de.klassenserver7b.k7bot.util.HelpCategories;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.util.HashMap;

public class OverallChartsCommand extends GenericChartsCommand implements ServerCommand {

    private boolean isEnabled;

    @Override
    public String getHelp() {
        return "Zeigt die Bot-Charts seit jeher über alle server an";
    }

    @Override
    public String[] getCommandStrings() {
        return new String[]{"charts"};
    }

    @Override
    public HelpCategories getCategory() {
        return HelpCategories.MUSIC;
    }

    @Override
    public void performCommand(Member m, GuildMessageChannel channel, Message message) {

        ChartList chartlist = new ChartList();
        HashMap<String, Long> charts = chartlist.getcharts();

        sendMessage(new GenericMessageSendHandler(channel), charts);

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
