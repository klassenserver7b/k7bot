package de.klassenserver7b.k7bot.manage;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.slash.logging.LoggingConfigSlashCommand;
import de.klassenserver7b.k7bot.commands.slash.logging.SystemChannelSlashCommand;
import de.klassenserver7b.k7bot.commands.slash.subscriptions.SubscribeSlashCommand;
import de.klassenserver7b.k7bot.commands.slash.subscriptions.UnsubscribeSlashCommand;
import de.klassenserver7b.k7bot.commands.slash.util.*;
import de.klassenserver7b.k7bot.commands.types.TopLevelSlashCommand;
import de.klassenserver7b.k7bot.sql.LiteSQL;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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
        registerSchedule.add(new SubscribeSlashCommand());
        registerSchedule.add(new UnsubscribeSlashCommand());
        registerSchedule.add(new VotingCommand());
        registerSchedule.add(new TUNavigateSlashCommand());
        registerSchedule.add(new MemesChannelSlashCommand());
        registerSchedule.add(new LoggingConfigSlashCommand());
        registerSchedule.add(new SystemChannelSlashCommand());

        for (JDA shard : K7Bot.getInstance().getShardManager().getShards()) {
            CommandListUpdateAction commup = shard.updateCommands();

            for (TopLevelSlashCommand command : registerSchedule) {
                SlashCommandData cdata = command.getCommandData();
                this.commands.put(cdata.getName(), command);
                //noinspection ResultOfMethodCallIgnored
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

        String guild = "PRIVATE";
        if (event.isFromGuild()) {
            guild = event.getGuild().getName();
        }

        commandlog.info("SlashCommand - see next lines:\n\nUser: {} | \nGuild: {} | \nChannel: {} | \nMessage: {}\n", event.getUser().getName(), guild, event.getChannel().getName(), event.getCommandString());

        LiteSQL.onUpdate(
                "INSERT INTO slashcommandlog (command, guildId, userId, timestamp, commandstring) VALUES (?, ?, ?, ?, ?)",
                event.getName(), ((event.getGuild() != null) ? event.getGuild().getIdLong() : 0),
                event.getUser().getIdLong(),
                event.getTimeCreated().format(DateTimeFormatter.ofPattern("uuuuMMddHHmmss")),
                event.getCommandString());

        cmd.performSlashCommand(event);

        return true;
    }
}
