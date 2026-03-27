package de.klassenserver7b.k7bot.audio;

import java.util.HashMap;

public class AudioManager {

    private HashMap<Long, GuildAudioManager> guildAudioManagers;

    public AudioManager() {
        guildAudioManagers = new HashMap<>();
    }

    public GuildAudioManager getGuildAudioManager(long guildId) {
        return this.guildAudioManagers.computeIfAbsent(guildId, GuildAudioManager::new);
    }

    public boolean hasGuildAudioManager(long guildId) {
        return this.guildAudioManagers.containsKey(guildId);
    }

    public boolean deleteGuildAudioManager(long guildId){
        return this.guildAudioManagers.remove(guildId) != null;
    }

}
