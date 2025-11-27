package de.klassenserver7b.k7bot.music.utilities;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.music.lavaplayer.MusicController;

import java.util.concurrent.ConcurrentHashMap;

public class AudioPlayerUtil {

    private final ConcurrentHashMap<Long, MusicController> controller = new ConcurrentHashMap<>();

    public static final int STANDARDVOLUME = 10;

    public MusicController getController(long guildid) {
        MusicController mc;

        if (this.controller.containsKey(guildid)) {
            mc = this.controller.get(guildid);
        } else {

            mc = new MusicController(K7Bot.getInstance().getShardManager().getGuildById(guildid));

            this.controller.put(guildid, mc);
        }

        return mc;
    }

    public long getGuildbyPlayerHash(int hash) {
        for (MusicController controller : this.controller.values()) {
            if (controller.getPlayer().hashCode() == hash) {
                return controller.getGuild().getIdLong();
            }
        }
        return -1L;
    }

    public void stopAllTracks() {
        controller.values().forEach(contr -> contr.getPlayer().stopTrack());
    }
}