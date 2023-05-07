/**
 *
 */
package de.k7bot.music.commands.common;

import de.k7bot.HelpCategories;
import de.k7bot.Klassenserver7bbot;
import de.k7bot.commands.types.ServerCommand;
import de.k7bot.music.lavaplayer.MusicController;
import de.k7bot.music.utilities.MusicUtil;
import de.k7bot.util.GenericMessageSendHandler;
import de.k7bot.util.errorhandler.SyntaxError;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

/**
 * @author Klassenserver7b
 *
 */
public class SeekCommand implements ServerCommand {

	private boolean isEnabled;

	@Override
	public String gethelp() {
		return "Spult zur gewählten Position im Song vor.\n - z.B. [prefix]seek [position in seconds]";
	}

	@Override
	public String[] getCommandStrings() {
		return new String[] { "seek" };
	}

	@Override
	public HelpCategories getcategory() {
		return HelpCategories.MUSIK;
	}

	@Override
	public void performCommand(Member m, TextChannel channel, Message message) {

		if (!MusicUtil.checkConditions(new GenericMessageSendHandler(channel), m)) {
			return;
		}

		String[] args = message.getContentDisplay().split(" ");

		if (args.length < 2) {
			SyntaxError.oncmdSyntaxError(new GenericMessageSendHandler(channel), "seek [position in seconds]", m);
			return;
		}

		MusicController controller = Klassenserver7bbot.getInstance().getPlayerUtil()
				.getController(m.getGuild().getIdLong());
		int pos = Integer.valueOf(args[1]);
		controller.seek(pos * 1000);

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
