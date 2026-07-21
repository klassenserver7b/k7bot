/**
 *
 */
package de.klassenserver7b.k7bot.util;

import de.klassenserver7b.k7bot.K7Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;

import java.awt.*;
import java.time.OffsetDateTime;

/**
 * @author K7
 */
public abstract class EmbedUtils {

    public static final String LAVALINK_ERROR_MESSAGE = "Audio playback is currently not possible. Please try again in a few seconds.";

    public static java.util.function.Consumer<Throwable> getLavalinkErrorHandler(net.dv8tion.jda.api.entities.channel.middleman.MessageChannel channel, Long guildId) {
        return e -> channel.sendMessageEmbeds(getErrorEmbed(LAVALINK_ERROR_MESSAGE, guildId).build()).queue();
    }

    public static java.util.function.Consumer<Throwable> getLavalinkErrorHandler(net.dv8tion.jda.api.interactions.InteractionHook hook, Long guildId) {
        return e -> hook.sendMessageEmbeds(getErrorEmbed(LAVALINK_ERROR_MESSAGE, guildId).build()).queue();
    }

    public static EmbedBuilder getErrorEmbed(CharSequence description) {
        return getErrorEmbed(description, null);
    }

    public static EmbedBuilder getErrorEmbed(CharSequence description, Long guildId) {
        return getBuilderOf(Color.decode("#e74c3c"), description, guildId);
    }

    public static EmbedBuilder getSuccessEmbed(CharSequence description) {
        return getSuccessEmbed(description, null);
    }

    public static EmbedBuilder getSuccessEmbed(CharSequence description, Long guildId) {
        return getBuilderOf(Color.decode("#2ecc71"), description, guildId);
    }

    public static EmbedBuilder getInfoEmbed(CharSequence description) {
        return getInfoEmbed(description, null);
    }

    public static EmbedBuilder getInfoEmbed(CharSequence description, Long guildId) {
        return getBuilderOf(Color.decode("#3498db"), description, guildId);
    }

    public static EmbedBuilder getBuilderOf(Color c) {
        return getDefault().setColor(c);
    }

    public static EmbedBuilder getBuilderOf(CharSequence description) {
        return getBuilderOf(description, null);
    }

    public static EmbedBuilder getBuilderOf(Color c, Long guildId) {
        return getDefault(guildId).setColor(c);
    }

    public static EmbedBuilder getBuilderOf(CharSequence description, Long guildId) {
        return getDefault(guildId).appendDescription(description);
    }

    public static EmbedBuilder getBuilderOf(Color c, CharSequence description) {
        return getBuilderOf(c).appendDescription(description);
    }

    public static EmbedBuilder getBuilderOf(Color c, CharSequence description, Long guildId) {
        return getBuilderOf(c, guildId).appendDescription(description);
    }

    public static EmbedBuilder getDefault() {
        return getDefault((Long) null);
    }

    public static EmbedBuilder getDefault(Long guildId) {
        return new EmbedBuilder().setTimestamp(OffsetDateTime.now())
                .setFooter("@" + K7Bot.getInstance().getSelfName(guildId));
    }

    public static EmbedBuilder getDefault(Guild guild) {
        return new EmbedBuilder().setTimestamp(OffsetDateTime.now())
                .setFooter("@" + guild.getSelfMember().getEffectiveName());
    }

}
