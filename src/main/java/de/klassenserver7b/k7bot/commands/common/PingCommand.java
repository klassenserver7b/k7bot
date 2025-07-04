package de.klassenserver7b.k7bot.commands.common;

import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.util.HelpCategories;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class PingCommand implements ServerCommand {

    private boolean isEnabled;

    @Override
    public String getHelp() {
        return "Gibt den aktuellen Ping des Bots zurück.";
    }

    @Override
    public String[] getCommandStrings() {
        return new String[]{"ping"};
    }

    @Override
    public HelpCategories getCategory() {
        return HelpCategories.GENERIC;
    }

    @Override
    public void performCommand(Member m, GuildMessageChannel channel, Message message) {

        long gatewayPing = channel.getJDA().getGatewayPing();
        long time = channel.getJDA().getRestPing().complete();
        channel.sendMessageFormat("Pong! %dms", time, gatewayPing, "s").queue();
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