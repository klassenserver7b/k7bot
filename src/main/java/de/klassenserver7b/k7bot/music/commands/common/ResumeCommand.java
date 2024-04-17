package de.klassenserver7b.k7bot.music.commands.common;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import de.klassenserver7b.k7bot.HelpCategories;
import de.klassenserver7b.k7bot.Klassenserver7bbot;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.music.lavaplayer.MusicController;
import de.klassenserver7b.k7bot.music.utilities.MusicUtil;
import de.klassenserver7b.k7bot.util.GenericMessageSendHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.util.concurrent.TimeUnit;

public class ResumeCommand implements ServerCommand {

	private boolean isEnabled;

	@Override
	public String getHelp() {
		String help = "Setzt den aktuellen Track fort.";
		return help;
	}

	@Override
	public String[] getCommandStrings() {
		return new String[] { "resume" };
	}

	@Override
	public HelpCategories getCategory() {
		return HelpCategories.MUSIC;
	}

	@Override
	public void performCommand(Member m, GuildMessageChannel channel, Message message) {

		if (!MusicUtil.checkConditions(new GenericMessageSendHandler(channel), m)) {
			return;
		}
		AudioChannel vc = MusicUtil.getMembVcConnection(m);
		MusicController controller = Klassenserver7bbot.getInstance().getPlayerUtil()
				.getController(vc.getGuild().getIdLong());

		AudioPlayer player = controller.getPlayer();
		MusicUtil.updateChannel(channel);
		if (player.isPaused()) {
			MusicUtil.updateChannel(channel);
			player.setPaused(false);
			channel.sendMessage(":arrow_forward: resumed").queue();
		} else {
			channel.sendMessage("player is already playing!" + m.getAsMention()).complete().delete().queueAfter(10L,
					TimeUnit.SECONDS);
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