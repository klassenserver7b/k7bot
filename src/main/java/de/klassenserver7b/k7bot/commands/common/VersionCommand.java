package de.klassenserver7b.k7bot.commands.common;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.util.HelpCategories;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class VersionCommand implements ServerCommand {
    /**
     * @return The Help for the {@link ServerCommand
     * ServerCommand}
     */
    @Override
    public String getHelp() {
        return "Shows the current version of the bot.";
    }

    /**
     * @return The Category for the {@link ServerCommand
     * ServerCommand}
     */
    @Override
    public HelpCategories getCategory() {
        return HelpCategories.GENERIC;
    }

    /**
     * @return The Command Strings for the {@link ServerCommand
     * ServerCommand}
     */
    @Override
    public String[] getCommandStrings() {
        return new String[]{"version"};
    }

    /**
     * @param caller
     * @param channel
     * @param message
     */
    @Override
    public void performCommand(Member caller, GuildMessageChannel channel, Message message) {
        channel.sendMessageFormat("K7bot version %s", K7Bot.class.getPackage().getImplementationVersion()).queue();
    }

    /**
     * @return
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     *
     */
    @Override
    public void disableCommand() {
        // No action needed, this command is always enabled
    }

    /**
     *
     */
    @Override
    public void enableCommand() {
        // No action needed, this command is always enabled
    }
}
