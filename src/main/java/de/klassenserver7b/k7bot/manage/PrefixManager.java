/**
 *
 */
package de.klassenserver7b.k7bot.manage;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.sql.LiteSQL;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * @author Klassenserver7b
 */
public class PrefixManager {

    private final HashMap<Long, String> prefixl;
    private final Logger log;

    public PrefixManager() {
        this.prefixl = new HashMap<>();
        log = LoggerFactory.getLogger(this.getClass());
        reload();
    }

    protected void reload() {

        try (ResultSet set = LiteSQL.onQuery("SELECT * FROM botutil;")) {

            while (set.next()) {
                long guildid = set.getLong("guildId");
                String prefix = set.getString("prefix");

                if (guildid == 0) {
                    continue;
                }

                assert prefix != null; // SET as NOT NULL AND DEFAULT '-' in DB

                prefixl.put(guildid, prefix);
            }

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }

        K7Bot.getInstance().getShardManager().getShards().forEach(jda -> {

            for (Guild g : jda.getGuilds()) {

                if (!prefixl.containsKey(g.getIdLong())) {
                    try {
                        setInternalPrefix(g.getIdLong(), "-");
                    } catch (IllegalArgumentException e) {
                        log.warn(e.getMessage(), e);
                    }
                }

            }

        });

    }

    /**
     * @param guildid   the guildid to set the prefix for
     * @param newprefix the new prefix
     * @throws IllegalArgumentException if newprefix is null or empty
     */
    protected void setInternalPrefix(long guildid, @SuppressWarnings("SameParameterValue") String newprefix) throws IllegalArgumentException {

        if (newprefix == null || newprefix.isBlank()) {
            throw new IllegalArgumentException("can't use a empty prefix - guildid: " + guildid,
                    new Throwable().fillInStackTrace());
        }

        applyPrefix(guildid, newprefix);

    }

    /**
     * @param guildid   the guildid to set the prefix for
     * @param newprefix the new prefix
     * @throws IllegalArgumentException if newprefix is null or empty
     */
    public void setPrefix(long guildid, String newprefix) throws IllegalArgumentException {

        reload();

        if (newprefix == null || newprefix.isBlank()) {
            throw new IllegalArgumentException("can't use a empty prefix", new Throwable().fillInStackTrace());
        }

        applyPrefix(guildid, newprefix);

    }

    protected void applyPrefix(long guildid, String prefix) {

        LiteSQL.onUpdate("INSERT OR REPLACE INTO botutil(guildId, prefix) VALUES(?, ?)", guildid, prefix);

        prefixl.put(guildid, prefix);
    }

    public String getPrefix(Guild guild) {
        return this.prefixl.get(guild.getIdLong());
    }

    public String getPrefix(Long guildid) {
        return this.prefixl.computeIfAbsent(guildid, k -> {
            try {
                setInternalPrefix(k, "-");
            } catch (IllegalArgumentException e) {
                log.warn(e.getMessage(), e);
            }
            return "-";
        });
    }

}
