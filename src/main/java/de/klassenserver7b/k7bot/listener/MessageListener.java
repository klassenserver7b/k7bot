/* (C)2026 */
package de.klassenserver7b.k7bot.listener;

import de.klassenserver7b.k7bot.K7Bot;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public class MessageListener extends ListenerAdapter {

	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {

		if (!event.isFromGuild()) {
			return;
		}

		K7Bot.getInstance().getDb()
				.update("INSERT INTO messagelogs(messageId, guildId, timestamp, authorId,"
						+ " messageText) VALUES(?,?,?,?,?)", event.getMessageIdLong(), event.getGuild().getIdLong(),
						System.currentTimeMillis(), event.getAuthor().getIdLong(), event.getMessage().getContentRaw());
	}
}
