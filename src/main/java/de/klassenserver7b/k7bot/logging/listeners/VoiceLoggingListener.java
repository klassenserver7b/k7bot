/* (C)2026 */
package de.klassenserver7b.k7bot.logging.listeners;

import static de.klassenserver7b.k7bot.util.ChannelUtil.getSystemChannel;

import java.awt.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.klassenserver7b.k7bot.logging.LoggingConfigDBHandler;
import de.klassenserver7b.k7bot.logging.LoggingOptions;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.guild.voice.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 *
 */
public class VoiceLoggingListener extends ListenerAdapter {

	private final Logger log;

	public VoiceLoggingListener() {
		log = LoggerFactory.getLogger(getClass());
	}

	public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
		AudioChannel joinedChannel = event.getChannelJoined();
		AudioChannel leftChannel = event.getChannelLeft();
		GuildMessageChannel sysChannel = getSystemChannel(event);

		if (joinedChannel != null && leftChannel != null) {
			onChannelMove(joinedChannel, leftChannel, sysChannel, event);
			return;
		}

		if (joinedChannel != null) {
			onChannelJoin(joinedChannel, sysChannel, event);
			return;
		}

		if (leftChannel != null) {
			onChannelLeave(leftChannel, sysChannel, event);
			return;
		}

		log.warn("GuildVoiceUpdateEvent triggered without joined or left channel - guild: {}, member: {}",
				event.getGuild().getName(), event.getMember().getEffectiveName());
	}

	@Override
	public void onGuildVoiceMute(GuildVoiceMuteEvent event) {

		if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.GUILD_MUTE, event.getGuild())) {
			return;
		}

		EmbedBuilder embbuild = EmbedUtils.getDefault(event.getGuild());

		embbuild.setColor(Color.red);
		embbuild.setTitle("Member server-muted");
		embbuild.setDescription(event.getMember().getAsMention() + " was muted");

		getSystemChannel(event).sendMessageEmbeds(embbuild.build()).queue();
	}

	@Override
	public void onGuildVoiceDeafen(GuildVoiceDeafenEvent event) {

		if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.GUILD_DEAF, event.getGuild())) {
			return;
		}

		EmbedBuilder embbuild = EmbedUtils.getDefault(event.getGuild());

		embbuild.setColor(Color.red);
		embbuild.setTitle("Member server-deafed");
		embbuild.setDescription(event.getMember().getAsMention() + " was deafed");

		getSystemChannel(event).sendMessageEmbeds(embbuild.build()).queue();
	}

	@Override
	public void onGuildVoiceStream(GuildVoiceStreamEvent event) {

		if (event.isStream()) {
			onStreamStart(event);
		} else {
			onStreamStop(event);
		}

	}

	@Override
	public void onGuildVoiceVideo(GuildVoiceVideoEvent event) {

		if (event.isSendingVideo()) {
			onVideoStart(event);
		} else {
			onVideoStop(event);
		}

	}

	protected void onVideoStart(GuildVoiceVideoEvent event) {

		if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.VIDEO_START, event.getGuild())) {
			return;
		}

		EmbedBuilder embbuild = EmbedUtils.getDefault(event.getGuild());

		embbuild.setColor(Color.green);
		embbuild.setTitle("Member started camera");

		StringBuilder sb = new StringBuilder();
		sb.append(event.getMember().getAsMention()).append(" started camera");

		if (event.getVoiceState().getChannel() != null) {
			sb.append(" in ").append(event.getVoiceState().getChannel().getAsMention());
		}

		embbuild.setDescription(sb);

		getSystemChannel(event).sendMessageEmbeds(embbuild.build()).queue();

	}

	protected void onVideoStop(GuildVoiceVideoEvent event) {

		if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.VIDEO_STOP, event.getGuild())) {
			return;
		}

		EmbedBuilder embbuild = EmbedUtils.getDefault(event.getGuild());

		embbuild.setColor(Color.red);
		embbuild.setTitle("Member stopped camera");

		StringBuilder sb = new StringBuilder();
		sb.append(event.getMember().getAsMention()).append(" stopped camera");

		if (event.getVoiceState().getChannel() != null) {
			sb.append(" in ").append(event.getVoiceState().getChannel().getAsMention());
		}

		embbuild.setDescription(sb);

		getSystemChannel(event).sendMessageEmbeds(embbuild.build()).queue();

	}

	protected void onStreamStart(GuildVoiceStreamEvent event) {

		if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.STREAM_START, event.getGuild())) {
			return;
		}

		EmbedBuilder embbuild = EmbedUtils.getDefault(event.getGuild());

		embbuild.setColor(Color.green);
		embbuild.setTitle("Member started streaming");

		StringBuilder sb = new StringBuilder();
		sb.append(event.getMember().getAsMention()).append(" started streaming");

		if (event.getVoiceState().getChannel() != null) {
			sb.append(" in ").append(event.getVoiceState().getChannel().getAsMention());
		}

		embbuild.setDescription(sb);

		getSystemChannel(event).sendMessageEmbeds(embbuild.build()).queue();

	}

	protected void onStreamStop(GuildVoiceStreamEvent event) {

		if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.STREAM_STOP, event.getGuild())) {
			return;
		}

		EmbedBuilder embbuild = EmbedUtils.getDefault(event.getGuild());

		embbuild.setColor(Color.red);
		embbuild.setTitle("Member stopped streaming");

		StringBuilder sb = new StringBuilder();
		sb.append(event.getMember().getAsMention()).append(" stopped streaming");

		if (event.getVoiceState().getChannel() != null) {
			sb.append(" in ").append(event.getVoiceState().getChannel().getAsMention());
		}

		embbuild.setDescription(sb);

		getSystemChannel(event).sendMessageEmbeds(embbuild.build()).queue();

	}

	protected void onChannelJoin(AudioChannel joinedChannel, GuildMessageChannel sysChannel,
			GuildVoiceUpdateEvent event) {

		if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.VOICE_JOIN, event.getGuild())) {
			return;
		}

		EmbedBuilder embbuild = EmbedUtils.getDefault(event.getGuild());

		embbuild.setColor(Color.green);
		embbuild.setTitle("Member joined voice channel");
		embbuild.setDescription(event.getMember().getAsMention() + " joined " + joinedChannel.getAsMention());

		sysChannel.sendMessageEmbeds(embbuild.build()).queue();

	}

	protected void onChannelLeave(AudioChannel leftChannel, GuildMessageChannel sysChannel,
			GuildVoiceUpdateEvent event) {

		if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.VOICE_LEAVE, event.getGuild())) {
			return;
		}

		EmbedBuilder embbuild = EmbedUtils.getDefault(event.getGuild());

		embbuild.setColor(Color.red);
		embbuild.setTitle("Member left voice channel");
		embbuild.setDescription(event.getMember().getAsMention() + " left " + leftChannel.getAsMention());

		sysChannel.sendMessageEmbeds(embbuild.build()).queue();

	}

	protected void onChannelMove(AudioChannel joinedChannel, AudioChannel leftChannel, GuildMessageChannel sysChannel,
			GuildVoiceUpdateEvent event) {

		if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.VOICE_MOVE, event.getGuild())) {
			return;
		}

		EmbedBuilder embbuild = EmbedUtils.getDefault(event.getGuild());

		embbuild.setColor(Color.yellow);
		embbuild.setTitle("Member moved in voice channel");
		embbuild.setDescription(event.getMember().getAsMention() + " moved from\n" + leftChannel.getAsMention() + " to "
				+ joinedChannel.getAsMention());

		sysChannel.sendMessageEmbeds(embbuild.build()).queue();

	}

}
