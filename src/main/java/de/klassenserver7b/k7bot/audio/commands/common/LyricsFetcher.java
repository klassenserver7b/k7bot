/* (C)2026 */
package de.klassenserver7b.k7bot.audio.commands.common;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.klassenserver7b.k7bot.manage.LavaLinkManager;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import dev.arbjerg.lavalink.client.LavalinkNode;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Consumer;

public class LyricsFetcher {
	private static final Logger log = LoggerFactory.getLogger(LyricsFetcher.class);

	public static void fetchAndSendLyrics(LavalinkNode node, long guildId, Consumer<MessageEmbed> sender) {
		if (node == null) {
			sender.accept(EmbedUtils.getErrorEmbed("No active Lavalink node found", guildId).build());
			return;
		}

		String sessionId = LavaLinkManager.SESSION_IDS.get(node.getName());
		if (sessionId == null) {
			sender.accept(EmbedUtils.getErrorEmbed("Lavalink session not ready.", guildId).build());
			return;
		}

		String baseUri = node.getBaseUri();
		if (baseUri.startsWith("wss://"))
			baseUri = baseUri.replaceFirst("wss://", "https://");
		else if (baseUri.startsWith("ws://"))
			baseUri = baseUri.replaceFirst("ws://", "http://");
		String uri = baseUri + "/v4/sessions/" + sessionId + "/players/" + guildId
				+ "/track/lyrics?skipTrackSource=false";

		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).header("Authorization", node.getPassword())
				.GET().build();

		HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(response -> {
			if (response.statusCode() == 404) {
				sender.accept(EmbedUtils.getErrorEmbed("No track is playing or lyrics not found.", guildId).build());
			} else if (response.statusCode() == 204) {
				sender.accept(EmbedUtils.getErrorEmbed("No lyrics found for this track.", guildId).build());
			} else if (response.statusCode() == 200) {
				try {
					JsonObject obj = JsonParser.parseString(response.body()).getAsJsonObject();
					StringBuilder sb = new StringBuilder();

					if (obj.has("text") && !obj.get("text").isJsonNull()) {
						sb.append(obj.get("text").getAsString());
					} else if (obj.has("lines")) {
						obj.getAsJsonArray("lines").forEach(line -> {
							sb.append(line.getAsJsonObject().get("line").getAsString()).append("\n");
						});
					}

					String lyrics = sb.toString().trim();
					if (lyrics.isEmpty()) {
						sender.accept(EmbedUtils.getErrorEmbed("Lyrics are empty.", guildId).build());
						return;
					}

					for (int i = 0; i < lyrics.length(); i += 4096) {
						String chunk = lyrics.substring(i, Math.min(lyrics.length(), i + 4096));
						sender.accept(EmbedUtils.getDefault(guildId).setTitle("Lyrics").setDescription(chunk).build());
					}
				} catch (Exception e) {
					sender.accept(EmbedUtils.getErrorEmbed("Error parsing lyrics.", guildId).build());
				}
			} else {
				String msg = "Error fetching lyrics: HTTP " + response.statusCode();
				if (response.statusCode() == 500 && response.body().contains("SocketTimeoutException")) {
					msg = "The lyrics provider (e.g. LRCLib) timed out while" + " searching for this track.";
					log.warn("Lavalink lyrics search timed out (HTTP 500).");
				} else {
					log.warn("Error fetching lyrics: HTTP {}", response.statusCode());
				}
				sender.accept(EmbedUtils.getErrorEmbed(msg, guildId).build());
			}
		}).exceptionally(e -> {
			log.error("Exception fetching lyrics", e);
			sender.accept(EmbedUtils.getErrorEmbed("Error fetching lyrics: " + e.getMessage(), guildId).build());
			return null;
		});
	}
}
