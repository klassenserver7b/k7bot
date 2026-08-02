/* (C)2026 */
package de.klassenserver7b.k7bot.logging.listeners;

import static de.klassenserver7b.k7bot.util.ChannelUtil.getSystemChannel;

import java.awt.*;

import de.klassenserver7b.k7bot.logging.LoggingConfigDBHandler;
import de.klassenserver7b.k7bot.logging.LoggingOptions;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.emoji.EmojiAddedEvent;
import net.dv8tion.jda.api.events.emoji.EmojiRemovedEvent;
import net.dv8tion.jda.api.events.sticker.GuildStickerAddedEvent;
import net.dv8tion.jda.api.events.sticker.GuildStickerRemovedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 *
 */
public class EmojiLoggingListener extends ListenerAdapter {
	public EmojiLoggingListener() {
		super();
	}

	@Override
	public void onEmojiAdded(EmojiAddedEvent event) {

		if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.EMOJI_ADD, event.getGuild())) {
			return;
		}

		EmbedBuilder embbuild = EmbedUtils.getDefault(event.getGuild());
		embbuild.setTitle("Emoji added");
		embbuild.setColor(Color.green);
		embbuild.setDescription("**Emoji: **" + event.getEmoji().getAsMention() + "\n**Owner: **"
				+ event.getEmoji().retrieveOwner().complete().getAsMention());

		getSystemChannel(event.getGuild()).sendMessageEmbeds(embbuild.build()).queue();

	}

	@Override
	public void onEmojiRemoved(EmojiRemovedEvent event) {

		if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.EMOJI_REMOVE, event.getGuild())) {
			return;
		}

		EmbedBuilder embbuild = EmbedUtils.getDefault(event.getGuild());
		embbuild.setTitle("Emoji removed");
		embbuild.setColor(Color.red);
		embbuild.setDescription("**Emoji: **" + event.getEmoji().getAsMention() + "\n**Owner: **"
				+ event.getEmoji().retrieveOwner().complete().getAsMention());

		getSystemChannel(event.getGuild()).sendMessageEmbeds(embbuild.build()).queue();

	}

	@Override
	public void onGuildStickerAdded(GuildStickerAddedEvent event) {

		if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.STICKER_ADD, event.getGuild())) {
			return;
		}

		EmbedBuilder embbuild = EmbedUtils.getDefault(event.getGuild());
		embbuild.setTitle("Sticker added");
		embbuild.setColor(Color.green);
		embbuild.setDescription("**Name: **" + event.getSticker().getName() + "\n**Description: **"
				+ event.getSticker().getDescription() + "\n**Owner: **"
				+ event.getSticker().retrieveOwner().complete().getAsMention());

		getSystemChannel(event.getGuild()).sendMessageEmbeds(embbuild.build()).setStickers(event.getSticker()).queue();

	}

	@Override
	public void onGuildStickerRemoved(GuildStickerRemovedEvent event) {

		if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.STICKER_REMOVE, event.getGuild())) {
			return;
		}

		EmbedBuilder embbuild = EmbedUtils.getDefault(event.getGuild());
		embbuild.setTitle("Sticker removed");
		embbuild.setColor(Color.red);
		embbuild.setDescription("**Name: **" + event.getSticker().getName() + "\n**Description: **"
				+ event.getSticker().getDescription() + "\n**Owner: **"
				+ event.getSticker().retrieveOwner().complete().getAsMention());

		getSystemChannel(event.getGuild()).sendMessageEmbeds(embbuild.build()).queue();

	}

}
