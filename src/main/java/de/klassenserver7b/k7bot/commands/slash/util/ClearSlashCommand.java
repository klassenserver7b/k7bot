/* (C)2026 */
package de.klassenserver7b.k7bot.commands.slash.util;

import java.awt.*;

import org.jetbrains.annotations.NotNull;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.types.GuildSlashCommand;
import de.klassenserver7b.k7bot.util.CommandUtils;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import de.klassenserver7b.k7bot.util.GenericMessageSendHandler;
import de.klassenserver7b.k7bot.util.MessageClearUtil;
import de.klassenserver7b.k7bot.util.errorhandler.PermissionError;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ClearSlashCommand implements GuildSlashCommand {
	@Override
	public void performGuildSlashCommand(@NotNull SlashCommandInteraction event, @NotNull Guild guild,
			@NotNull Member member) {

		if (member.hasPermission(Permission.MESSAGE_MANAGE)) {

			OptionMapping amountOption = CommandUtils.getRequiredOption(event, "amount");

			InteractionHook hook = event.deferReply(true).complete();
			int amount;
			amount = (int) amountOption.getAsLong();

			if (amount > 200) {
				hook.sendMessage(
						"Aufgrund von Zugriffslimitierungen, kann ich nicht mehr als 200" + " Nachrichten löschen!")
						.queue();
				return;
			}

			MessageClearUtil.onclear(amount - 1, event.getChannel().asGuildMessageChannel());

			hook.sendMessage(amount + " messages deleted.").queue();

			EmbedBuilder builder = EmbedUtils.getBuilderOf(Color.orange, amount + " messages deleted!\n\n"
					+ "**Channel: **\n" + "#" + event.getChannel().asGuildMessageChannel().getName(),
					guild.getIdLong());

			builder.setFooter("requested by @" + member.getEffectiveName());
			GuildMessageChannel system = K7Bot.getInstance().getSysChannelMgr().getSysChannel(guild);

			if (system != null) {
				system.sendMessageEmbeds(builder.build()).queue();
			}

		} else {
			PermissionError.onPermissionError(new GenericMessageSendHandler(event.getChannel().asGuildMessageChannel()),
					member);
		}
	}

	@Override
	public @NotNull SlashCommandData getCommandData() {
		return Commands.slash("clear", "Löscht die ausgewählte Anzahl an Nachrichten.")
				.addOptions(
						new OptionData(OptionType.INTEGER, "amount", "Wie viele Nachrichten sollen gelöscht werden?")
								.setRequired(true))
				.setContexts(InteractionContextType.GUILD)
				.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE));
	}
}
