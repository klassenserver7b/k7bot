package de.klassenserver7b.k7bot.music.asms;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import de.klassenserver7b.k7bot.K7Bot;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JellyfinAudioSourceManager extends HttpAudioSourceManager {

    private static final String SERVER_URL = K7Bot.getInstance().getPropertiesManager().getProperty("jellyfin-url");
    private static final String API_KEY = K7Bot.getInstance().getPropertiesManager().getProperty("jellyfin-api-key");
    private static final String JELLYFIN_URL_REGEX = String.format("%s/web/#/details\\?id=(.+)&.*$", SERVER_URL.replaceAll("\\.", "\\."));
    private static final Pattern JEYLLYFIN_URL_PATTERN = Pattern.compile(JELLYFIN_URL_REGEX);
    private final CloseableHttpClient httpClient;

    public JellyfinAudioSourceManager() {
        super();
        this.httpClient = HttpClientBuilder.create().useSystemProperties().build();
    }

    /**
     * @return The name of the source manager, used for logging and identification.
     */
    @Override
    public String getSourceName() {
        return "jellyfin";
    }

    /**
     * @param audioPlayerManager
     * @param audioReference
     * @return AudioItem if the reference is valid and Jellyfin API is enabled, null otherwise.
     */
    @Override
    public AudioItem loadItem(AudioPlayerManager audioPlayerManager, AudioReference audioReference) {

        if (audioReference == null || audioReference.identifier == null || !K7Bot.getInstance().getPropertiesManager().isApiEnabled("jellyfin")) {
            return null;
        }

        String query = audioReference.identifier;

        if (query.startsWith("jfsearch: ")) {
            return searchJellyfin(audioPlayerManager, query.substring(10).trim());
        }

        Matcher matcher = JEYLLYFIN_URL_PATTERN.matcher(query);
        if (matcher.matches()) {
            String itemId = matcher.group(1);
            return loadItemById(audioPlayerManager, itemId, null);
        }

        return null;
    }

    protected AudioItem searchJellyfin(AudioPlayerManager audioPlayerManager, String query) {
        K7Bot.getInstance().getMainLogger().debug("Searching Jellyfin for query: {}", query);

        try {
            BasicClassicHttpRequest request = new BasicClassicHttpRequest("GET", SERVER_URL + "/Search/Hints?mediaTypes=Audio&includeItemTypes=Audio&searchTerm=" + URLEncoder.encode(query));
            request.addHeader("X-MediaBrowser-Token", API_KEY);

            String body = httpClient.execute(request, new BasicHttpClientResponseHandler());

            K7Bot.getInstance().getMainLogger().debug("Jellyfin Query response: {}", body);

            JsonObject mainObj = JsonParser.parseString(body).getAsJsonObject();
            JsonArray searchArray = mainObj.get("SearchHints").getAsJsonArray();

            if (searchArray.size() == 0) {
                K7Bot.getInstance().getMainLogger().info("No results found for Jellyfin query: {}", query);
                return null;
            }

            JsonObject firstResult = searchArray.get(0).getAsJsonObject();

            String id = firstResult.get("Id").getAsString();
            String name = firstResult.get("Name").getAsString();


            return loadItemById(audioPlayerManager, id, name);

        } catch (IOException | IllegalStateException e) {
            K7Bot.getInstance().getMainLogger().warn("Error while searching Jellyfin for query: {}", query, e);
            return null;
        }
    }

    protected AudioItem loadItemById(AudioPlayerManager audioPlayerManager, String itemId, String itemName) {
        K7Bot.getInstance().getMainLogger().debug("Loading Jellyfin item by ID: {}", itemId);

        if (itemName == null || itemName.isEmpty()) {
            itemName = getNameById(itemId);
        }
        String itemUrl = SERVER_URL + "/Items/" + itemId + "/Download?api_key=" + API_KEY;
        return super.loadItem(audioPlayerManager, new AudioReference(itemUrl, itemName));
    }

    protected String getNameById(String itemId) {
        try {
            BasicClassicHttpRequest request = new BasicClassicHttpRequest("GET", HttpHost.create(SERVER_URL), "/Items/?ids" + itemId);
            request.addHeader("X-MediaBrowser-Token", API_KEY);
            String body = httpClient.execute(request, new BasicHttpClientResponseHandler());

            JsonObject mainObj = JsonParser.parseString(body).getAsJsonObject();
            return mainObj.get("Items").getAsJsonArray().get(0).getAsJsonObject().get("Name").getAsString();

        } catch (IOException | URISyntaxException | IllegalStateException e) {
            K7Bot.getInstance().getMainLogger().warn("Error while loading item details for id : {}", itemId, e);
            return "Unknown Item";
        }
    }
}
