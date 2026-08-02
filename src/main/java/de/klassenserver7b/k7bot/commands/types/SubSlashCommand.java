/* (C)2026 */
package de.klassenserver7b.k7bot.commands.types;

import org.jetbrains.annotations.NotNull;

import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

/**
 * @author K7
 *
 */
@SuppressWarnings("unused")
public interface SubSlashCommand extends SlashCommand {
	@NotNull
	SubcommandData getSubCommandData();

	String getSubPath();

}
