/**
 * 
 */
package de.klassenserver7b.k7bot.commands.types;

import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

/**
 * @author K7
 *
 */
public interface SubSlashCommand extends SlashCommand {
	@NotNull
    SubcommandData getSubCommandData();

	String getSubPath();

}
