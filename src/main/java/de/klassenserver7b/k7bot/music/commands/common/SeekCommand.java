/**
 *
 */
package de.klassenserver7b.k7bot.music.commands.common;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.music.lavaplayer.MusicController;
import de.klassenserver7b.k7bot.music.utilities.MusicUtil;
import de.klassenserver7b.k7bot.util.GenericMessageSendHandler;
import de.klassenserver7b.k7bot.util.HelpCategories;
import de.klassenserver7b.k7bot.util.errorhandler.SyntaxError;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

/**
 * @author Klassenserver7b
 */
public class SeekCommand implements ServerCommand {

    private boolean isEnabled;

    @Override
    public String getHelp() {
        return "Spult zur gewählten Position im Song vor.\n - z.B. [prefix]seek [position in seconds]";
    }

    @Override
    public String[] getCommandStrings() {
        return new String[]{"seek"};
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

        if (args.length < 2) {
            SyntaxError.oncmdSyntaxError(new GenericMessageSendHandler(channel), "seek [position in seconds]", m);
            return;
        }

        MusicController controller = K7Bot.getInstance().getPlayerUtil()
                .getController(m.getGuild().getIdLong());
        int pos = Integer.parseInt(args[1]);
        controller.seek(pos * 1000L);

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
