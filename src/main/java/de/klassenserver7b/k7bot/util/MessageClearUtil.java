/**
 *
 */
package de.klassenserver7b.k7bot.util;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Klassenserver7b
 *
 */
public class MessageClearUtil {

	private static final Logger log = LoggerFactory.getLogger(MessageClearUtil.class);

	public static void onclear(int amount, GuildMessageChannel chan) {
		try {

			chan.purgeMessages(getMessages(chan, amount));

		} catch (NumberFormatException e) {
			log.error(e.getMessage(), e);
		}
	}

	public static List<Message> getMessages(MessageChannel channel, int amount) {
		List<Message> messages = new ArrayList<>();

		int i = 0;

		for (Message message : channel.getIterableHistory().cache(false)) {
			if (!message.isPinned()) {
				messages.add(message);
			}

			if (i++ >= amount) {
				break;
			}
		}
		return messages;
	}

}
