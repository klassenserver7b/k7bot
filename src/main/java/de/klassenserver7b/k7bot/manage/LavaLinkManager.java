/* (C)2026 */
package de.klassenserver7b.k7bot.manage;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import dev.arbjerg.lavalink.client.Helpers;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.LavalinkNode;
import dev.arbjerg.lavalink.client.NodeOptions;
import dev.arbjerg.lavalink.client.event.ReadyEvent;
import dev.arbjerg.lavalink.client.event.StatsEvent;
import dev.arbjerg.lavalink.client.event.TrackEndEvent;
import dev.arbjerg.lavalink.client.event.TrackStartEvent;
import dev.arbjerg.lavalink.client.loadbalancing.RegionGroup;
import dev.arbjerg.lavalink.client.loadbalancing.builtin.VoiceRegionPenaltyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LavaLinkManager {

	private static final Logger log = LoggerFactory.getLogger(LavaLinkManager.class);
	public static final Map<String, String> SESSION_IDS = new ConcurrentHashMap<>();

	public static LavalinkClient initialize(String token) {

		LavalinkClient client = new LavalinkClient(Helpers.getUserIdFromToken(token));
		client.getLoadBalancer().addPenaltyProvider(new VoiceRegionPenaltyProvider());

		registerLavalinkListeners(client);
		registerLavalinkNodes(client);

		return client;
	}

	private static boolean registerLavalinkNodes(LavalinkClient client) {

		Gson gson = new GsonBuilder().registerTypeAdapter(NodeOptions.class, new NodeOptionsDeserializer()).create();

		try (FileReader reader = new FileReader("resources/lavalink-nodes.json")) {

			List<NodeOptions> nodeOptions = gson.fromJson(reader, new TypeToken<List<NodeOptions>>() {
			}.getType());

			List<LavalinkNode> nodes = nodeOptions.stream().map(client::addNode).toList();

			nodes.forEach((node) -> log.info("registered node: {} - {}", node.getName(), node.getBaseUri()));
			return true;

		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return false;
		}
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
