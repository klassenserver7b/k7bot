/* (C)2026 */
package de.klassenserver7b.k7bot.commands.slash.util;

import java.awt.*;

import org.jetbrains.annotations.NotNull;

import de.klassenserver7b.k7bot.commands.types.GuildSlashCommand;
import de.klassenserver7b.k7bot.database.dao.MemeChannelDAO;
import de.klassenserver7b.k7bot.util.CommandUtils;
import de.klassenserver7b.k7bot.util.GenericMessageSendHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
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

/**
 *
 */
public class MemesChannelSlashCommand implements GuildSlashCommand {

	@Override
	public void performGuildSlashCommand(@NotNull SlashCommandInteraction event, @NotNull Guild guild,
			@NotNull Member member) {

		InteractionHook hook = event.deferReply(false).complete();

		OptionMapping channelOption = CommandUtils.getRequiredOption(event, "channel");

		GuildChannelUnion channel = channelOption.getAsChannel();
		Long channelId = channel.getIdLong();

		if (event.getFullCommandName().split(" ")[1].equalsIgnoreCase("add")) {
			new MemeChannelDAO().addChannel(channelId);

			new GenericMessageSendHandler(hook)
					.sendMessageEmbeds(new EmbedBuilder().setColor(Color.green)
							.setDescription("Successfully added " + channel.getAsMention() + " as Memechannel").build())
					.queue();
		} else {

			new MemeChannelDAO().removeChannel(channelId);

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
