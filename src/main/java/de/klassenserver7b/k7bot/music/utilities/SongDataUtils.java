package de.klassenserver7b.k7bot.music.utilities;

import com.vdurmont.emoji.EmojiParser;
import de.klassenserver7b.k7bot.util.customapis.DiscogsAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SongDataUtils {

    private final DiscogsAPI api;
    private final Logger log;

    public SongDataUtils() {
        api = new DiscogsAPI();
        log = LoggerFactory.getLogger(this.getClass());
    }

    public SongDataUtils(DiscogsAPI api) {
        this.api = api;
        log = LoggerFactory.getLogger(this.getClass());
    }

    public String stripSongTitle(String title) {

        String[] split = title.trim().split(" - ");

        String strippedtitle;

        if (split.length <= 1) {
            strippedtitle = split[0];
        } else {
            strippedtitle = split[0] + " - " + split[1];
        }

        strippedtitle = EmojiParser.removeAllEmojis(strippedtitle);

        strippedtitle = strippedtitle.replaceAll("\\|", "");

        strippedtitle = strippedtitle.replaceAll("( )?([(\\[])((?!(sped|slow|speed|reverb|nightcore)).)*([)\\]])/i", "");

        return strippedtitle.trim();
    }

    public String stripSongAuthor(String author) {

        String strippedauthor = author.trim();
        strippedauthor = strippedauthor.replaceAll(" - Thema", "");
        strippedauthor = strippedauthor.replaceAll(" - ", "");
        strippedauthor = strippedauthor.replaceAll("-", "");

        return strippedauthor;
    }

    public SongJson parseYtTitle(String yttitle, String channel) {

        String stripedtitle = stripSongTitle(yttitle);
        String author = stripSongAuthor(channel);

        log.info("YT TitleParseRequest - title={}&channel={}", stripedtitle, author);

        if (!api.isApiEnabled()) {
            return parseOffline(stripedtitle, author);
        }
        return parseViaDiscogs(stripedtitle, author);

    }

    public SongJson parseViaDiscogs(String title, String channel) {

        log.debug("Parsing Online");
        String newtitle = title;

        if (!titleContainsAuthor(title)) {
            newtitle = String.format("%s - %s", channel, title);
        }

        try {

            SongJson json = api.getFilteredSongJson(newtitle);

            if (json == null) {

                log.warn("Online Parse Error - Parsing Offline");
                return parseOffline(title, channel);

            }

            log.info("Successfully Parsed Online");
            return json;

        } catch (IllegalArgumentException e) {
            return null;
        }

    }

    public boolean titleContainsAuthor(String title) {

        String[] parts = title.split(" - ");

        return (parts.length > 1);

    }

    public SongJson parseOffline(String title, String channel) {
        log.debug("Parsing Offline");

        if (titleContainsAuthor(title)) {
            log.debug("Parsing YT Title Offline - Author in title");
            return parseTitleWithAuthor(title);
        }
        log.debug("Parsing YT Title Offline - Author not in title");
        return parseTitleAndAuthor(title, channel);

    }

    protected SongJson parseTitleWithAuthor(String title) {

        String[] parts = title.split(" - ");
        return parseTitleAndAuthor(parts[1], parts[0]);

    }

    protected SongJson parseTitleAndAuthor(String title, String channel) {
        try {
            SongJson json = SongJson.ofUnvalidated(title, channel, "0",
                    "https://www.discogs.com/de/search/?q=" + title + "+" + channel, "");
            log.debug("Successfully Parsed Offline");
            return json;
        } catch (IllegalArgumentException e) {
            log.warn("Couldn't Parse");
            return null;
        }

    }


}
