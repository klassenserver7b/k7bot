package de.klassenserver7b.k7bot.music.utilities;

import de.klassenserver7b.k7bot.sql.LiteSQL;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

public class ChartList {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * @param guild    <br>
     *                 The specific {@link Guild} for which you want the charts.
     * @param time     <br>
     *                 The {@link java.lang.Long amount} of (days/weeks/months/etc.)
     *                 you want the charts for.
     * @param timeunit <br>
     *                 The specification which {@link ChronoUnit Unit} is used for
     *                 time.
     * @return A {@link HashMap HashHashMap} containing the selected Charts.
     * "songname - author" Strings are the {@link java.lang.String keys} and
     * the "timesplayed" are the {@link java.lang.Long values}.
     */
    public HashMap<String, Long> getcharts(Guild guild, Long time, ChronoUnit timeunit) {

        return listcharts(guild.getIdLong(), time, timeunit);
    }

    /**
     * @param guildid  <br>
     *                 The specific Id of the {@link Guild} for which you want the
     *                 charts.
     * @param time     <br>
     *                 The {@link java.lang.Long amount} of (days/weeks/months/etc.)
     *                 you want the charts for.
     * @param timeunit <br>
     *                 The specification which {@link ChronoUnit Unit} is used for
     *                 time.
     * @return A {@link HashMap HashHashMap} containing the selected Charts.
     * "songname - author" Strings are the {@link java.lang.String keys} and
     * the "timesplayed" are the {@link java.lang.Long values}.
     */
    public HashMap<String, Long> getcharts(Long guildid, Long time, ChronoUnit timeunit) {

        return listcharts(guildid, time, timeunit);
    }

    /**
     * @param guild <br>
     *              The specific {@link Guild} for which you want the charts.
     * @return A {@link HashMap HashHashMap} containing the selected Charts.
     * "songname - author" Strings are the {@link java.lang.String keys} and
     * the "timesplayed" are the {@link java.lang.Long values}.
     */
    public HashMap<String, Long> getcharts(Guild guild) {

        return listcharts(guild.getIdLong(), null, null);
    }

    /**
     * @param guildid <br>
     *                The specific Id of the {@link Guild} for which you want the
     *                charts.
     * @return A {@link HashMap HashHashMap} containing the selected Charts.
     * "songname - author" Strings are the {@link java.lang.String keys} and
     * the "timesplayed" are the {@link java.lang.Long values}.
     */
    public HashMap<String, Long> getcharts(Long guildid) {

        return listcharts(guildid, null, null);
    }

    /**
     * @param time     <br>
     *                 The {@link java.lang.Long amount} of (days/weeks/months/etc.)
     *                 you want the charts for.
     * @param timeunit <br>
     *                 The specification which {@link ChronoUnit Unit} is used for
     *                 time.
     * @return A {@link HashMap HashHashMap} containing the selected Charts.
     * "songname - author" Strings are the {@link java.lang.String keys} and
     * the "timesplayed" are the {@link java.lang.Long values}.
     */
    public HashMap<String, Long> getcharts(Long time, ChronoUnit timeunit) {

        return listcharts(null, time, timeunit);
    }

    /**
     * @return A {@link HashMap HashHashMap} containing the charts for all servers
     * over all time. "songname - author" Strings are the
     * {@link java.lang.String keys} and the "timesplayed" are the
     * {@link java.lang.Long values}.
     */
    public HashMap<String, Long> getcharts() {

        return listcharts(null, null, null);
    }

    /**
     * @param guildid  <br>
     *                 The {@link java.lang.Long Id} of the specific guild whose
     *                 charts the method should fetch or null if you want the charts
     *                 for every guild.
     * @param time     <br>
     *                 The {@link java.lang.Long amount} of (days/weeks/months/etc.)
     *                 the method should search the charts for.
     * @param timeunit <br>
     *                 The specification which {@link ChronoUnit Unit} is used for
     *                 time.
     * @return A {@link HashMap HashHashMap} containing the charts for all servers
     * over all time. "songname - author" Strings are the
     * {@link java.lang.String keys} and the "timesplayed" are the
     * {@link java.lang.Long values}.
     */
    private HashMap<String, Long> listcharts(Long guildid, Long time, ChronoUnit timeunit) {

        String guildselect = "";
        String dateselect = "";

        if (guildid != null) {
            guildselect = " WHERE guildId=" + guildid;
        }

        if (time != null && timeunit != null) {

            if (guildid == null) {
                dateselect = " WHERE timestamp>="
                        + OffsetDateTime.now().minus(time, timeunit).format(DateTimeFormatter.BASIC_ISO_DATE);
            } else {
                dateselect = " AND timestamp>="
                        + OffsetDateTime.now().minus(time, timeunit).format(DateTimeFormatter.BASIC_ISO_DATE);
            }

        }

        HashMap<String, Long> chartslist = new HashMap<>();

        try (ResultSet set = LiteSQL
                .onQuery("SELECT * FROM musiclogs" + guildselect + dateselect + " ORDER BY timestamp DESC;")) {

            if (set != null) {

                while (set.next()) {

                    String title = set.getString("songname");

                    String author = set.getString("songauthor");

                    SongJson js = new SongDataUtils().parseOffline(title, author);

                    String songhead = js.getAuthorString() + " - " + js.getTitle();

                    if (chartslist.containsKey(songhead)) {
                        chartslist.put(songhead, chartslist.get(songhead) + 1);
                    } else {
                        chartslist.put(songhead, 1L);
                    }

                }

                return chartslist;

            }

            return null;

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            return null;
        }

    }

}
