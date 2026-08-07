/* (C)2026 */
package de.klassenserver7b.k7bot.manage;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import dev.arbjerg.lavalink.client.Helpers;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.LavalinkNode;
import dev.arbjerg.lavalink.client.NodeOptions;
import dev.arbjerg.lavalink.client.event.*;
import dev.arbjerg.lavalink.client.loadbalancing.RegionGroup;
import dev.arbjerg.lavalink.client.loadbalancing.builtin.VoiceRegionPenaltyProvider;

public class LavaLinkManager {

	public static final Map<String, String> SESSION_IDS = new ConcurrentHashMap<>();
	private static final Logger log = LoggerFactory.getLogger(LavaLinkManager.class);

	public static LavalinkClient initialize(String token) {

		LavalinkClient client = new LavalinkClient(Helpers.getUserIdFromToken(token));
		client.getLoadBalancer().addPenaltyProvider(new VoiceRegionPenaltyProvider());

		registerLavalinkListeners(client);
		List<NodeOptions> nodeOptions = LavaLinkManager.parseNodesJson();

		if (!(nodeOptions == null || nodeOptions.isEmpty())) {
			registerLavalinkNodes(client, nodeOptions);
		}

		return client;
	}

	@Nullable
	private static List<NodeOptions> parseNodesJson() {
		Gson gson = new GsonBuilder().registerTypeAdapter(NodeOptions.class, new NodeOptionsDeserializer()).create();

		try (FileReader reader = new FileReader("resources/lavalink-nodes.json")) {

			return gson.fromJson(reader, new TypeToken<List<NodeOptions>>() {
			}.getType());
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	private static void registerLavalinkNodes(LavalinkClient client, Collection<NodeOptions> nodeOptions) {
		nodeOptions.forEach(nodeOption -> {
			LavalinkNode node = client.addNode(nodeOption);
			node.enableResuming(Duration.ofSeconds(60)).subscribe();
			log.info("registered node: {} - {}", node.getName(), node.getBaseUri());
		});
	}

	private static void registerLavalinkListeners(LavalinkClient client) {
		client.on(ReadyEvent.class).subscribe((event) -> {
			final LavalinkNode node = event.getNode();
			SESSION_IDS.put(node.getName(), event.getSessionId());

			log.info("Node {} is ready, session id is {}!", node.getName(), event.getSessionId());
		});

		client.on(StatsEvent.class).subscribe((event) -> {
			final LavalinkNode node = event.getNode();

			log.debug("Node {} has stats, current players: {}{}", node.getName(), event.getPlayingPlayers(),
					event.getPlayers());
		});

		client.on(TrackStartEvent.class).subscribe((event) -> {
			log.info("TrackStartEvent on node {} for guild {}", event.getNode().getName(), event.getGuildId());
			de.klassenserver7b.k7bot.audio.GuildAudioManager gam = de.klassenserver7b.k7bot.K7Bot.getInstance()
					.getAudioManager().getGuildAudioManager(event.getGuildId());
			if (gam != null) {
				gam.getTrackScheduler().onTrackStart(event.getTrack());
			}
		});

		client.on(TrackEndEvent.class).subscribe((event) -> {
			log.info("TrackEndEvent on node {} for guild {}", event.getNode().getName(), event.getGuildId());
			de.klassenserver7b.k7bot.audio.GuildAudioManager gam = de.klassenserver7b.k7bot.K7Bot.getInstance()
					.getAudioManager().getGuildAudioManager(event.getGuildId());
			if (gam != null) {
				gam.getTrackScheduler().onTrackEnd(event.getTrack(), event.getEndReason());
			}
		});

		client.on(WebSocketClosedEvent.class).subscribe((event) -> {
			LavalinkNode node = event.getNode();
			log.info("WebSocketClosedEvent on node {} for guild {} with reason {} - trying to reconnect in 30s",
					node.getName(), event.getGuildId(), event.getReason());

		});
	}

	public static class NodeOptionsDeserializer implements JsonDeserializer<NodeOptions> {
		@Override
		public NodeOptions deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			JsonObject obj = json.getAsJsonObject();
			NodeOptions.Builder builder = new NodeOptions.Builder();
			builder.setName(obj.get("name").getAsString());
			builder.setServerUri(obj.get("uri").getAsString());
			builder.setPassword(obj.get("password").getAsString());
			builder.setHttpTimeout(obj.get("timeout").getAsLong());
			builder.setRegionFilter(RegionGroup.INSTANCE.valueOf(obj.get("region").getAsString()));
			return builder.build();
		}
	}

	public record UserData(long requester) {
	}
}
