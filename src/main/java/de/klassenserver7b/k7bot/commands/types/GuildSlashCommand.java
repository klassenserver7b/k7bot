/* (C)2026 */
package de.klassenserver7b.k7bot.commands.types;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import org.jetbrains.annotations.NotNull;

public interface GuildSlashCommand extends TopLevelSlashCommand {

	@Override
	default void performSlashCommand(SlashCommandInteraction event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();

		if (guild == null || member == null) {
			event.reply("This command can only be used in a server!").setEphemeral(true).queue();
			return;
		}

		performGuildSlashCommand(event, guild, member);
	}

	void performGuildSlashCommand(@NotNull SlashCommandInteraction event, @NotNull Guild guild, @NotNull Member member);
}
