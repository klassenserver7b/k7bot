/* (C)2026 */
package de.klassenserver7b.k7bot.commands.types;

import org.jetbrains.annotations.NotNull;

import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public interface TopLevelSlashCommand extends SlashCommand {
	@NotNull
	SlashCommandData getCommandData();
}