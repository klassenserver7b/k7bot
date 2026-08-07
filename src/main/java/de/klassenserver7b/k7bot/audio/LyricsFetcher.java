/* (C)2026 */
package de.klassenserver7b.k7bot.audio;

import java.util.function.Consumer;

import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.klassenserver7b.k7bot.manage.LavaLinkManager;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import dev.arbjerg.lavalink.client.LavalinkNode;
import net.dv8tion.jda.api.entities.MessageEmbed;

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
			// noinspection HttpUrlsUsage
			baseUri = baseUri.replaceFirst("ws://", "http://");
		String uri = baseUri + "/v4/sessions/" + sessionId + "/players/" + guildId
				+ "/track/lyrics?skipTrackSource=false";

		try (CloseableHttpAsyncClient httpClient = HttpAsyncClients.createDefault()) {
			httpClient.start();

			SimpleHttpRequest request = SimpleHttpRequest.create("GET", uri);
			request.setHeader("Authorization", node.getPassword());

			httpClient.execute(request, new FutureCallback<>() {
				@Override
				public void completed(SimpleHttpResponse response) {
					handleResponse(response, guildId, sender);
				}

				@Override
				public void failed(Exception ex) {
					log.error("Exception fetching lyrics", ex);
					sender.accept(
							EmbedUtils.getErrorEmbed("Error fetching lyrics: " + ex.getMessage(), guildId).build());
				}

				@Override
				public void cancelled() {
					log.warn("Lyrics request cancelled");
				}
			});
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private static void handleResponse(SimpleHttpResponse response, long guildId, Consumer<MessageEmbed> sender) {
		int statusCode = response.getCode();
		String body = response.getBodyText();

		if (statusCode == HttpStatus.SC_OK) {
			try {
				JsonObject obj = JsonParser.parseString(body).getAsJsonObject();
				StringBuilder sb = new StringBuilder();

				if (obj.has("text") && !obj.get("text").isJsonNull()) {
					sb.append(obj.get("text").getAsString());
				} else if (obj.has("lines")) {
					obj.getAsJsonArray("lines")
							.forEach(line -> sb.append(line.getAsJsonObject().get("line").getAsString()).append("\n"));
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
		} else if (statusCode == HttpStatus.SC_NO_CONTENT) {
			String msg = "No Lyrics found for this track";
			sender.accept(EmbedUtils.getErrorEmbed(msg, guildId).build());
		} else if (statusCode == HttpStatus.SC_NOT_FOUND) {
			String msg = "No track playing to fetch lyrics for";
			sender.accept(EmbedUtils.getErrorEmbed(msg, guildId).build());
		} else {
			String msg = "Error on fetching lyrics. status: " + statusCode;
			sender.accept(EmbedUtils.getErrorEmbed(msg, guildId).build());
		}
	}
}
