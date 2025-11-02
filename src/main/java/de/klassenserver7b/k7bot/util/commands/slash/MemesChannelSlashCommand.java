/**
 *
 */
package de.klassenserver7b.k7bot.util.commands.slash;

import de.klassenserver7b.k7bot.commands.types.TopLevelSlashCommand;
import de.klassenserver7b.k7bot.sql.LiteSQL;
import de.klassenserver7b.k7bot.util.GenericMessageSendHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 *
 */
public class MemesChannelSlashCommand implements TopLevelSlashCommand {

    @Override
    public void performSlashCommand(SlashCommandInteraction event) {

        InteractionHook hook = event.deferReply(false).complete();

        OptionMapping channelOption = event.getOption("channel");

        assert channelOption != null : "channel is null";

        GuildChannelUnion channel = channelOption.getAsChannel();
        Long channelId = channel.getIdLong();

        if (event.getFullCommandName().split(" ")[1].equalsIgnoreCase("add")) {
            LiteSQL.onUpdate("INSERT INTO memechannels(channelId) VALUES(?)", channelId);

            new GenericMessageSendHandler(hook)
                    .sendMessageEmbeds(new EmbedBuilder().setColor(Color.green)
                            .setDescription("Successfully added " + channel.getAsMention() + " as Memechannel").build())
                    .queue();
        } else {

            LiteSQL.onUpdate("REMOVE FROM memechannels WHERE channelId = ?", channelId);

            new GenericMessageSendHandler(hook).sendMessageEmbeds(new EmbedBuilder().setColor(Color.green)
                            .setDescription("Successfully removed " + channel.getAsMention() + " as Memechannel").build())
                    .queue();
        }

    }

    @NotNull
    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("memeschannel", "modify memechannels")
                .addSubcommands(new SubcommandData("add", "adds a memechannel")
                        .addOptions(new OptionData(OptionType.CHANNEL, "channel", "the channel to use")
                                .setRequired(true).setChannelTypes(ChannelType.TEXT)))
                .addSubcommands(new SubcommandData("remove", "removes a memechannel")
                        .addOptions(new OptionData(OptionType.CHANNEL, "channel", "the channel to use")
                                .setRequired(true).setChannelTypes(ChannelType.TEXT)))
                .setContexts(InteractionContextType.GUILD);
    }

}
