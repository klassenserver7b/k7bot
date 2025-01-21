package de.klassenserver7b.k7bot.commands.common;

import de.klassenserver7b.k7bot.HelpCategories;
import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.util.errorhandler.PermissionError;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class ShutdownCommand implements ServerCommand {

	private boolean isEnabled;

	@Override
	public String getHelp() {
        return "Fährt den Bot herunter.\n - kann nur vom Bot Owner ausgeführt werden!";
	}

	@Override
	public String[] getCommandStrings() {
		return new String[] { "shutdown" };
	}

	@Override
	public HelpCategories getCategory() {
		return HelpCategories.TOOLS;
	}

	@Override
	public void performCommand(Member caller, GuildMessageChannel channel, Message message) {

		if (caller.getIdLong() == K7Bot.getInstance().getOwnerId()) {

			K7Bot.getInstance().setExit(true);
			K7Bot.getInstance().getShutdownThread().onShutdown();
			return;

		}

		PermissionError.onPermissionError(caller, channel);

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