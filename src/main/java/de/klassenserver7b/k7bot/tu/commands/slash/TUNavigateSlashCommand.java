package de.klassenserver7b.k7bot.tu.commands.slash;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.types.TopLevelSlashCommand;
import de.klassenserver7b.k7bot.tu.navigator.TUNavigator;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

public class TUNavigateSlashCommand implements TopLevelSlashCommand {
    static final Logger logger = LoggerFactory.getLogger(TUNavigateSlashCommand.class);

    @Override
    public @NotNull SlashCommandData getCommandData() {
        return Commands.slash("tu-navigate", "Zeigt einen Raum oder ein Gebäude an")
                .addOptions(new OptionData(OptionType.STRING, "id", "ID des Raums / Gebäudes")
                        .setRequired(true));
    }

    @Override
    public void performSlashCommand(SlashCommandInteraction event) {
        InteractionHook interaction = event.deferReply().complete();
        String value = Objects.requireNonNull(event.getOption("id")).getAsString().strip();

        TUNavigator.Target target = K7Bot.getInstance().getTuNavigator().resolveTarget(value);
        if (target == null) {
            interaction.sendMessage("Fehler: `" + value + "` konnte nicht gefunden werden").queue();
            return;
        }

        MessageCreateBuilder builder = new MessageCreateBuilder();
        builder.addContent(target.building() + ", Etage " + target.floor() + "\n");
        if (target.room() != null)
            builder.addContent("Raum " + target.room() + "\n");

        String mapsUrl = K7Bot.getInstance().getTuNavigator().getMapsUrl(target.building(), target.partId());
        if (mapsUrl != null)
            builder.addContent("[Google Maps](<" + mapsUrl + ">)\n");

        BufferedImage backgroundImage = K7Bot.getInstance().getTuNavigator().getBackground(target.building(), target.floor());
        BufferedImage outputImage = new BufferedImage(backgroundImage.getWidth(), backgroundImage.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D gfx = outputImage.createGraphics();
        gfx.setColor(Color.WHITE);
        gfx.fillRect(0, 0, backgroundImage.getWidth(), backgroundImage.getHeight());
        K7Bot.getInstance().getTuNavigator().renderFloor(target.building(), target.floor(), target.room(), gfx);
        gfx.drawImage(backgroundImage, 0, 0, null);
        K7Bot.getInstance().getTuNavigator().renderFloorLabels(target.building(), target.floor(), gfx);
        gfx.dispose();

        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(outputImage, "png", os);
            builder.addFiles(FileUpload.fromData(os.toByteArray(), "background.png"));
        } catch (IOException e) {
            logger.error("error attaching background image", e);
            interaction.sendMessage("Fehler").queue();
            return;
        }

        interaction.sendMessage(builder.build()).queue();
    }
}
