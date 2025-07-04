package de.klassenserver7b.k7bot.util.customapis;

import de.klassenserver7b.k7bot.util.HelpCategories;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class DisabledAPI implements ServerCommand {

    private final String[] commandstrings;

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public String[] getCommandStrings() {
        return commandstrings;
    }

    @Override
    public HelpCategories getCategory() {
        return HelpCategories.UNKNOWN;
    }

    /**
     * @param commandStrings the commandstrings for which the API is disabled
     */
    public DisabledAPI(String[] commandStrings) {
        this.commandstrings = commandStrings;
    }

    @Override
    public void performCommand(Member m, GuildMessageChannel channel, Message message) {

        channel.sendMessageEmbeds(EmbedUtils.getErrorEmbed(
                        "We are sorry but you try to use an command which is currently disabled!\nPlease contact the Bot-Admin if you think that is an issue.",
                        channel.getGuild().getIdLong()).setTitle("Disabled Command")
                .setFooter("requested by @" + m.getEffectiveName()).build()).queue();

    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void disableCommand() {
        // nothing to do here

    }

    @Override
    public void enableCommand() {
        // nothing to do here
    }

}
