/* (C)2026 */
package de.klassenserver7b.k7bot.listener;

import org.jetbrains.annotations.NotNull;

import de.klassenserver7b.k7bot.database.dao.MessageLogsDAO;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 *
 */
public class MessageListener extends ListenerAdapter {

	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {

		if (!event.isFromGuild()) {
			return;
		}

		new MessageLogsDAO().insertLog(event.getMessageIdLong(), event.getGuild().getIdLong(),
				System.currentTimeMillis(), event.getAuthor().getIdLong(), event.getMessage().getContentRaw()).join();
	}
}
