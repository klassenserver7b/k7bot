/**
 *
 */
package de.klassenserver7b.k7bot.util;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.logging.LoggingFilter;
import de.klassenserver7b.k7bot.sql.LiteSQL;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumSet;

/**
 * @author Klassenserver7b
 */
public class StatsCategoryUtil {

    private static final Logger log = LoggerFactory.getLogger(StatsCategoryUtil.class);

    public static void fillCategory(Category cat) {
        try (AutoCloseable _ = LoggingFilter.getInstance().blockEventExecution()) {
            VoiceChannel vc = cat.createVoiceChannel("🟢 Bot Online").complete();
            LoggingFilter.getInstance().getLoggingBlocker().block(vc.getIdLong());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }


        cat.getManager()
                .putPermissionOverride(cat.getGuild().getPublicRole(), null, EnumSet.of(Permission.VOICE_CONNECT))
                .complete();

    }

    public static void onStartup() {
        K7Bot.getInstance().getShardManager().getGuilds().forEach(guild -> {
            try (ResultSet set = LiteSQL.onQuery("SELECT categoryId FROM statschannels WHERE guildId = ?;",
                    guild.getIdLong())) {

                if (set.next()) {
                    long catid = set.getLong("categoryId");
                    Category cat = guild.getCategoryById(catid);

                    cat.getChannels().forEach(chan -> {
                        LoggingFilter.getInstance().getLoggingBlocker().block(chan.getIdLong());
                        chan.delete().complete();

                    });

                    fillCategory(cat);

                }
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }

        });
    }

    public static void onShutdown() {
        K7Bot.getInstance().getShardManager().getGuilds().forEach(guild -> {

            try (ResultSet set = LiteSQL.onQuery("SELECT categoryId FROM statschannels WHERE guildId = ?;",
                    guild.getIdLong())) {

                if (set.next()) {
                    long catid = set.getLong("categoryId");
                    Category cat = guild.getCategoryById(catid);

                    try (AutoCloseable ignored = LoggingFilter.getInstance().blockEventExecution()) {

                        cat.getChannels().forEach(chan -> {
                            LoggingFilter.getInstance().getLoggingBlocker().block(chan.getIdLong());
                            chan.delete().complete();

                        });

                        VoiceChannel vc = cat.createVoiceChannel("🔴 Bot offline").complete();
                        LoggingFilter.getInstance().getLoggingBlocker().block(vc.getIdLong());
                    }


                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
    }

}
