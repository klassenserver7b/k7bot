/* (C)2026 */
package de.klassenserver7b.k7bot.util;

import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;

public class CommandUtils {

	@NotNull
	public static OptionMapping getRequiredOption(@NotNull SlashCommandInteraction event, @NotNull String name) {
		return Objects.requireNonNull(event.getOption(name), "Required option '" + name + "' is missing!");
	}
}
