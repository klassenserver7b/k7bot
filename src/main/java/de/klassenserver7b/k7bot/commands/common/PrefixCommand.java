package de.klassenserver7b.k7bot.commands.common;

import de.klassenserver7b.k7bot.util.HelpCategories;
import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import de.klassenserver7b.k7bot.util.GenericMessageSendHandler;
import de.klassenserver7b.k7bot.util.errorhandler.PermissionError;
import de.klassenserver7b.k7bot.util.errorhandler.SyntaxError;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.util.concurrent.TimeUnit;

public class PrefixCommand implements ServerCommand {

	private boolean isEnabled;

	@Override
	public String getHelp() {
		return "Ändert das Prefix des Bots auf diesem Server.\n - z.B. [prefix][new prefix]";
	}

	@Override
	public String[] getCommandStrings() {
		return new String[] { "prefix" };
	}

	@Override
	public HelpCategories getCategory() {
		return HelpCategories.GENERIC;
	}

	@Override
	public void performCommand(Member caller, GuildMessageChannel channel, Message message) {

		if (caller.hasPermission(Permission.ADMINISTRATOR)) {
			String[] args = message.getContentDisplay().split(" ");

			if (args.length > 1) {

				K7Bot.getInstance().getPrefixMgr().setPrefix(channel.getGuild().getIdLong(), args[1]);
				EmbedBuilder builder = EmbedUtils.getDefault(channel.getGuild().getIdLong());
				builder.setFooter("Requested by @" + caller.getEffectiveName());
				builder.setTitle("Prefix was set to \"" + args[1] + "\"");
				channel.sendMessageEmbeds(builder.build()).complete().delete().queueAfter(10L, TimeUnit.SECONDS);

			} else {

				SyntaxError.oncmdSyntaxError(new GenericMessageSendHandler(channel), "prefix [String]", caller);
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