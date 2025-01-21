package de.klassenserver7b.k7bot.util.customapis;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.manage.PropertiesManager;
import de.klassenserver7b.k7bot.sql.LiteSQL;
import de.klassenserver7b.k7bot.subscriptions.types.SubscriptionTarget;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import de.klassenserver7b.k7bot.util.InternalStatusCodes;
import de.klassenserver7b.k7bot.util.customapis.types.LoopedEvent;
import de.konsl.webweaverapi.WebWeaverClient;
import de.konsl.webweaverapi.WebWeaverException;
import de.konsl.webweaverapi.messages.request.SelfAutologinRequest;
import de.konsl.webweaverapi.model.FocusObject;
import de.konsl.webweaverapi.model.auth.Credentials;
import de.konsl.webweaverapi.model.messages.Message;
import de.konsl.webweaverapi.model.messages.MessageType;
import de.konsl.webweaverscraper.Popup;
import de.konsl.webweaverscraper.WebWeaverScraper;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Used to interact with the LernsaxAPI <br>
 * Provides methods to:<br>
 * - {@link LernsaxInteractions#connect(String, String, String) login}<br>
 * - {@link LernsaxInteractions#checkForLernplanMessages() check for new
 * "Learning Plans"} <br>
 * - {@link LernsaxInteractions#disconnect() disconnect}
 *
 * @author Klassenserver7b
 */
public class LernsaxInteractions implements LoopedEvent {

    private WebWeaverClient client;
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private int ecount = 0;

    /**
     * Used to connect to the LernsaxAPI via the credentials given in the configfile
     */
    public boolean connect() {

        PropertiesManager propMgr = K7Bot.getInstance().getPropertiesManager();

        this.client = new WebWeaverClient();
        Credentials cred = new Credentials(propMgr.getProperty("lsaxemail"), propMgr.getProperty("lsaxtoken"),
                propMgr.getProperty("lsaxappid"));

        try {

            client.login(cred);
            return true;

        } catch (IOException | NoSuchAlgorithmException | WebWeaverException e) {
            log.warn(e.getMessage());
            return false;
        }

    }

    /**
     * Used to connect to the LernsaxAPI via.
     *
     * @param lsaxemail     The email of your Lernsax account
     * @param token         The token for this application
     * @param applicationID The Id of this application
     * @return if the connection was successfully established
     */
    @SuppressWarnings("unused")
    public boolean connect(String lsaxemail, String token, String applicationID) {

        this.client = new WebWeaverClient();
        Credentials cred = new Credentials(lsaxemail, token, applicationID);

        try {

            client.login(cred);

        } catch (IOException | NoSuchAlgorithmException | WebWeaverException e) {
            return false;
        }

        return true;

    }

    /**
     * Disconnects this client from the API
     */
    public void disconnect() {
        try {
            client.logout();
        } catch (IOException | WebWeaverException e) {
            log.error(e.getMessage(), e);
        }
        client = null;
    }

    /**
     *
     */
    @Override
    public int checkforUpdates() {

        if (client == null || ecount >= 15) {
            ecount = 0;
            if (!connect()) {
                return InternalStatusCodes.FAILURE;
            }
        }

        List<Message> messages = checkForLernplanMessages();

        if (messages == null) {
            return InternalStatusCodes.FAILURE;
        }

        if (!messages.isEmpty()) {
            sendLernsaxEmbeds(messages);
        }

        return InternalStatusCodes.SUCCESS;

    }

    /**
     * Checks if there are any new "Lernplaene" -> "LearningPlans"<br>
     * Should only be used in connection with
     * {@link LernsaxInteractions#sendLernsaxEmbeds(List)}
     *
     * @return A List of all new LearningPlans (empty if there are none)
     */
    private List<Message> checkForLernplanMessages() {

        List<Message> messages;

        String currentMessageID = null;

        try (ResultSet set = LiteSQL.onQuery("Select LernplanId from lernsaxinteractions;")) {
            if (set.next()) {
                currentMessageID = set.getString("LernplanId");
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            return null;
        }

        if (currentMessageID == null) {

            try {
                messages = client.getMessagesScope().getMessages();

                LiteSQL.onUpdate("INSERT INTO lernsaxinteractions(LernplanId) VALUES(?);",
                        messages.getLast().getId());
            } catch (WebWeaverException | IOException e) {
                log.error(e.getMessage(), e);
                return null;
            }

        } else {
            try {
                messages = client.getMessagesScope().getMessages(Integer.parseInt(currentMessageID));

                if (!messages.isEmpty())
                    messages = messages.stream().skip(1).toList();

                if (!messages.isEmpty()) {
                    LiteSQL.onUpdate("UPDATE lernsaxinteractions SET LernplanId=?;",
                            messages.getLast().getId());
                }
            } catch (NumberFormatException | WebWeaverException e) {
                if (e.getMessage().equalsIgnoreCase("null")) {
                    log.warn("Lernsax API request failed", e);
                    ecount++;
                    return null;
                }
                log.error(e.getMessage(), e);
                ecount++;
                return null;
            } catch (NullPointerException | IOException e) {
                log.warn("Lernsax API request failed", e);
                ecount++;
                return null;
            }
        }

        if (messages.isEmpty()) {
            return messages;
        }

        messages = messages.stream().filter(msg -> msg.getType() == MessageType.NEW_LEARNING_PLAN).toList();

        return messages;
    }

    /**
     * Sends the given List of "lerplanmessages" obtained by
     * {@link LernsaxInteractions#checkForLernplanMessages()} to every "Lernsax"
     * subscribing channel
     *
     * @param lernplanmessages The List obtained by
     *                         {@link LernsaxInteractions#checkForLernplanMessages()}
     */
    public void sendLernsaxEmbeds(List<Message> lernplanmessages) {

        if (lernplanmessages == null) {
            log.warn("Illegal lernplanmessages list provided - List is null");
            return;
        }

        if (!lernplanmessages.isEmpty()) {

            log.info("Sending {} new Lernplan-Embeds", lernplanmessages.size());
            List<MessageCreateData> messageData = new ArrayList<>();

            for (Message msg : lernplanmessages) {

                String linkURLQuery;

                linkURLQuery = URLEncoder.encode(
                        "learning_plan|" + msg.getFromGroup().getLogin() + "|" + msg.getData() + "|/", StandardCharsets.UTF_8);

                MessageEmbed messEmbed = EmbedUtils.getDefault()
                        .setTitle("Ein neuer Lernplan wurde bereitgestellt!",
                                "https://" + client.getRemoteHost() + "/l.php?" + linkURLQuery)
                        .addField("Name", msg.getData(), false)
                        .addField("Klasse / Gruppe", msg.getFromGroup().getName(), false)
                        .addField("Autor", msg.getFromUser().getName(), false).build();

                messageData.add(new MessageCreateBuilder().addEmbeds(messEmbed).build());

                try {

                    String autologinUrl = client.request(
                            new SelfAutologinRequest(msg.getFromGroup().getLogin(), "learning_plan", msg.getData()),
                            FocusObject.TRUSTS).getUrl();

                    WebWeaverScraper scraper = new WebWeaverScraper();
                    Popup popup = scraper.navigate(URI.create(autologinUrl), true);

                    Element learningPlanContent = popup.document().selectFirst("#main_content > p.panel");
                    if (learningPlanContent == null)
                        continue;
                    String content = learningPlanContent.wholeText();

                    Element logoutLink = scraper.getDocument().selectFirst("a[href^=107480.php]");
                    if (logoutLink != null) {
                        URI logoutUri = URI.create(logoutLink.attr("href"));
                        scraper.navigate(logoutUri);
                    }

                    try (FileUpload fup = FileUpload.fromData(content.getBytes(StandardCharsets.UTF_8), "Learningplan_"
                            + OffsetDateTime.now().toEpochSecond() + "-" + msg.getData().replace(" ", "_") + ".txt")) {
                        messageData.add(new MessageCreateBuilder().addFiles(fup).build());
                    }

                } catch (WebWeaverException | IOException e1) {
                    log.error(e1.getMessage(), e1);
                }

            }

            for (MessageCreateData data : messageData) {

                K7Bot.getInstance().getSubscriptionManager()
                        .provideSubscriptionNotification(SubscriptionTarget.LERNPLAN, data);

            }

        }

    }

    /**
     * Used to shutdown the lernsaxAPI
     */
    @Override
    public void shutdown() {
        disconnect();

    }

    @Override
    public boolean restart() {
        log.debug("restart requested");
        disconnect();
        return connect();
    }

    @Override
    public boolean isAvailable() {
        return connect();
    }

    @Override
    public String getIdentifier() {
        return "lernsax";
    }

}
