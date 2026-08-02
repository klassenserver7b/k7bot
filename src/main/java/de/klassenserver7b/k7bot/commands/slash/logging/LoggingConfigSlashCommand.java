/* (C)2026 */
package de.klassenserver7b.k7bot.commands.slash.logging;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.types.GuildSlashCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import de.klassenserver7b.k7bot.logging.LoggingConfigEmbedProvider;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class LoggingConfigSlashCommand implements GuildSlashCommand {

	@SuppressWarnings({ "unused", "FieldCanBeLocal" })
	private final Logger log;

	/**
	 *
	 */
	public LoggingConfigSlashCommand() {
		log = LoggerFactory.getLogger(getClass());
	}

	@Override
	public void performGuildSlashCommand(@NotNull SlashCommandInteraction event, @NotNull Guild guild,
			@NotNull Member member) {
		InteractionHook hook = event.deferReply().complete();

		K7Bot.getInstance().getShardManager().addEventListener(new LoggingConfigEmbedProvider(hook));

	}

	@NotNull
	@Override
	public SlashCommandData getCommandData() {
		return Commands.slash("loggingconfig", "get an embed to configure logging")
				.setDefaultPermissions(DefaultMemberPermissions.DISABLED).setContexts(InteractionContextType.GUILD);
	}

}
