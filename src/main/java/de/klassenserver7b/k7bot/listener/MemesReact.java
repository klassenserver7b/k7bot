/* (C)2026 */
package de.klassenserver7b.k7bot.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.klassenserver7b.k7bot.database.dao.MemeChannelDAO;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MemesReact extends ListenerAdapter {

	private final Logger log;

	public MemesReact() {
		log = LoggerFactory.getLogger(getClass());
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.isFromType(ChannelType.TEXT)) {

			GuildMessageChannel chan = event.getChannel().asGuildMessageChannel();
			long channelId = chan.getIdLong();

			try {
				if (new MemeChannelDAO().isMemeChannel(channelId).join()) {
					long msgId = event.getMessage().getIdLong();
					react(msgId, chan);
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	public void react(long msgId, GuildMessageChannel chan) {

		chan.addReactionById(msgId, Emoji.fromFormatted("U+2B06")).queue();

		chan.addReactionById(msgId, Emoji.fromFormatted("U+2B07")).queue();
	}
}
