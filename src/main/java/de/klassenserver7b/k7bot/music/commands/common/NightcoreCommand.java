/**
 * 
 */
package de.klassenserver7b.k7bot.music.commands.common;

import com.github.natanbc.lavadsp.timescale.TimescalePcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import de.klassenserver7b.k7bot.HelpCategories;
import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.music.lavaplayer.MusicController;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.util.Collections;

/**
 * 
 * @author K7
 *
 */
public class NightcoreCommand implements ServerCommand {

	private boolean isEnabled;

	@Override
	public String getHelp() {
		return null;
	}

	@Override
	public String[] getCommandStrings() {
		return new String[] { "nightcore", "nc" };
	}

	@Override
	public HelpCategories getCategory() {
		return HelpCategories.UNKNOWN;
	}

	@Override
	public void performCommand(Member m, GuildMessageChannel channel, Message message) {

		MusicController controller = K7Bot.getInstance().getPlayerUtil()
				.getController(channel.getGuild().getIdLong());
		AudioPlayer player = controller.getPlayer();

		player.setFilterFactory((track, format, output) -> {
			TimescalePcmAudioFilter timescale = new TimescalePcmAudioFilter(output, format.channelCount,
					format.sampleRate);
			timescale.setRate(1.25);
			return Collections.singletonList(timescale);
		});
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
