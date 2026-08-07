/* (C)2026 */
package de.klassenserver7b.k7bot.audio;

import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public class AudioManager {

	private final ConcurrentHashMap<Long, GuildAudioManager> guildAudioManagers;

	public AudioManager() {
		guildAudioManagers = new ConcurrentHashMap<>();
	}

	public GuildAudioManager getGuildAudioManager(long guildId) {
		return this.guildAudioManagers.computeIfAbsent(guildId, GuildAudioManager::new);
	}

	public boolean hasGuildAudioManager(long guildId) {
		return this.guildAudioManagers.containsKey(guildId);
	}

	public boolean deleteGuildAudioManager(long guildId) {
		return this.guildAudioManagers.remove(guildId) != null;
	}

}
