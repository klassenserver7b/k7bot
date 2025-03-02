package de.klassenserver7b.k7bot.util.customapis;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.sql.LiteSQL;
import de.klassenserver7b.k7bot.subscriptions.types.SubscriptionTarget;
import de.klassenserver7b.k7bot.util.tablemessage.Cell;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import de.klassenserver7b.k7bot.util.tablemessage.TableMessage;
import de.klassenserver7b.k7bot.util.TeacherDB;
import net.dv8tion.jda.annotations.DeprecatedSince;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;

import java.awt.*;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author K7
 * @deprecated use {@link de.klassenserver7b.k7bot.util.customapis.Stundenplan24Vplan
 * VplanNEW_XML instead}
 */
@Deprecated
@DeprecatedSince(value = "1.14.0")
public class VPlan_MANOS_OLD {

    private final Logger log = K7Bot.getInstance().getMainLogger();
    private final String vplanpw;

    public VPlan_MANOS_OLD(String pw) {
        vplanpw = pw;
    }

    /**
     * @param force   if true, the message will be sent regardless of the current state
     * @param klasse  the class to send the message to
     * @param channel the channel to send the message to
     * @since 1.15.0
     */
    public void sendVplanToChannel(boolean force, String klasse, GuildMessageChannel channel) {

        try (MessageCreateData d = getVplanMessage()) {
            if (d != null) {
                channel.sendMessage(d).queue();
            }
        }

    }

    /**
     * @param klasse the class to send the message to
     * @since 1.15.0
     */
    public void VplanNotify(String klasse) {

        try (MessageCreateData d = getVplanMessage()) {
            if (d != null) {
                K7Bot.getInstance().getSubscriptionManager()
                        .provideSubscriptionNotification(SubscriptionTarget.VPLAN, d);
            }
        }
    }

    /**
     * See {@link VPlan_MANOS_OLD}
     */
    @DeprecatedSince(value = "1.14.0")
    private MessageCreateData getVplanMessage() {

        JsonObject plan = getPlan();

        List<JsonObject> fien = finalplancheck(plan);

        if (fien == null) {
            return null;
        }

        if (log != null) {
            log.debug("sending Vplanmessage with following hash: {} and devmode = {}", fien.hashCode(), K7Bot.getInstance().isDevMode());
        }

        EmbedBuilder builder = buildEmbed(fien.isEmpty(), plan);

        if (!fien.isEmpty()) {

            TableMessage tablemess = buildMessage(fien);

            builder.setDescription("**Änderungen**\n" + tablemess.build());

        }

        LiteSQL.onUpdate("UPDATE vplannext SET classEntrys = ?;", fien.hashCode());

        return new MessageCreateBuilder().setEmbeds(builder.build()).build();

    }

    /**
     * @param fien the list of entries to build the message from
     * @return the built message
     */
    private TableMessage buildMessage(List<JsonObject> fien) {

        TableMessage tablemess = new TableMessage();
        tablemess.addHeadline("Stunde", "Fach", "Lehrer", "Raum", "Info");

        for (JsonObject entry : fien) {

            tablemess.addCell(entry.get("lesson").getAsString().replaceAll("\"", ""));

            if (!entry.get("subject").toString().equalsIgnoreCase("\"---\"")) {

                appendSubject(tablemess, entry);

            } else {

                tablemess.addRow(Cell.of("AUSFALL", Cell.STYLE_BOLD), "---", "---");

            }

            if (!entry.get("info").toString().equalsIgnoreCase("\"\"")) {

                tablemess.addCell(entry.get("info").getAsString().replaceAll("\"", ""));

            } else {
                tablemess.addCell("   ");
            }

        }

        tablemess.automaticLineBreaks(4);

        return tablemess;

    }

    private TableMessage appendSubject(TableMessage mess, JsonObject entry) {

        JsonArray changes = entry.get("changed").getAsJsonArray();
        boolean subjectchange = false;
        boolean teacherchange = false;
        boolean roomchange = false;

        if (!changes.isEmpty()) {

            if (changes.toString().contains("subject")) {
                subjectchange = true;
            }
            if (changes.toString().contains("teacher")) {
                teacherchange = true;
            }
            if (changes.toString().contains("room")) {
                roomchange = true;
            }

        }

        Cell subjectCell = Cell.of(entry.get("subject").getAsString().replaceAll("\"", ""),
                (subjectchange ? Cell.STYLE_BOLD : Cell.STYLE_NONE));
        Cell teacherCell = Cell.of(entry.get("teacher").getAsString().replaceAll("\"", ""),
                (teacherchange ? Cell.STYLE_BOLD : Cell.STYLE_NONE));

        String teacherId = entry.get("teacher").getAsString();
        TeacherDB.Teacher teacher = K7Bot.getInstance().getTeacherDB().getTeacher(teacherId);
        if (teacher != null) {
            teacherCell.setLinkTitle(teacher.getDecoratedName());
            teacherCell.setLinkURL("https://manos-dresden.de/lehrer");
        }

        Cell roomCell = Cell.of(entry.get("room").getAsString().replaceAll("\"", ""),
                (roomchange ? Cell.STYLE_BOLD : Cell.STYLE_NONE));

        mess.addRow(subjectCell, teacherCell, roomCell);

        return mess;

    }

    private EmbedBuilder buildEmbed(boolean fienempty, JsonObject plan) {

        EmbedBuilder builder = EmbedUtils.getBuilderOf(Color.decode("#038aff"));

        String info = plan.get("info").toString();

        info = info.replaceAll("\"", "").replaceAll("\\[", "").replaceAll("]", "").trim();

        builder.setTitle("Es gibt einen neuen Vertretungsplan für "
                + plan.get("head").getAsJsonObject().get("title").getAsString() + "\n");

        if (fienempty) {
            builder.setTitle("**KEINE ÄNDERUNGEN :sob:**");

            if (!(info.equalsIgnoreCase(""))) {

                builder.addField("Sonstige Infos", info, false);

            }
        }

        builder.setFooter("Stand vom ");

        if (!(info.equalsIgnoreCase(""))) {

            builder.addField("Sonstige Infos", info, false);

        }

        return builder;
    }

