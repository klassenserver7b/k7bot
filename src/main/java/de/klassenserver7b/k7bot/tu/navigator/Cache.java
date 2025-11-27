package de.klassenserver7b.k7bot.tu.navigator;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class Cache {
    record Entry(String url, String hash, JsonElement value, LocalDateTime time) {
    }

    private final ArrayList<Entry> entries;
    private final CloseableHttpClient httpClient;

    public Cache() {
        entries = new ArrayList<>();
        httpClient = HttpClients.createDefault();
    }

    public void shutdown() {
        entries.clear();

        try {
            httpClient.close();
        } catch (IOException ignored) {
        }
    }

    public JsonElement request(String url) {
        Entry cacheEntry = entries.stream().filter(entry -> entry.url.equals(url)).findAny().orElse(null);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        if (cacheEntry != null && ChronoUnit.HOURS.between(now, cacheEntry.time) <= 1)
            return cacheEntry.value;

        String hash = requestHash(url);
        if (hash == null) return requestInner(url);

        if (cacheEntry != null && cacheEntry.hash.equals(hash)) {
            Entry newEntry = new Entry(url, hash, cacheEntry.value, now);
            entries.remove(cacheEntry);
            entries.add(newEntry);

            return cacheEntry.value;
        }

        JsonElement value = requestInner(url);
        if (value == null) {
            Entry newEntry = new Entry(url, "", null, now);
            entries.remove(cacheEntry);
            entries.add(newEntry);

            return null;
        }

        Entry newEntry = new Entry(url, hash, value, now);
        entries.remove(cacheEntry);
        entries.add(newEntry);

        return value;
    }

    private String requestHash(String url) {
        String hashUrl;
        if (url.endsWith("/all"))
            hashUrl = url.replaceFirst("/all$", "/hash");
        else hashUrl = url + "/hash";

        JsonElement response = requestInner(hashUrl);
        if (response == null || !response.isJsonObject() || !response.getAsJsonObject().has("hash"))
            return null;

        return response.getAsJsonObject().get("hash").getAsString();
    }

    private JsonElement requestInner(String url) {
        HttpGet request = new HttpGet(url);

        try {
            return httpClient.execute(request, response -> {
                String content = EntityUtils.toString(response.getEntity());
                try {
                    return JsonParser.parseString(content);
                } catch (JsonSyntaxException e) {
                    return JsonNull.INSTANCE;
                }
            });
        } catch (IOException e) {
            return null;
        }
    }
}
