package de.klassenserver7b.k7bot.commands.slash;

import de.klassenserver7b.k7bot.util.HelpCategories;
import de.klassenserver7b.k7bot.commands.common.HelpCommand;
import de.klassenserver7b.k7bot.commands.types.TopLevelSlashCommand;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class HelpSlashCommand implements TopLevelSlashCommand {

	@Override
	public void performSlashCommand(SlashCommandInteraction event) {

		HelpCommand help = new HelpCommand();

		InteractionHook hook = event.deferReply(true).complete();

		hook.sendMessage("** look into your DM's **" + event.getUser().getAsMention()
				+ "\n (Only available if you have the option `get DM's from server members` in the `Privacy & Safety` settings enabled!)")
				.queue();

		OptionMapping category = event.getOption("category");

		MessageEmbed embed;

		if (category == null || category.getAsString().equalsIgnoreCase(HelpCategories.OVERVIEW.toString())) {
			embed = help.generateHelpOverview(event.getGuild());
		} else {
			embed = help.generateHelpforCategory(category.getAsString(), event.getGuild());
		}

		PrivateChannel ch = event.getUser().openPrivateChannel().complete();

		if (ch == null) {

			MessageEmbed errorEmbed = EmbedUtils.getErrorEmbed(
					"Couldn't send you a DM - please check if you have the option `get DM's from server members` in the `Privacy & Safety` settings enabled!")
					.build();

			hook.sendMessageEmbeds(errorEmbed).complete().delete().queueAfter(20, TimeUnit.SECONDS);
			return;
		}

		ch.sendMessageEmbeds(embed).queue();

	}

	@Override
	public @NotNull SlashCommandData getCommandData() {

		ArrayList<Choice> choices = new ArrayList<>();

		for (HelpCategories c : HelpCategories.values()) {

			if (c == HelpCategories.UNKNOWN) {
				continue;
			}

			choices.add(new Choice(c.toString(), c.toString()));

		}

		return Commands.slash("help", "Gibt dir die Hilfe-Liste aus.")
				.addOptions(new OptionData(OptionType.STRING, "category",
						"Wähle die Kategorie aus -> Overview für die Kategorieübersicht").setRequired(false)
						.addChoices(choices));
	}
}