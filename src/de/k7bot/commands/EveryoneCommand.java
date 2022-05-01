package de.k7bot.commands;

import de.k7bot.commands.types.ServerCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class EveryoneCommand implements ServerCommand {
	public void performCommand(Member m, TextChannel channel, Message message) {
		String[] args = message.getContentDisplay().split(" ");
		StringBuilder builder = new StringBuilder();

		for (int i = 1; i < args.length; i++) {
			builder.append(" " + args[i]);
		}

		channel.sendMessage(
				String.valueOf(channel.getGuild().getPublicRole().getAsMention()) + " " + builder.toString().trim())
				.queue();
	}

	@Override
	public String gethelp() {
		String help = "Sendet die aktuelle Nachricht als @everyone.\n - z.B. [prefix]everyone [Nachricht]";
		return help;
	}

	@Override
	public String getcategory() {
		String category = "Tools";
		return category;
	}
}