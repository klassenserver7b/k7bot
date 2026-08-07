/* (C)2026 */
package de.klassenserver7b.k7bot.manage;

import java.util.*;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.klassenserver7b.k7bot.commands.common.audio.AudioServerCommand;
import de.klassenserver7b.k7bot.commands.common.logging.SystemChannelCommand;
import de.klassenserver7b.k7bot.commands.common.moderation.*;
import de.klassenserver7b.k7bot.commands.common.uncategorized.TestCommand;
import de.klassenserver7b.k7bot.commands.common.util.*;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

/**
 * @author Klassenserver7b
 */
public class CommandManager {
	private final ArrayList<ServerCommand> commands;
	private final HashMap<String, ServerCommand> mappedCommands;

	private final Logger commandlog;

	/**
	 *
	 */
	public CommandManager() {
		this.commands = new ArrayList<>();
		this.mappedCommands = new HashMap<>();
		this.commandlog = LoggerFactory.getLogger("Commandlog");

		// Allgemein
		this.commands.add(new HelpCommand());
		this.commands.add(new PrefixCommand());
		this.commands.add(new PingCommand());
		this.commands.add(new VersionCommand());
		this.commands.add(new SystemChannelCommand());

		// Util Commands
		this.commands.add(new ClearCommand());
		this.commands.add(new ReactRolesCommand());
		this.commands.add(new AddReactionCommand());
		this.commands.add(new MessagetoEmbedCommand());
		this.commands.add(new MemberInfoCommand());

		// Moderation Commands
		this.commands.add(new WarnCommand());
		this.commands.add(new KickCommand());
		this.commands.add(new BanCommand());
		this.commands.add(new ModLogsCommand());
		this.commands.add(new MemberLogsCommand());
		this.commands.add(new TimeoutCommand());
		this.commands.add(new StopTimeoutCommand());

		this.commands.add(new TestCommand());
		this.commands.add(new AudioServerCommand());

		commands.forEach(command -> {
			for (String s : command.getCommandStrings()) {
				mappedCommands.put(s, command);
			}
		});
	}

	/**
	 * @param command Command
	 * @param m       Member
	 * @param channel Channel
	 * @param message Message
	 * @return 1 if command was found and executed, 0 if command is disabled, -1 if
	 *         command was not found
	 */
	public int perform(String command, Member m, GuildMessageChannel channel, Message message) {
		ServerCommand cmd;
		if ((cmd = this.mappedCommands.get(command.toLowerCase())) != null) {

			message.delete().queue();

			commandlog.info("see next lines:\n\nMember: {} | \nGuild: {} | \nChannel: {} | \nMessage: {}\n",
					m.getEffectiveName(), channel.getGuild().getName(), channel.getName(), message.getContentRaw());

			cmd.performCommand(m, channel, message);

			return 1;
		}

		return -1;
	}

	@SuppressWarnings("unused")
	public List<ServerCommand> getCommandsByClass(Class<?> command) {

		ArrayList<ServerCommand> add = new ArrayList<>();

		for (ServerCommand serverCommand : commands) {
			if (serverCommand.getClass().isAssignableFrom(command)
					|| serverCommand.getClass().getCanonicalName().equalsIgnoreCase(command.getCanonicalName())) {
				add.add(serverCommand);
			}
		}

		return add;
	}

	public @Nullable String getNearestCommand(String str) {
		LevenshteinDistance dist = LevenshteinDistance.getDefaultInstance();
		Optional<String> nearestCommand = mappedCommands.keySet().stream()
				.min(Comparator.comparingInt(s -> dist.apply(str, s)));
		return nearestCommand.orElse(null);
	}

	public ArrayList<ServerCommand> getCommands() {
		return commands;
	}
}
