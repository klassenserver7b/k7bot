package de.klassenserver7b.k7bot.commands.types;

import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

public interface TopLevelSlashCommand extends SlashCommand {
    @NotNull
    SlashCommandData getCommandData();
}