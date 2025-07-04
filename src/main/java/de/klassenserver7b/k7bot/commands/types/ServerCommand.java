package de.klassenserver7b.k7bot.commands.types;

import de.klassenserver7b.k7bot.util.HelpCategories;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

/**
 * Interface used to specify and registrate a general Command.
 */

public interface ServerCommand {

    /**
     * @return The Help for the {@link de.klassenserver7b.k7bot.commands.types.ServerCommand
     * ServerCommand}
     */
    String getHelp();

    /**
     * @return The Category for the {@link de.klassenserver7b.k7bot.commands.types.ServerCommand
     * ServerCommand}
     */
    HelpCategories getCategory();

    /**
     * @return The Command Strings for the {@link de.klassenserver7b.k7bot.commands.types.ServerCommand
     * ServerCommand}
     */
    String[] getCommandStrings();

    void performCommand(Member caller, GuildMessageChannel channel, Message message);

    boolean isEnabled();

    void disableCommand();

    void enableCommand();
}
