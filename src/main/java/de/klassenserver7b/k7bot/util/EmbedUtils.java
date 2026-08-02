/* (C)2026 */
package de.klassenserver7b.k7bot.util;

import java.awt.*;
import java.time.OffsetDateTime;

import org.jetbrains.annotations.Nullable;

import de.klassenserver7b.k7bot.K7Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;

/**
 * @author K7
 */
public abstract class EmbedUtils {

	public static final String LAVALINK_ERROR_MESSAGE = "Audio playback is currently not possible. Please try again in a few seconds.";

	public static java.util.function.Consumer<Throwable> getLavalinkErrorHandler(
			net.dv8tion.jda.api.entities.channel.middleman.MessageChannel channel, Long guildId) {
		return _ -> channel.sendMessageEmbeds(getErrorEmbed(LAVALINK_ERROR_MESSAGE, guildId).build()).queue();
	}

	public static java.util.function.Consumer<Throwable> getLavalinkErrorHandler(
			net.dv8tion.jda.api.interactions.InteractionHook hook, Long guildId) {
		return _ -> hook.sendMessageEmbeds(getErrorEmbed(LAVALINK_ERROR_MESSAGE, guildId).build()).queue();
	}

	public static EmbedBuilder getErrorEmbed(CharSequence description) {
		return getErrorEmbed(description, (Long) null);
	}

	public static EmbedBuilder getErrorEmbed(CharSequence description, @Nullable Long guildId) {
		return getBuilderOf(Color.decode("#e74c3c"), description, guildId);
	}

	public static EmbedBuilder getErrorEmbed(CharSequence description, @Nullable Guild guild) {
		Long guildId = guild == null ? null : guild.getIdLong();
		return getBuilderOf(Color.decode("#e74c3c"), description, guildId);
	}

	@SuppressWarnings("unused")
	public static EmbedBuilder getSuccessEmbed(CharSequence description) {
		return getSuccessEmbed(description, null);
	}

	public static EmbedBuilder getSuccessEmbed(CharSequence description, @Nullable Long guildId) {
		return getBuilderOf(Color.decode("#2ecc71"), description, guildId);
	}

	@SuppressWarnings("unused")
	public static EmbedBuilder getInfoEmbed(CharSequence description) {
		return getInfoEmbed(description, null);
	}

	public static EmbedBuilder getInfoEmbed(CharSequence description, @Nullable Long guildId) {
		return getBuilderOf(Color.decode("#3498db"), description, guildId);
	}

	public static EmbedBuilder getBuilderOf(Color c) {
		return getDefault().setColor(c);
	}

	public static EmbedBuilder getBuilderOf(CharSequence description) {
		return getBuilderOf(description, (Long) null);
	}

	public static EmbedBuilder getBuilderOf(Color c, @Nullable Long guildId) {
		return getDefault(guildId).setColor(c);
	}

	public static EmbedBuilder getBuilderOf(CharSequence description, @Nullable Long guildId) {
		return getDefault(guildId).appendDescription(description);
	}

	public static EmbedBuilder getBuilderOf(CharSequence description, @Nullable Guild guild) {
		Long guildId = guild == null ? null : guild.getIdLong();
		return getDefault(guildId).appendDescription(description);
	}

	public static EmbedBuilder getBuilderOf(Color c, CharSequence description) {
		return getBuilderOf(c).appendDescription(description);
	}

	public static EmbedBuilder getBuilderOf(Color c, CharSequence description, @Nullable Long guildId) {
		return getBuilderOf(c, guildId).appendDescription(description);
	}

	public static EmbedBuilder getBuilderOf(Color c, CharSequence description, @Nullable Guild guild) {
		Long guildId = guild == null ? null : guild.getIdLong();
		return getBuilderOf(c, guildId).appendDescription(description);
	}

	public static EmbedBuilder getDefault() {
		return getDefault((Long) null);
	}

	public static EmbedBuilder getDefault(@Nullable Long guildId) {
		return new EmbedBuilder().setTimestamp(OffsetDateTime.now())
				.setFooter("@" + K7Bot.getInstance().getSelfName(guildId));
	}

	public static EmbedBuilder getDefault(@Nullable Guild guild) {
		Long guildId = guild == null ? null : guild.getIdLong();
		return getDefault(guildId);
	}

}
