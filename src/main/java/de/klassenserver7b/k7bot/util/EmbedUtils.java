/* (C)2026 */
package de.klassenserver7b.k7bot.util;

import java.awt.*;
import java.time.OffsetDateTime;

import de.klassenserver7b.k7bot.K7Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;

/**
 * @author K7
 */
@SuppressWarnings("unused")
public final class EmbedUtils {

	private EmbedUtils() {
	}

	public static final String LAVALINK_ERROR_MESSAGE = "Audio playback is currently not possible. Please try again in a few seconds.";
	public static Color errorColor = new Color(0xe74c3c);
	public static Color successColor = new Color(0x2ecc71);
	public static Color warningColor = new Color(0xe74c71);
	public static Color infoColor = new Color(0x3498db);

	public static java.util.function.Consumer<Throwable> getLavalinkErrorHandler(
			net.dv8tion.jda.api.entities.channel.middleman.MessageChannel channel, Long guildId) {
		return _ -> channel.sendMessageEmbeds(getErrorEmbed(LAVALINK_ERROR_MESSAGE, guildId).build()).queue();
	}

	public static java.util.function.Consumer<Throwable> getLavalinkErrorHandler(
			net.dv8tion.jda.api.interactions.InteractionHook hook, Long guildId) {
		return _ -> hook.sendMessageEmbeds(getErrorEmbed(LAVALINK_ERROR_MESSAGE, guildId).build()).queue();
	}

	public static EmbedBuilder getErrorEmbed(CharSequence description) {
		return getBuilderOf(errorColor, description);
	}

	public static EmbedBuilder getErrorEmbed(CharSequence description, long guildId) {
		return getBuilderOf(errorColor, description, guildId);
	}

	public static EmbedBuilder getErrorEmbed(CharSequence description, long guildId, long userId) {
		return setUserFooter(getBuilderOf(errorColor, description, guildId), userId);
	}

	@SuppressWarnings("unused")
	public static EmbedBuilder getSuccessEmbed(CharSequence description) {
		return getBuilderOf(successColor, description);
	}

	public static EmbedBuilder getSuccessEmbed(CharSequence description, long guildId) {
		return getBuilderOf(successColor, description, guildId);
	}

	@SuppressWarnings("unused")
	public static EmbedBuilder getInfoEmbed(CharSequence description) {
		return getBuilderOf(infoColor, description);
	}

	@SuppressWarnings("unused")
	public static EmbedBuilder getInfoEmbed(CharSequence description, long guildId, long userId) {
		return setUserFooter(getInfoEmbed(description, guildId), userId);
	}

	public static EmbedBuilder getInfoEmbed(CharSequence description, long guildId) {
		return getBuilderOf(infoColor, description, guildId);
	}

	public static EmbedBuilder getBuilderOf(CharSequence description) {
		return getDefault().appendDescription(description);
	}

	public static EmbedBuilder getBuilderOf(CharSequence description, long guildId) {
		return getDefault(guildId).appendDescription(description);
	}

	public static EmbedBuilder getBuilderOf(Color c) {
		return getDefault().setColor(c);
	}

	public static EmbedBuilder getBuilderOf(Color c, long guildId) {
		return getDefault(guildId).setColor(c);
	}

	public static EmbedBuilder getBuilderOf(Color c, CharSequence description) {
		return getBuilderOf(c).appendDescription(description);
	}

	public static EmbedBuilder getBuilderOf(Color c, CharSequence description, long guildId) {
		return getBuilderOf(c, guildId).appendDescription(description);
	}

	public static EmbedBuilder getDefault() {
		return getDefault(0);
	}

	public static EmbedBuilder getDefault(long guildId) {
		return new EmbedBuilder().setTimestamp(OffsetDateTime.now())
				.setFooter("@" + K7Bot.getInstance().getSelfName(guildId));
	}

	public static EmbedBuilder setUserFooter(EmbedBuilder builder, long userId) {
		User u = K7Bot.getInstance().getShardManager().getUserById(userId);

		if (u != null) {
			return builder.setFooter("requested by @" + u.getEffectiveName());
		} else {
			return builder;
		}
	}

}
