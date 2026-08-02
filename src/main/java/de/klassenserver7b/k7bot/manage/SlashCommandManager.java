/* (C)2026 */
package de.klassenserver7b.k7bot.manage;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.audio.commands.slash.AudioSlashCommands;
import de.klassenserver7b.k7bot.commands.slash.logging.LoggingConfigSlashCommand;
import de.klassenserver7b.k7bot.commands.slash.logging.SystemChannelSlashCommand;
import de.klassenserver7b.k7bot.commands.slash.util.*;
import de.klassenserver7b.k7bot.commands.types.TopLevelSlashCommand;
import de.klassenserver7b.k7bot.database.dao.SlashCommandLogDAO;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

public class SlashCommandManager {

	public final ConcurrentHashMap<String, TopLevelSlashCommand> commands;
	public final Logger commandlog = LoggerFactory.getLogger("Commandlog");

	public SlashCommandManager() {

		this.commands = new ConcurrentHashMap<>();

		List<TopLevelSlashCommand> registerSchedule = new ArrayList<>();

		registerSchedule.add(new HelpSlashCommand());
		registerSchedule.add(new ClearSlashCommand());
		registerSchedule.add(new PingSlashCommand());
		registerSchedule.add(new ToEmbedSlashCommand());
		registerSchedule.add(new ReactRolesSlashCommand());
		registerSchedule.add(new TUNavigateSlashCommand());
		registerSchedule.add(new MemesChannelSlashCommand());
		registerSchedule.add(new LoggingConfigSlashCommand());
		registerSchedule.add(new SystemChannelSlashCommand());

		registerSchedule.addAll(AudioSlashCommands.getAllCommands());

		for (JDA shard : K7Bot.getInstance().getShardManager().getShards()) {
			CommandListUpdateAction commup = shard.updateCommands();

			for (TopLevelSlashCommand command : registerSchedule) {
				SlashCommandData cdata = command.getCommandData();

				if (command instanceof de.klassenserver7b.k7bot.commands.types.GuildSlashCommand) {
					cdata.setContexts(InteractionContextType.GUILD);
				}

				this.commands.put(cdata.getName(), command);
				// noinspection ResultOfMethodCallIgnored
				commup.addCommands(cdata);
			}

			commup.complete();
		}
	}

	public boolean perform(SlashCommandInteraction event) {
		TopLevelSlashCommand cmd = this.commands.get(event.getName().toLowerCase());

		if (cmd == null) {
			return false;
		}

		String guildName = "PRIVATE";
		Guild g = event.getGuild();
		if (g != null) {
			guildName = g.getName();
		}

		commandlog.info("""
				SlashCommand - see next lines:

				User: {} |\s
				Guild: {} |\s
				Channel: {} |\s
				Message: {}
				""", event.getUser().getName(), guildName, event.getChannel().getName(), event.getCommandString());

		new SlashCommandLogDAO().insertLog(event.getName(),
				((event.getGuild() != null) ? event.getGuild().getIdLong() : 0L), event.getUser().getIdLong(),
				Long.parseLong(event.getTimeCreated().format(DateTimeFormatter.ofPattern("uuuuMMddHHmmss"))),
				event.getCommandString());

		cmd.performSlashCommand(event);

		return true;
	}
}