    /**
     * See {@link VPlan_MANOS_OLD}
     */
    @DeprecatedSince(value = "1.14.0")
    private List<JsonObject> finalplancheck(JsonObject plan) {

        Integer dbh = null;
        List<JsonObject> finalentries = new ArrayList<>();

        if (plan == null) {
            return null;
        }
        boolean synced;

        synced = synchronizePlanDB(plan);

        List<JsonObject> getC = getyourC(plan);
        if (getC == null) {
            return null;
        }
        int h = getC.hashCode();

        try (ResultSet set = LiteSQL.onQuery("SELECT classEntrys FROM vplannext;")) {

            if (set.next()) {

                dbh = set.getInt("classEntrys");

            }

            if (dbh != null) {
                if (dbh != h || synced) {

                    finalentries = getC;

                    String date = plan.get("head").getAsJsonObject().get("title").getAsString().replaceAll(" ", "")
                            .replaceAll("\\(B-Woche\\)", "").replaceAll("\\(A-Woche\\)", "").replaceAll(",", "")
                            .replaceAll("Montag", "").replaceAll("Dienstag", "").replaceAll("Mittwoch", "")
                            .replaceAll("Donnerstag", "").replaceAll("Freitag", "").toLowerCase();
                    LiteSQL.onUpdate("UPDATE vplannext SET targetDate = ?;", date);

                } else {
                    return null;

                }
            } else {
                finalentries = getC;
                String date = plan.get("head").getAsJsonObject().get("title").getAsString().replaceAll(" ", "")
                        .replaceAll("\\(B-Woche\\)", "").replaceAll("\\(A-Woche\\)", "").replaceAll(",", "")
                        .replaceAll("Montag", "").replaceAll("Dienstag", "").replaceAll("Mittwoch", "")
                        .replaceAll("Donnerstag", "").replaceAll("Freitag", "").toLowerCase();
                LiteSQL.onUpdate("INSERT INTO vplannext(targetDate, classEntrys) VALUES(?, ?);", date,
                        finalentries.hashCode());
            }

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        return finalentries;

    }

    /**
     * See {@link VPlan_MANOS_OLD}
     */
    @DeprecatedSince(value = "1.14.0")
    private boolean synchronizePlanDB(JsonObject plan) {
        if (plan != null) {
            String dbdate = "";

            String onlinedate = plan.get("head").getAsJsonObject().get("title").getAsString().replaceAll(" ", "")
                    .replaceAll("\\(B-Woche\\)", "").replaceAll("\\(A-Woche\\)", "").replaceAll(",", "")
                    .replaceAll("Montag", "").replaceAll("Dienstag", "").replaceAll("Mittwoch", "")
                    .replaceAll("Donnerstag", "").replaceAll("Freitag", "").toLowerCase();
            OffsetDateTime time = OffsetDateTime.now();
            String realdate = time.getDayOfMonth() + "."
                    + time.getMonth().getDisplayName(TextStyle.FULL, Locale.GERMAN).toLowerCase() + time.getYear();

            try (ResultSet next = LiteSQL.onQuery("SELECT targetDate FROM vplannext;")) {
                if (next.next()) {

                    dbdate = next.getString("targetDate");
                }

                if (!(dbdate.equalsIgnoreCase(onlinedate)) && dbdate.equalsIgnoreCase(realdate)) {

                    LiteSQL.getdblog().info("Plan-DB-Sync");

                    try (ResultSet old = LiteSQL.onQuery("SELECT * FROM vplannext;")) {

                        if (old.next()) {
                            LiteSQL.onUpdate("UPDATE vplancurrent SET targetDate = ?, classEntrys = ?;",
                                    old.getString("targetDate"), old.getInt("classEntrys"));
                            LiteSQL.onUpdate("UPDATE vplannext SET targetDate = '', classEntrys = '';");
                        }
                    }
                    return true;
                }
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
        }
        return false;

    }

    /**
     * See {@link VPlan_MANOS_OLD}
     */
    @DeprecatedSince(value = "1.14.0")
    private List<JsonObject> getyourC(JsonObject obj) {
        List<JsonObject> classentries = new ArrayList<>();
        if (obj != null) {
            JsonArray arr = obj.get("body").getAsJsonArray();
            arr.forEach(element -> {
                String elem = element.getAsJsonObject().get("class").toString().replaceAll("\"", "");
                if (elem.contains("10b") || elem.equalsIgnoreCase("10a-10c/ Spw") || elem.equalsIgnoreCase("10a-10c")) {

                    classentries.add(element.getAsJsonObject());

                }

            });
            return classentries;
        }
        return null;

    }

    /**
     * See {@link VPlan_MANOS_OLD}
     */
    @DeprecatedSince(value = "1.14.0")
    private JsonObject getPlan() {

        final BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope("manos-dresden.de", 443),
                new UsernamePasswordCredentials("manos", vplanpw));

        final HttpGet httpget = new HttpGet("https://manos-dresden.de/vplan/upload/next/students.json");

        try (final CloseableHttpClient httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider)
                .build(); final CloseableHttpResponse response = httpclient.execute(httpget)) {

            if (response.getStatusLine().getStatusCode() == 200) {
                JsonElement elem = new JsonParser().parse(EntityUtils.toString(response.getEntity()));
                return elem.getAsJsonObject();
            }
            return null;

        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }

    }

}