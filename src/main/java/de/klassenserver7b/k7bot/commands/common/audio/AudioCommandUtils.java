/* (C)2026 */
package de.klassenserver7b.k7bot.commands.common.audio;

import java.awt.*;
import java.time.Duration;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import de.klassenserver7b.k7bot.audio.AudioLoadOption;
import de.klassenserver7b.k7bot.audio.AudioLoadResultHandler;
import de.klassenserver7b.k7bot.audio.GuildAudioManager;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import dev.arbjerg.lavalink.client.Link;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class AudioCommandUtils {

	public static void loadItem(Link link, String query, GuildAudioManager gam, long memberId, AudioLoadOption option,
			boolean justConnected, Consumer<Throwable> errorHandler) {
		if (justConnected) {
			reactor.core.publisher.Mono.delay(Duration.ofMillis(600)).then(link.loadItem(query))
					.subscribe(new AudioLoadResultHandler(gam, option, memberId), errorHandler);
		} else {
			link.loadItem(query).subscribe(new AudioLoadResultHandler(gam, option, memberId), errorHandler);
		}
	}

	public static String resolveQuery(String query) {
		return query.startsWith("http") ? query : "ytsearch:" + query;
	}

	public static MessageEmbed formatQueue(GuildAudioManager gam, long guildId) {
		StringBuilder sb = new StringBuilder("**Current Queue:**\n");
		int i = 1;
		for (var track : gam.getTrackScheduler().queue) {
			sb.append(i++).append(". ").append(track.getInfo().getTitle()).append("\n");
			if (i > 10)
				break;
		}
		if (i == 1)
			sb.append("Empty");

		return EmbedUtils.getBuilderOf(Color.decode("#14cdc8"), sb.toString(), guildId).setTitle("Queue List").build();
	}

	public static @Nullable MessageEmbed formatNowPlaying(GuildAudioManager gam, long guildId) {
		var player = gam.getPlayer().orElse(null);
		if (player != null && player.getTrack() != null) {
			var track = player.getTrack();
			var info = track.getInfo();
			long pos = player.getPosition();
			long len = info.getLength();

			String posStr = String.format("%02d:%02d", (pos / 1000) / 60, (pos / 1000) % 60);
			String lenStr = String.format("%02d:%02d", (len / 1000) / 60, (len / 1000) % 60);

			return EmbedUtils.getDefault(guildId).setTitle("Now Playing")
					.setDescription("[" + info.getTitle() + "](" + info.getUri() + ")\n\n" + "Author: "
							+ info.getAuthor() + "\n" + "Position: `" + posStr + " / " + lenStr + "`")
					.build();
		}
		return null;
	}
}
