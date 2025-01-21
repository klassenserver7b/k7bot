package de.klassenserver7b.k7bot.manage;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.sql.LiteSQL;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

public class SystemNotificationChannelManager {

    private final ConcurrentHashMap<Guild, GuildMessageChannel> systemchannellist;
    private final Logger log;

    public SystemNotificationChannelManager() {

        systemchannellist = new ConcurrentHashMap<>();
        log = LoggerFactory.getLogger(this.getClass());
        reload();
    }

    private void reload() {

        try (ResultSet set = LiteSQL.onQuery("SELECT * FROM botutil;")) {

            while (set.next()) {
                long guildid = set.getLong("guildId");
                long systemchannel = set.getLong("syschannelId");

                if (guildid == 0) {
                    continue;
                }

                Guild g = K7Bot.getInstance().getShardManager().getGuildById(guildid);

                if (g == null) {
                    continue;
                }

                GuildChannel gchan = g.getGuildChannelById(systemchannel);

                if (!(gchan instanceof GuildMessageChannel)) {
                    systemchannellist.put(g, g.getSystemChannel());
                    LiteSQL.onUpdate("UPDATE botutil SET syschannelId = ?;", g.getSystemChannel().getIdLong());
                }

                assert gchan instanceof GuildMessageChannel;
                GuildMessageChannel chan = (GuildMessageChannel) gchan;

                systemchannellist.put(g, chan);

            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }

    }

    /**
     * Puts the given {@link GuildMessageChannel SystemChannel} into the Hashmap
     * keyed by his {@link Guild}.
     *
     * @param channel <br>
     *                The {@link GuildMessageChannel SystemChannel} wich u want to
     *                use in this {@link Guild}.
     */
    public void insertChannel(GuildMessageChannel channel) {

        reload();

        Guild guild = channel.getGuild();

        if (systemchannellist.containsKey(guild)) {

            LiteSQL.onUpdate("UPDATE botutil SET syschannelId = ? WHERE guildId = ?;", channel.getIdLong(),
                    guild.getIdLong());

        } else {

            LiteSQL.onUpdate("INSERT INTO botutil(guildId, syschannelId) VALUES(?, ?);", guild.getIdLong(),
                    channel.getIdLong());

        }

        systemchannellist.put(guild, channel);

    }

    /**
     * @param guild <br>
     *              The {@link Guild} for which you want the SystemChannel.
     * @return The {@link GuildMessageChannel SystemChannel} for the Guild or
     * {@code null} if no channel is listed.
     */
    @Nullable
    public GuildMessageChannel getSysChannel(@Nonnull Guild guild) {

        if (systemchannellist.get(guild) == null) {
            return guild.getSystemChannel();
        }

        return systemchannellist.get(guild);
    }

    /**
     * @param guildId <br>
     *                The Id of the {@link Guild} for which you want the
     *                SystemChannel.
     * @return The {@link GuildMessageChannel SystemChannel} for the Guild or
     * {@code null} if no channel is listed.
     */
    @Nullable
    @SuppressWarnings("unused")
    public GuildMessageChannel getSysChannel(@Nonnull Long guildId) throws NullPointerException {

        Guild guild = K7Bot.getInstance().getShardManager().getGuildById(guildId);

        return systemchannellist.get(guild);
    }

    /**
     * Only used if you want the full ConcurrentHashMap! If you want only one
     * {@link GuildMessageChannel SystemChannel} please use
     * {@link SystemNotificationChannelManager#getSysChannel(Guild)}
     *
     * @return The current HashMap of SystemChannels which is used by the Bot.
     */
    @SuppressWarnings("unused")
    public ConcurrentHashMap<Guild, GuildMessageChannel> getHashMap() {
        reload();
        return systemchannellist;
    }
}
