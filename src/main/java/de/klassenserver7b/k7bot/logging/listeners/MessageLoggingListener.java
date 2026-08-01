/* (C)2026 */
package de.klassenserver7b.k7bot.logging.listeners;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.logging.LoggingConfigDBHandler;
import de.klassenserver7b.k7bot.logging.LoggingOptions;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;

import static de.klassenserver7b.k7bot.util.ChannelUtil.getSystemChannel;

/**
 *
 */
public class MessageLoggingListener extends ListenerAdapter {

	private final Logger log;

	/**
	 *
	 */
	public MessageLoggingListener() {
		super();
		log = LoggerFactory.getLogger(getClass());
	}

	@Override
	public void onMessageUpdate(MessageUpdateEvent event) {

		if (isIgnoredEvent(LoggingOptions.MESSAGE_EDITED, event.getMessageIdLong(), event.getGuild())) {
			return;
		}

		GuildMessageChannel system = getSystemChannel(event.getGuild());

		EmbedBuilder embbuild = EmbedUtils.getDefault(event.getGuild());

		embbuild.setColor(Color.yellow);
		embbuild.setTitle("Message edited in " + event.getChannel().getAsMention());
		embbuild.setDescription(
				"**User: **" + event.getAuthor().getAsMention() + "\n**Message: **" + event.getJumpUrl());

		system.sendMessageEmbeds(embbuild.build()).queue();
	}

	@Override
	public void onMessageDelete(MessageDeleteEvent event) {

		if (isIgnoredEvent(LoggingOptions.MESSAGE_DELETED, event.getMessageIdLong(), event.getGuild())) {
			return;
		}
		GuildMessageChannel system = getSystemChannel(event.getGuild());

		EmbedBuilder embbuild = EmbedUtils.getDefault(event.getGuild());

		embbuild.setColor(Color.red);
		embbuild.setTitle("Message deleted in " + event.getChannel().getAsMention());
		embbuild.setDescription("**Message: **" + event.getJumpUrl() + "\n**MessageId: **" + event.getMessageIdLong());

		system.sendMessageEmbeds(embbuild.build()).queue();
	}

	@Override
	public void onMessageBulkDelete(MessageBulkDeleteEvent event) {

		if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.MESSAGE_BULK_DELETED, event.getGuild())) {
			return;
		}

		GuildMessageChannel system = getSystemChannel(event.getGuild());

		EmbedBuilder embbuild = EmbedUtils.getDefault(event.getGuild());

		embbuild.setColor(Color.red);
		embbuild.setTitle("Messages deleted in " + event.getChannel().getAsMention());
		embbuild.setDescription("**Amount: **" + event.getMessageIds().size());

		system.sendMessageEmbeds(embbuild.build()).queue();
	}

	protected boolean isIgnoredEvent(LoggingOptions option, long messageId, Guild guild) {

		if (LoggingConfigDBHandler.isOptionDisabled(option, guild)) {
			return true;
		}

		return isBotMessage(messageId, guild);
	}

	protected boolean isBotMessage(long messageId, Guild guild) {

		try (ResultSet set = K7Bot.getInstance().getDb().query(
				"SELECT authorId FROM messagelogs WHERE messageId = ? AND guildId =" + " ?;", messageId,
				guild.getIdLong())) {

			assert set != null;
			if (set.next()) {
				return set.getLong("authorId") == guild.getSelfMember().getUser().getIdLong();
			}

		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		}

		return false;
	}
}
