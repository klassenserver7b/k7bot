package de.klassenserver7b.k7bot.util.customapis;

import com.google.gson.*;
import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.subscriptions.types.SubscriptionTarget;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import de.klassenserver7b.k7bot.util.InternalStatusCodes;
import de.klassenserver7b.k7bot.util.customapis.types.LoopedEvent;
import net.dv8tion.jda.annotations.ForRemoval;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.EntityBuilder;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Deprecated
@ForRemoval(deadline = "1.24.0")
public class VVOInteractions implements LoopedEvent {

    private final Logger log;

    public VVOInteractions() {
        log = LoggerFactory.getLogger(getClass());
    }

    @Override
    public int checkforUpdates() {

        JsonElement plan = downloadPlan(LocalDateTime.now());
        if (plan == null || plan.isJsonNull()) {
            return InternalStatusCodes.FAILURE;
        }

        List<String> lines = getAllFieldsFromData(plan.getAsJsonObject());

        if (lines == null || lines.isEmpty()) {
            return InternalStatusCodes.FAILURE;
        }

        try (MessageCreateData data = getMessage(getEmbed(lines))) {

            K7Bot.getInstance().getSubscriptionManager()
                    .provideSubscriptionNotification(SubscriptionTarget.DVB, data);
        }

        return InternalStatusCodes.SUCCESS;

    }

    private MessageCreateData getMessage(MessageEmbed embed) {
        MessageCreateBuilder dataBuild = new MessageCreateBuilder();
        dataBuild.setEmbeds(embed);
        return dataBuild.build();
    }

    private MessageEmbed getEmbed(List<String> fields) {

        StringBuilder builder = new StringBuilder();

        for (String f : fields) {
            builder.append(f);
            builder.append("\n\n");
        }

        return EmbedUtils.getSuccessEmbed(builder).setTitle("Die nächsten Abfahrten an der Mosenstraße").build();
    }

    private String getLineStringFromData(JsonElement departure) {
        JsonObject departobj = departure.getAsJsonObject();
        String lineName = departobj.get("LineName").getAsString();
        String direction = departobj.get("Direction").getAsString();

        Entry<String, Long> times = getDepartureTime(departobj);

        if (times == null) {
            return null;
        }

        String departureTime = times.getKey();
        Long delay = times.getValue();

        String delaystr = "";

        if (delay > 0) {
            delaystr = "(+" + delay + ")";
        } else if (delay < 0) {
            delaystr = "(-" + delay + ")";
        }

        return lineName + " " + direction + " | " + departureTime + " " + delaystr;
    }

    private List<String> getAllFieldsFromData(JsonObject object) {
        if (object == null || object.isJsonNull()) {
            return null;
        }
        JsonArray departures = object.get("Departures").getAsJsonArray();
        ArrayList<String> ret = new ArrayList<>();
        for (JsonElement depart : departures) {
            String f = getLineStringFromData(depart);
            if (f == null) {
                ret.add(null);
            }
        }
        return ret;
    }

    private Entry<String, Long> getDepartureTime(JsonObject depart) {

        if (depart == null || depart.isJsonNull()) {
            return null;
        }

        Pattern timePattern = Pattern.compile("\\d+");

        JsonElement realTimeElem = depart.get("RealTime");
        JsonElement sheduledTimeElem = depart.get("ScheduledTime");

        if (realTimeElem == null || realTimeElem.isJsonNull()) {
            return null;
        }

        Matcher departureMatcher = timePattern.matcher(realTimeElem.getAsString());
        Matcher delayMatcher = timePattern.matcher(
                ((sheduledTimeElem == null || sheduledTimeElem.isJsonNull()) ? "" : sheduledTimeElem.getAsString()));

        String departure;
        long delay = 0L;

        if (!departureMatcher.find()) {
            return null;
        }

        LocalDateTime theoreticdeparTime = LocalDateTime.ofEpochSecond(Long.parseLong(departureMatcher.group()) / 1000, 0,
                ZoneOffset.ofHours(1));
        Duration timediff = Duration.between(LocalDateTime.now(), theoreticdeparTime);
        long departureTime = timediff.getSeconds() / 60;
        departure = theoreticdeparTime.toLocalTime().toString();

        if (delayMatcher.find()) {
            LocalDateTime delaytime = LocalDateTime.ofEpochSecond(Long.parseLong(delayMatcher.group()) / 1000, 0,
                    ZoneOffset.ofHours(1));
            Duration delaydur = Duration.between(LocalDateTime.now(), delaytime);
            long delaycheck = delaydur.getSeconds() / 60;
            if (delaycheck != departureTime) {
                delay = departureTime - delaycheck;

            }
        }

        return Map.entry(departure, delay);
    }

    public JsonElement downloadPlan(LocalDateTime time) {

        HttpPost request = new HttpPost("https://webapi.vvo-online.de/dm");
        EntityBuilder entityBuilder = getEntityBuilder(time);

        try (HttpEntity entity = entityBuilder.build(); CloseableHttpClient httpClient = HttpClients.createDefault()) {

            request.setEntity(entity);

            String content = httpClient.execute(request, new BasicHttpClientResponseHandler());
            return JsonParser.parseString(content);

        } catch (IOException | JsonParseException e) {
            log.error(e.getMessage(), e);
            return null;
        }

    }

    private static @NotNull EntityBuilder getEntityBuilder(LocalDateTime time) {
        JsonObject requestData = new JsonObject();
        requestData.addProperty("format", "json");
        requestData.addProperty("stopid", 33000063);
        requestData.addProperty("time", time.toString());
        requestData.addProperty("isarrival", false);
        requestData.addProperty("limit", 12);
        requestData.addProperty("shorttermchanges", true);
        requestData.addProperty("mentzonly", false);
        EntityBuilder entityBuilder = EntityBuilder.create();
        entityBuilder.setContentType(ContentType.APPLICATION_JSON);
        entityBuilder.setText(requestData.toString());
        return entityBuilder;
    }

    @Override
    public boolean restart() {
        // Nothing to do here
        return true;
    }

    @Override
    public void shutdown() {
        // Nothing to do here
    }

    @Override
    public boolean isAvailable() {

        HttpGet request = new HttpGet("https://webapi.vvo-online.de/");

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            httpClient.execute(request, new BasicHttpClientResponseHandler());

        } catch (IOException e) {
            return false;
        }

        return true;
    }

    @Override
    public String getIdentifier() {
        return "vvo";
    }
}