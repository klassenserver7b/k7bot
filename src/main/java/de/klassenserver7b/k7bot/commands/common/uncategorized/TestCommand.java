/* (C)2026 */
package de.klassenserver7b.k7bot.commands.common.uncategorized;

import org.jspecify.annotations.Nullable;

import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.util.HelpCategories;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class TestCommand implements ServerCommand {

	@Override
	public @Nullable String getHelp() {
		return null;
	}

	@Override
	public String[] getCommandStrings() {
		return new String[] { "test" };
	}

	@Override
	public HelpCategories getCategory() {
		return HelpCategories.UNKNOWN;
	}

	@Override
	public void performCommand(Member caller, GuildMessageChannel channel, Message message) {
		// Test command is only used when I have something to test......
	}
}
