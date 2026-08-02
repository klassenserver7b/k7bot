/* (C)2026 */
package de.klassenserver7b.k7bot.logging.listeners;

import static de.klassenserver7b.k7bot.util.ChannelUtil.getSystemChannel;

import java.awt.*;
import java.util.Objects;

import de.klassenserver7b.k7bot.logging.LoggingConfigDBHandler;
import de.klassenserver7b.k7bot.logging.LoggingOptions;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.dv8tion.jda.api.events.guild.scheduledevent.ScheduledEventCreateEvent;
import net.dv8tion.jda.api.events.guild.scheduledevent.ScheduledEventDeleteEvent;
import net.dv8tion.jda.api.events.guild.scheduledevent.ScheduledEventUserAddEvent;
import net.dv8tion.jda.api.events.guild.scheduledevent.ScheduledEventUserRemoveEvent;
import net.dv8tion.jda.api.events.guild.scheduledevent.update.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 *
 */
public class EventLoggingListener extends ListenerAdapter {

	public EventLoggingListener() {
		super();
	}

	@Override
	public void onScheduledEventCreate(ScheduledEventCreateEvent event) {

		if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.EVENT_CREATE, event.getGuild())) {
			return;
		}

		EmbedBuilder embbuild = EmbedUtils.getDefault(event.getGuild());
		embbuild.setTitle("Event created");
		embbuild.setColor(Color.green);
		embbuild.setDescription(createScheduledEventMessage(event.getScheduledEvent()));

		getSystemChannel(event).sendMessageEmbeds(embbuild.build()).queue();

	}

	@Override
	public void onScheduledEventDelete(ScheduledEventDeleteEvent event) {

		if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.EVENT_DELETE, event.getGuild())) {
			return;
		}

		EmbedBuilder embbuild = EmbedUtils.getDefault(event.getGuild());
		embbuild.setTitle("Event removed");
		embbuild.setColor(Color.red);
		embbuild.setDescription(createScheduledEventMessage(event.getScheduledEvent()));

		getSystemChannel(event).sendMessageEmbeds(embbuild.build()).queue();

	}

	@Override
	public void onScheduledEventUpdateStatus(ScheduledEventUpdateStatusEvent event) {

		if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.EVENT_UPDATE_STATUS, event.getGuild())) {
			return;
		}

		EmbedBuilder embbuild = EmbedUtils.getDefault(event.getGuild());
		embbuild.setTitle("Event status updated");
		embbuild.setColor(Color.yellow);
		embbuild.setDescription("**Event: **" + event.getScheduledEvent().getName() + "\n**Old status: **"
				+ event.getOldStatus().name() + "\n**New status: **" + event.getNewStatus().name());

		getSystemChannel(event).sendMessageEmbeds(embbuild.build()).queue();

	}

	@Override
	public void onScheduledEventUpdateName(ScheduledEventUpdateNameEvent event) {

		if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.EVENT_UPDATE_NAME, event.getGuild())) {
			return;
		}

		EmbedBuilder embbuild = EmbedUtils.getDefault(event.getGuild());
		embbuild.setTitle("Event name updated");
		embbuild.setColor(Color.yellow);
		embbuild.setDescription("**New name: **" + event.getNewName() + "\n**Old name: **" + event.getOldName());

		getSystemChannel(event).sendMessageEmbeds(embbuild.build()).queue();

	}

	@Override
	public void onScheduledEventUpdateStartTime(ScheduledEventUpdateStartTimeEvent event) {

		if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.EVENT_UPDATE_STARTTIME, event.getGuild())) {
			return;
		}

		EmbedBuilder embbuild = EmbedUtils.getDefault(event.getGuild());
		embbuild.setTitle("Event StartTime updated");
		embbuild.setColor(Color.yellow);
		embbuild.setDescription("**New time: **<t:" + event.getNewStartTime().toEpochSecond() + ">" + "\n**Old time: **"
				+ event.getOldStartTime().toEpochSecond() + ">");

		getSystemChannel(event).sendMessageEmbeds(embbuild.build()).queue();

	}

	@Override
	public void onScheduledEventUpdateEndTime(ScheduledEventUpdateEndTimeEvent event) {

		if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.EVENT_UPDATE_ENDTIME, event.getGuild())) {
			return;
		}

		EmbedBuilder embbuild = EmbedUtils.getDefault(event.getGuild());
		embbuild.setTitle("Event StartTime updated");
		embbuild.setColor(Color.yellow);

		StringBuilder sb = new StringBuilder();

		if (event.getNewEndTime() != null) {
			sb.append("**New time: **<t:").append(event.getNewEndTime().toEpochSecond()).append(">");
		}
		if (event.getOldEndTime() != null) {
			sb.append("\n**Old time: **").append(event.getOldEndTime().toEpochSecond()).append(">");
		}

		embbuild.setDescription(sb);

		getSystemChannel(event).sendMessageEmbeds(embbuild.build()).queue();

	}

	@Override
	public void onScheduledEventUpdateLocation(ScheduledEventUpdateLocationEvent event) {

		if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.EVENT_UPDATE_LOCATION, event.getGuild())) {
			return;
		}

		EmbedBuilder embbuild = EmbedUtils.getDefault(event.getGuild());
		embbuild.setTitle("Event location updated");
		embbuild.setColor(Color.yellow);
		embbuild.setDescription(
				"**New location: **" + event.getNewLocation() + "\n**Old location: **" + event.getOldLocation());

		getSystemChannel(event).sendMessageEmbeds(embbuild.build()).queue();

	}

	@Override
	public void onScheduledEventUserAdd(ScheduledEventUserAddEvent event) {

		if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.EVENT_MEMBER_JOIN, event.getGuild())) {
			return;
		}

		EmbedBuilder embbuild = EmbedUtils.getDefault(event.getGuild());
		embbuild.setTitle("Member joined event");
		embbuild.setColor(Color.green);
		embbuild.setDescription("**Event: **" + event.getScheduledEvent().getName() + "\n**Member: **"
				+ event.retrieveMember().complete().getAsMention());

		getSystemChannel(event).sendMessageEmbeds(embbuild.build()).queue();

	}

	@Override
	public void onScheduledEventUserRemove(ScheduledEventUserRemoveEvent event) {

		if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.EVENT_MEMBER_LEAVE, event.getGuild())) {
			return;
		}

		EmbedBuilder embbuild = EmbedUtils.getDefault(event.getGuild());
		embbuild.setTitle("Member left event");
		embbuild.setColor(Color.red);
		embbuild.setDescription("**Event: **" + event.getScheduledEvent().getName() + "\n**Member: **"
				+ event.retrieveMember().complete().getAsMention());

		getSystemChannel(event).sendMessageEmbeds(embbuild.build()).queue();

	}

	/**
	 * Creates a message for the given ScheduledEvent.
	 *
	 * @param event The ScheduledEvent to create the message for.
	 * @return The message.
	 */

	protected String createScheduledEventMessage(ScheduledEvent event) {

		StringBuilder sb = new StringBuilder();
		sb.append("**Name: **").append(event.getName());

		if (event.getCreator() != null) {
			sb.append("\n**Creator: **").append(event.getCreator().getAsMention());
		}

		sb.append("\n**StartTime: **<t:").append(event.getStartTime().toEpochSecond()).append(">");

		if (event.getType() == ScheduledEvent.Type.EXTERNAL) {
			sb.append("\n**EndTime: **<t:").append(Objects.requireNonNull(event.getEndTime()).toEpochSecond())
					.append(">");
		} else {
			sb.append("\n**Location: **").append(Objects.requireNonNull(event.getChannel()).getAsMention());
		}

		return sb.toString();
	}

}
