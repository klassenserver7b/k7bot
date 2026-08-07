/* (C)2026 */
package de.klassenserver7b.k7bot.listener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.common.util.HelpCommand;
import de.klassenserver7b.k7bot.database.dao.CommandLogDAO;
import de.klassenserver7b.k7bot.util.BotState;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * @author K7
 */
public class CommandListener extends ListenerAdapter {
	final Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {
		if (K7Bot.getInstance().getState() == BotState.STOPPING) {
			return;
		}

		switch (event.getChannelType()) {
			case PRIVATE -> privateMessageRecieved(event, event.getMessage());

			case CATEGORY, GROUP, UNKNOWN -> throw new IllegalStateException(
					"Message from illegal ChannelType" + event.getChannel(), new Throwable().fillInStackTrace());

			default -> {
				try {
					String prefix = K7Bot.getInstance().getPrefixMgr().getPrefix(event.getGuild().getIdLong());

					if (prefix == null) {
						prefix = "-";
					}

					prefix = prefix.toLowerCase();
					guildMessageRecieved(event, prefix);
				} catch (IllegalStateException e) {
					log.error(e.getMessage(), e);
				}
			}
		}
	}

	public void privateMessageRecieved(@NotNull MessageReceivedEvent event, Message message) {
		PrivateChannel channel = event.getChannel().asPrivateChannel();

		if (message.getContentStripped().startsWith("-help")) {
			HelpCommand help = new HelpCommand();
			inserttoLog("help", LocalDateTime.now(), 0L, event.getAuthor().getIdLong());
			help.performCommand(channel, message);
		}
	}

	public void guildMessageRecieved(@NotNull MessageReceivedEvent event, String prefix) {

		GuildMessageChannel channel = event.getChannel().asGuildMessageChannel();
		String messstr = event.getMessage().getContentRaw();

		switch (messstr) {
			case "-help" -> {
				K7Bot.getInstance().getCmdMgr().perform("help", event.getMember(), channel, event.getMessage());

				inserttoLog("help", LocalDateTime.now(), event.getGuild(), event.getAuthor().getIdLong());
			}

			case "-getprefix" -> {
				channel.sendMessage("The prefix for your Guild is: `" + prefix + "`.").queue();

				inserttoLog("getprefix", LocalDateTime.now(), event.getGuild(), event.getAuthor().getIdLong());
			}

			default -> {
				if (!messstr.startsWith(prefix) || messstr.isEmpty()) {
					return;
				}

				String[] args = messstr.substring(prefix.length()).split(" ");

				if (args.length < 1) {
					return;
				}

				int status = K7Bot.getInstance().getCmdMgr().perform(args[0], event.getMember(), channel,
						event.getMessage());

				switch (status) {
					case 0 -> sendDisabledCommand(channel, args[0]);
					case -1 -> sendUnknownCommand(channel, args[0]);
				}

				inserttoLog(args[0].replace("'", ""), LocalDateTime.now(), event.getGuild(),
						event.getAuthor().getIdLong());
			}
		}
	}

	private void sendDisabledCommand(GuildMessageChannel chan, String command) {

		String shortcommand = command;

		if (shortcommand.length() >= 100) {
			shortcommand = shortcommand.substring(0, 99);
			shortcommand += "...";
		}

		chan.sendMessage("`Deaktivierter Command - '" + shortcommand + "'` -> Currently disabled by the Bot-devs!")
				.complete().delete().queueAfter(15L, TimeUnit.SECONDS);
	}

	private void sendUnknownCommand(GuildMessageChannel chan, String command) {

		String nearestComm = K7Bot.getInstance().getCmdMgr().getNearestCommand(command);

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("`");
		stringBuilder.append("Unbekannter Command: ");
		stringBuilder.append("'");
		stringBuilder.append(command, 0, Math.max(command.length(), 99));
		stringBuilder.append("'`");
		if (nearestComm != null) {
			stringBuilder.append(" -> ");
			stringBuilder.append("Meintest du: ");
			stringBuilder.append("`");
			stringBuilder.append(nearestComm);
			stringBuilder.append("`?");
		}

		chan.sendMessage(stringBuilder.toString()).complete().delete().queueAfter(15L, TimeUnit.SECONDS);
	}

	private void inserttoLog(String command, LocalDateTime time, Guild guild, Long userid) {
		inserttoLog(command, time, guild.getIdLong(), userid);
	}

	private void inserttoLog(String command, LocalDateTime time, Long guildid, Long userid) {

		if (K7Bot.getInstance().getState() == BotState.STOPPING) {
			return;
		}
		new CommandLogDAO().insertLog(command, guildid, userid,
				Long.parseLong(time.format(DateTimeFormatter.ofPattern("uuuuMMddHHmmss"))));
	}
}
