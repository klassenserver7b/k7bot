package de.klassenserver7b.k7bot.util.commands.common;

import de.klassenserver7b.k7bot.util.HelpCategories;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class MessagetoEmbedCommand implements ServerCommand {

	private boolean isEnabled;

	@Override
	public String getHelp() {
		return "Sendet die eingegebene Nachricht als Embed.\n - z.B. [prefix]toembed [nachricht]";
	}

	@Override
	public String[] getCommandStrings() {
		return new String[] { "toembed" };
	}

	@Override
	public HelpCategories getCategory() {
		return HelpCategories.TOOLS;
	}

	@Override
	public void performCommand(Member m, GuildMessageChannel channel, Message message) {

		String mess = message.getContentRaw().substring(9);

		EmbedBuilder builder = EmbedUtils.getSuccessEmbed(mess, channel.getGuild().getIdLong());
		builder.setFooter("Requested by @" + m.getEffectiveName());
		builder.setTitle("@" + m.getEffectiveName() + "'s embed");

		channel.sendMessageEmbeds(builder.build()).queue();
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