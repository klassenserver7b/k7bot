/**
 *
 */
package de.klassenserver7b.k7bot.commands.slash;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.types.TopLevelSlashCommand;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.EntityBuilder;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

/**
 * @author K7
 */
public class StableDiffusionCommand implements TopLevelSlashCommand {

    private static final Long DEFAULT_RATE_LIMIT = 30000L;

    private final HttpClient client;
    private final Logger log;

    private final HashMap<User, Long> limits;
    static final List<Long> allowedUsers = new ArrayList<>();

    /**
     *
     */
    public StableDiffusionCommand() {
        client = HttpClients.createSystem();
        log = LoggerFactory.getLogger(this.getClass());
        limits = new HashMap<>();

        allowedUsers.add(K7Bot.getInstance().getOwnerId());
        K7Bot.getInstance().getShardManager().getGuildById(779024287733776454L).loadMembers().onSuccess(members -> members.forEach(memb -> allowedUsers.add(memb.getUser().getIdLong())));
        K7Bot.getInstance().getShardManager().getGuildById(850697874147770368L).loadMembers().onSuccess(members -> members.forEach(memb -> allowedUsers.add(memb.getUser().getIdLong())));
    }

    @Override
    public void performSlashCommand(SlashCommandInteraction event) {

        if (!allowedUsers.contains(event.getUser().getIdLong())) {
            sendErrorEmbed(event, "You are not allowed to use the AI yet", "403 - Forbidden");
            return;
        }

        if (!(event.getChannel() instanceof StandardGuildMessageChannel)
                || !((StandardGuildMessageChannel) event.getChannel()).isNSFW()) {

            sendErrorEmbed(event, "You can only use this in a NSFW Channel", "Restricted");
            return;
        }

        HttpGet get = new HttpGet("http://127.0.0.1:7860/login_check");
        try {
            client.execute(get, (response) -> {

                if (response.getCode() != 200) {
                    throw new HttpException("Couldn't connect to service");
                }

                return null;
            });
        } catch (IOException e) {
            sendErrorEmbed(event, "Stable-diffusion is currently not available - please try again later", null);
            return;
        }

        if (!checkRateLimit(event.getUser())) {
            sendErrorEmbed(event, "You can only prompt every 30 seconds", ":warning: Rate Limit :warning:");
            return;
        }

        InteractionHook hook = event.deferReply().complete();

        HttpPost post = new HttpPost("http://127.0.0.1:7860/sdapi/v1/txt2img");
        EntityBuilder entbuild = EntityBuilder.create();
        entbuild.setContentType(ContentType.APPLICATION_JSON);

        JsonObject body = new JsonObject();

        body.addProperty("width", 512);
        body.addProperty("height", 512);
        body.addProperty("save_images", false);
        body.addProperty("send_images", true);

        body.addProperty("prompt", event.getOption("prompt").getAsString());

        body.addProperty("negative_prompt", event.getOption("negativeprompt").getAsString());

        body.addProperty("sampler_name", event.getOption("sampler").getAsString());

        body.addProperty("steps", (event.getOption("steps") != null ? event.getOption("steps").getAsLong() : 20));

        body.addProperty("cfg_scale",
                (event.getOption("cfg-scale") != null ? event.getOption("cfg-scale").getAsDouble() : 7));

        body.addProperty("seed", (event.getOption("seed") != null ? event.getOption("seed").getAsInt() : -1));
        body.addProperty("restore_faces",

                (event.getOption("restorefaces") != null && event.getOption("restorefaces").getAsBoolean()));

        entbuild.setText(body.toString());

        try (HttpEntity entity = entbuild.build()) {

            post.setEntity(entity);

            String resp = client.execute(post, new BasicHttpClientResponseHandler());

            JsonObject jsresponse = JsonParser.parseString(resp).getAsJsonObject();
            String seed = JsonParser.parseString(jsresponse.get("info").getAsString()).getAsJsonObject().get("seed")
                    .getAsString();

            byte[] decodedBytes = Base64.getDecoder()
                    .decode(jsresponse.get("images").getAsJsonArray().get(0).getAsString().getBytes());

            File f = File.createTempFile(OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "/"
                    + seed + "-" + event.getOption("sampler").getAsString() + "_"
                    + OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")), ".png");
            f.deleteOnExit();

            try (FileOutputStream fout = new FileOutputStream(f); FileUpload fup = FileUpload.fromData(f)) {
                fout.write(decodedBytes);
                hook.sendFiles(fup).complete();
                //noinspection ResultOfMethodCallIgnored
                f.delete();
            }

        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    protected void sendErrorEmbed(SlashCommandInteraction event, String description, String title) {
        EmbedBuilder builder = EmbedUtils.getErrorEmbed(description);

        if (title != null) {
            builder.setTitle(title);
        }

        event.replyEmbeds(builder.build()).setEphemeral(true).queue();
    }

    protected boolean checkRateLimit(User u) {

        if (u.getIdLong() == K7Bot.getInstance().getOwnerId()) {
            return true;
        }

        Long time = limits.get(u);

        if (time != null) {

            long current = Instant.now().toEpochMilli();

            if ((time + DEFAULT_RATE_LIMIT) >= current) {
                return false;
            }

            limits.put(u, current);
            return true;
        }

        limits.put(u, Instant.now().toEpochMilli());
        return true;
    }

    @NotNull
    @Override
    public SlashCommandData getCommandData() {

        List<OptionData> options = new ArrayList<>();

        options.add(new OptionData(OptionType.STRING, "prompt", "insert your prompt here").setRequired(true)
                .setMaxLength(2000));

        options.add(new OptionData(OptionType.STRING, "negativeprompt", "insert your negative-prompt here")
                .setRequired(true).setMaxLength(2000));

        options.add(new OptionData(OptionType.STRING, "sampler", "select the sampler").setRequired(true).addChoices(
                new Choice("Euler a", "Euler a"), new Choice("Euler", "Euler"), new Choice("DPM++ 2M", "DPM++ 2M"),
                new Choice("DPM++ 2M Karras", "DPM++ 2M Karras"), new Choice("DDIM", "DDIM")));

        options.add(new OptionData(OptionType.INTEGER, "steps", "insert your sample step count here - default: 20")
                .setRequired(false).setRequiredRange(1, 50));

        options.add(new OptionData(OptionType.NUMBER, "cfg-scale", "insert your config-scale here - default: 7")
                .setRequired(false).setRequiredRange(1, 15));

        options.add(
                new OptionData(OptionType.INTEGER, "seed", "insert your seed here - default: -1").setRequired(false));

        options.add(new OptionData(OptionType.BOOLEAN, "restorefaces",
                "Whether the 'restore faces' option should be enabled - default: false").setRequired(false));

        return Commands.slash("stable-diffusion", "starts the process to imagine a text -> txt2img")
                .addOptions(options);
    }

    public static void removeAIUser(Long userid) {
        allowedUsers.remove(userid);
    }

    public static void addAIUser(Long userid) {
        allowedUsers.add(userid);
    }

}
