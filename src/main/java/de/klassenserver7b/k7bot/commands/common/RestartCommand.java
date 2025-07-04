package de.klassenserver7b.k7bot.commands.common;

import de.klassenserver7b.k7bot.util.HelpCategories;
import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.util.GenericMessageSendHandler;
import de.klassenserver7b.k7bot.util.RestartUtil;
import de.klassenserver7b.k7bot.util.errorhandler.PermissionError;
import de.klassenserver7b.k7bot.util.errorhandler.SyntaxError;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.slf4j.Logger;

public class RestartCommand implements ServerCommand {

    private boolean isEnabled;

    @Override
    public String getHelp() {
        return "Startet den Bot neu.\n - kann nur vom Bot Owner ausgeführt werden!";
    }

    @Override
    public String[] getCommandStrings() {
        return new String[]{"restart"};
    }

    @Override
    public HelpCategories getCategory() {
        return HelpCategories.TOOLS;
    }

    @Override
    public void performCommand(Member caller, GuildMessageChannel channel, Message message) {

        if (caller.getIdLong() == K7Bot.getInstance().getOwnerId()) {
            Logger log = K7Bot.getInstance().getMainLogger();
            String[] args = message.getContentDisplay().split(" ");

            if (args.length > 1) {

                try {

                    RestartUtil.restart();

                    K7Bot.getInstance().getShardManager().restart(Integer.parseInt(args[1]));
                    log.info("Restarting Shard {}", args[1]);

                } catch (NumberFormatException e) {
                    SyntaxError.oncmdSyntaxError(new GenericMessageSendHandler(channel), "restart <shardId>", caller);
                }

            } else {
                K7Bot.getInstance().getShardManager().restart();
                log.info("Restarting all Shards");
            }

        } else {
            PermissionError.onPermissionError(caller, channel);
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
