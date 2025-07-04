package de.klassenserver7b.k7bot.logging.commands.common;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.manage.SystemNotificationChannelManager;
import de.klassenserver7b.k7bot.util.GenericMessageSendHandler;
import de.klassenserver7b.k7bot.util.HelpCategories;
import de.klassenserver7b.k7bot.util.errorhandler.SyntaxError;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class SystemChannelCommand implements ServerCommand {

    private boolean isEnabled;

    @Override
    public String getHelp() {
        return "Ändert den Channel für Systembenachrichtigungen (z.B. Logs für Einladungen oder gelöschte Nachrichten) des Bots auf diesem Server.\n - kann nur von Personen mit der Berechtigung 'Server Verwalten' ausgeführt werden!\n - z.B. [prefix]syschannel [@new_syschannel]";
    }

    @Override
    public String[] getCommandStrings() {
        return new String[]{"syschannel"};
    }

    @Override
    public HelpCategories getCategory() {
        return HelpCategories.GENERIC;
    }

    @Override
    public void performCommand(Member m, GuildMessageChannel channel, Message message) {

        if (m.hasPermission(Permission.MANAGE_SERVER)) {

            if (!message.getMentions().getChannels(GuildMessageChannel.class).isEmpty()) {

                GuildMessageChannel chan = message.getMentions().getChannels(GuildMessageChannel.class).getFirst();

                SystemNotificationChannelManager sys = K7Bot.getInstance().getSysChannelMgr();
                sys.insertChannel(chan);

                channel.sendMessage("Systemchannel was sucsessful set to " + chan.getAsMention()).queue();

            } else {
                SyntaxError.oncmdSyntaxError(new GenericMessageSendHandler(channel), "syschannel [@new syschannel]", m);
            }

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
