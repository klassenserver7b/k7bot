/**
 *
 */
package de.klassenserver7b.k7bot.util.customapis;

import com.google.gson.*;
import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.sql.LiteSQL;
import de.klassenserver7b.k7bot.subscriptions.types.SubscriptionTarget;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import de.klassenserver7b.k7bot.util.InternalStatusCodes;
import de.klassenserver7b.k7bot.util.customapis.types.LoopedEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.hc.client5.http.HttpHostConnectException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.EntityBuilder;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * @author Klassenserver7b
 */
public class GourmettaInteractions implements LoopedEvent {

    String token;
    String userid;

    boolean apiDisabled;

    final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     *
     */
    public GourmettaInteractions() {
        apiDisabled = true;
        token = null;
        userid = null;
    }

    /**
     * Login for the Gourmetta Rest-API based on the credentials given
     */
    public boolean login() {

        String username = K7Bot.getInstance().getPropertiesManager().getProperty("gourmettauserid");
        String password = K7Bot.getInstance().getPropertiesManager().getProperty("gourmettapassword");

        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            return false;
        }

        final HttpPost post = new HttpPost("https://bestellung-rest.gourmetta.de/login");

        EntityBuilder entitybuild = EntityBuilder.create();
        entitybuild.setContentType(ContentType.APPLICATION_JSON);

        JsonObject auth = new JsonObject();
        auth.addProperty("login", username);
        auth.addProperty("password", password);
        entitybuild.setText(auth.toString());

        try (final CloseableHttpClient httpclient = HttpClients.createSystem();
             HttpEntity entity = entitybuild.build()) {

            post.setEntity(entity);

            final String response = httpclient.execute(post, new BasicHttpClientResponseHandler());

            JsonElement elem = JsonParser.parseString(response);

            this.token = elem.getAsJsonObject().get("token").getAsString();

            String base64uid = token.split("\\.")[1];
            String uidjson = new String(Base64.getDecoder().decode(base64uid));
            this.userid = JsonParser.parseString(uidjson).getAsJsonObject().get("userUuid").getAsString();
            this.apiDisabled = false;

            if (userid != null && token != null) {
                return true;
            }

        } catch (HttpHostConnectException e1) {
            log.warn("Invalid response from bestellung-rest.gourmetta.de while logging in {}", e1.getMessage());
            return false;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return false;
        }

