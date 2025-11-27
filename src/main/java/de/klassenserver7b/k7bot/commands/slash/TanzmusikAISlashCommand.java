package de.klassenserver7b.k7bot.commands.slash;

import de.klassenserver7b.k7bot.commands.types.TopLevelSlashCommand;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static de.klassenserver7b.k7bot.commands.slash.StableDiffusionCommand.allowedUsers;

public class TanzmusikAISlashCommand implements TopLevelSlashCommand {

    private File tempdir;
    private final Logger log;

    public TanzmusikAISlashCommand() {

        log = LoggerFactory.getLogger(getClass());

        try {
            this.tempdir = Files.createTempDirectory("k7bot_tanzmusikai_" + System.currentTimeMillis()).toFile();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * @param event
     */
    @Override
    public void performSlashCommand(SlashCommandInteraction event) {

        if (!allowedUsers.contains(event.getUser().getIdLong())) {
            event.replyEmbeds(EmbedUtils.getErrorEmbed("You are not allowed to use the AI yet").setTitle("403 - Forbidden").build()).queue();
            return;
        }

        InteractionHook hook = event.deferReply(true).complete();

        List<String> results;

        try {

            File songFile = downloadAttachment(event.getOption("song").getAsAttachment());
            results = analyzeSong(songFile);
            log.debug("State of Songfile deletion: {}, {}", songFile, songFile.delete());


        } catch (IOException | InterruptedException | ExecutionException e) {
            hook.sendMessageEmbeds(EmbedUtils.getErrorEmbed("Failed to analyze the song").setTitle("500 - Internal Server Error").build()).queue();
            log.warn(e.getMessage(), e);
            return;
        }

        String filename = event.getOption("song").getAsAttachment().getFileName();

        StringBuilder sb = new StringBuilder();
        sb.append(filename).append(":\n");
        sb.append(results.get(0)).append(" - C:").append(results.get(3)).append("\n");
        sb.append(results.get(1)).append(" - C:").append(results.get(4)).append("\n");
        sb.append(results.get(2)).append(" - C:").append(results.get(5)).append("\n");
        sb.append("Overall: ").append(results.get(6));

        hook.sendMessageEmbeds(EmbedUtils.getSuccessEmbed(sb).setTitle("Tanzmusik AI result").build()).queue();

    }

    protected File downloadAttachment(Message.Attachment attachment) throws IOException, ExecutionException, InterruptedException {
        long now = System.currentTimeMillis();

        File tempfile = Files.createTempFile(tempdir.toPath(), "tanzmusikai-" + now, ".mp3").toFile();

        attachment.getProxy().downloadToFile(tempfile).get();

        return tempfile;
    }

    protected List<String> analyzeSong(File songFile) throws IOException, InterruptedException {

        ProcessBuilder pb = new ProcessBuilder();
        pb.environment().put("TRANSFORMERS_CACHE", "/opt/k7bot/resources/tanzmusik/.transformers_cache");
        int exitCode = pb.command("/opt/k7bot/resources/tanzmusik/run_k7bot.sh", songFile.getAbsolutePath()).start().waitFor();

        if (exitCode != 0) {
            throw new IOException("AI exited with code " + exitCode);
        }

        List<String> csvlines = Files.readAllLines(Path.of("/opt/k7bot/resources/tanzmusik/k7_classification.csv"), StandardCharsets.UTF_8);
        return Arrays.stream(csvlines.get(1).split(",")).skip(1).toList();
    }

    protected void sendErrorEmbed(SlashCommandInteraction event, String description, String title) {
        EmbedBuilder builder = EmbedUtils.getErrorEmbed(description);

        if (title != null) {
            builder.setTitle(title);
        }

        event.replyEmbeds(builder.build()).setEphemeral(true).queue();
    }

    /**
     * @return
     */
    @NotNull
    @Override
    public SlashCommandData getCommandData() {
        List<OptionData> options = new ArrayList<>();

        options.add(new OptionData(OptionType.ATTACHMENT, "song", "The song to analyze", true));

        return Commands.slash("tanzmusikai", "Analyzes the provided song for ballromm dancing")
                .addOptions(options);
    }
}