        return false;
    }

    /**
     * Disconnects from the Gourmetta API
     */
    public void logout() {
        this.token = null;
        this.userid = null;
        this.apiDisabled = true;
    }

    /**
     * Checks if there is a new Plan to provide and provides it
     */
    @Override
    public int checkforUpdates() {

        login();
        if (isApiDisabled()) {
            return InternalStatusCodes.SUCCESS;
        }

        log.debug("Gourmetta Check");

        OffsetDateTime offsetprovideDay = getNextDay();
        long provideday = Long.parseLong(offsetprovideDay.format(DateTimeFormatter.ofPattern("yyyyMMdd")));

        try (ResultSet set = LiteSQL.onQuery("Select * FROM gourmettaInteractions");
             MessageCreateData data = buildMessage(getStripedDayMeals(), offsetprovideDay)) {

            if (set.next()) {

                long dbday = set.getLong("lastday");

                if (dbday != provideday) {

                    if (data != null) {
                        providePlanMessage(data);
                    }

                    LiteSQL.onUpdate("UPDATE gourmettaInteractions SET lastday=?", provideday);
                }

            } else {

                if (data == null) {
                    return InternalStatusCodes.SUCCESS;
                }

                providePlanMessage(data);
                LiteSQL.onUpdate("INSERT INTO gourmettaInteractions(lastday) VALUES(?)", provideday);

            }

        } catch (IllegalArgumentException e) {
            log.info(e.getMessage(), e);
            return InternalStatusCodes.FAILURE;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            return InternalStatusCodes.FAILURE;
        }

        logout();

        return InternalStatusCodes.SUCCESS;

    }

    /**
     * Parses the given meals and returns the as a usable Discord
     * {@link MessageCreateData}
     *
     * @param stripedDayMeals {@link JsonArray} obtained by
     *                        {@link GourmettaInteractions#getStripedDayMeals()}
     * @param provideDay      The day for which the plan is as an
     *                        {@link OffsetDateTime}
     * @return The {@link MessageCreateData} which can be used in
     * {@link GourmettaInteractions#providePlanMessage(MessageCreateData)}
     */
    private MessageCreateData buildMessage(JsonArray stripedDayMeals, OffsetDateTime provideDay)
            throws IllegalArgumentException {

        if (stripedDayMeals == null) {
            throw new IllegalArgumentException();
        }

        if (stripedDayMeals.size() <= 0) {
            return null;
        }

        MessageCreateBuilder builder = new MessageCreateBuilder();

        StringBuilder descriptionbuild = new StringBuilder();

        stripedDayMeals.forEach(json -> {

            JsonObject meal = json.getAsJsonObject();

            descriptionbuild.append("**");
            descriptionbuild.append(meal.get("number").getAsString());
            descriptionbuild.append("** (");
            descriptionbuild.append(meal.get("price").getAsString());
            descriptionbuild.append("€) - **");
            descriptionbuild.append(StringEscapeUtils.unescapeJson(meal.get("name").getAsString()));
            descriptionbuild.append("**\n");
            descriptionbuild.append("_");
            descriptionbuild.append(StringEscapeUtils.unescapeJson(meal.get("description").getAsString()));
            descriptionbuild.append("_");
            descriptionbuild.append("\n\n");

        });

        EmbedBuilder embbuild = EmbedUtils.getBuilderOf(Color.decode("#038aff"), descriptionbuild);
        embbuild.setTitle("Gourmetta-Plan für den " + provideDay.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

        builder.setEmbeds(embbuild.build());
        return builder.build();
    }

    /**
     * @param data The pregenerated {@link MessageCreateData} which is used to build
     *             the {@link Message} - usually obtained by
     *             {@link GourmettaInteractions#buildMessage(JsonArray, OffsetDateTime)}
     */
    private void providePlanMessage(MessageCreateData data) {
        K7Bot.getInstance().getSubscriptionManager()
                .provideSubscriptionNotification(SubscriptionTarget.GOURMETTA, data);
    }

    /**
     * Reduces the meal-array to the necessary information
     *
     * @return The stripped meals as a {@link JsonArray}
     */
    private JsonArray getStripedDayMeals() throws IllegalStateException {

        if (isApiDisabled()) {
            return null;
        }

        JsonArray meals = getMealsforDayofWeek(DayOfWeek.of(getNextDay().getDayOfWeek().getValue()));

        if (meals == null) {
            throw new IllegalStateException();
        }

        if (meals.size() <= 0) {
            return new JsonArray();
        }

        JsonArray ret = new JsonArray();

        for (JsonElement json : meals) {

            JsonObject mealobj = json.getAsJsonObject().get("meal").getAsJsonObject();
            JsonObject retobj = new JsonObject();

            if (mealobj.get("categoryShortName").getAsString().matches("M\\d")) {

                JsonElement number = mealobj.get("categoryShortName");
                JsonElement name = mealobj.get("name");
                JsonElement description = mealobj.get("description");
                JsonElement price = mealobj.get("price");

                retobj.addProperty("number", (number.isJsonNull() ? "" : number.getAsString()));
                retobj.addProperty("name", (name.isJsonNull() ? "" : name.getAsString()));
                retobj.addProperty("description", (description.isJsonNull() ? "" : description.getAsString()));
                retobj.addProperty("price", (price.isJsonNull() ? "" : price.getAsString()));
                ret.add(retobj);
            }

        }

        return ret;

    }

    /**
     * Used to get all meals for a {@link DayOfWeek}
     *
     * @param day The {@link DayOfWeek} you want the meals for
     * @return The meals forthe selected {@link DayOfWeek} as a {@link JsonArray}
     */
    private JsonArray getMealsforDayofWeek(DayOfWeek day) {

        if (isApiDisabled() || day == null) {
            return null;
        }

        JsonArray days = getWeekPlan();

        if ((days == null) || (day.getValue() >= days.size())) {
            return new JsonArray();
        }

        JsonObject jsonday = days.get(day.getValue() - 1).getAsJsonObject();
        return jsonday.get("orderedMeals").getAsJsonArray();
    }

    /**
     * Requests the Plan of the current week from the REST-API and returns it as a
     * {@link JsonArray} <br>
     * If the current day is Saturday or Sunday the used week is the next week!
     *
     * @return The days with the meals as an {@link JsonArray}
     */
    private JsonArray getWeekPlan() {

        if (isApiDisabled()) {
            return null;
        }

        String url = generateOfferRequestURI();

        try (final CloseableHttpClient httpclient = HttpClients.createSystem()) {

            final HttpGet httpget = new HttpGet(url);

            httpget.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + this.token);
            httpget.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

            try {
                final String response = httpclient.execute(httpget, new BasicHttpClientResponseHandler());

                JsonElement elem = JsonParser.parseString(response);
                return elem.getAsJsonObject().get("orderDays").getAsJsonArray();

            } catch (HttpHostConnectException e1) {
                log.warn("Invalid response from bestellung-rest.gourmetta.de whilst accessing WeekPlan{}", e1.getMessage());
            } catch (IOException | JsonSyntaxException e) {
                log.error(e.getMessage(), e);
            }

        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        return null;

    }

    /**
     * Builds the offer request
     *
     * @return the url for the current week offer request
     */
    private String generateOfferRequestURI() {

        if (isApiDisabled()) {
            return null;
        }

        return "https://bestellung-rest.gourmetta.de/users/" +
                userid +
                "/order?from=" +
                getFirstDayofWeek().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +
                "&to=" +
                getLastDayofWeek().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    /**
     * Returns the Monday of the current week <br>
     * If the current day is Saturday or Sunday the used week is the next week!
     *
     * @return The Monday of the current week as an {@link OffsetDateTime}
     */
    private OffsetDateTime getFirstDayofWeek() {

        OffsetDateTime current = OffsetDateTime.now();
        int day = current.getDayOfWeek().getValue();

        if (day <= 5) {
            return current.minusDays(day - 1);
        }

        return current.plusDays(8 - day);
    }

    /**
     * Returns the Friday of the current week <br>
     * If the current day is Saturday or Sunday the used week is the next week!
     *
     * @return The Friday of the current week as an {@link OffsetDateTime}
     */
    private OffsetDateTime getLastDayofWeek() {

        OffsetDateTime current = OffsetDateTime.now();
        int day = current.getDayOfWeek().getValue();

        if (day == 5) {
            return current;
        }

        if (day < 5) {
            return current.plusDays(5 - day);
        }

        return current.plusDays(12 - day);
    }

    /**
     * @return The next day after the current day
     */
    private OffsetDateTime getNextDay() {

        OffsetDateTime cutime = OffsetDateTime.now();

        if ((cutime.getHour() <= 14) || cutime.getDayOfWeek().getValue() == 6
                || cutime.getDayOfWeek().getValue() == 5) {
            return cutime;
        }

        int day = cutime.getDayOfWeek().getValue();

        if (day >= 5) {
            return cutime.plusDays(8 - day);
        }

        return cutime.plusDays(1);
    }

    /**
     * Checks if the Api is enabled and warns if not
     *
     * @return Whether this Api is currently enabled or not
     */
    private boolean isApiDisabled() {

        if (apiDisabled) {
            log.warn("API not enabled! - Enable it by using 'login()'");
        }

        return apiDisabled;
    }

    @Override
    public void shutdown() {
        logout();

    }

    @Override
    public boolean restart() {
        logout();
        log.debug("restart requested");
        return true;
    }

    @Override
    public boolean isAvailable() {
        return login();
    }

    @Override
    public String getIdentifier() {
        return "gourmetta";
    }
}
