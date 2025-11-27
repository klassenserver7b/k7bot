package de.klassenserver7b.k7bot.manage;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.slash.*;
import de.klassenserver7b.k7bot.commands.types.TopLevelSlashCommand;
import de.klassenserver7b.k7bot.logging.commands.slash.LoggingConfigSlashCommand;
import de.klassenserver7b.k7bot.logging.commands.slash.SystemChannelSlashCommand;
import de.klassenserver7b.k7bot.music.commands.slash.*;
import de.klassenserver7b.k7bot.sql.LiteSQL;
import de.klassenserver7b.k7bot.subscriptions.commands.SubscribeSlashCommand;
import de.klassenserver7b.k7bot.subscriptions.commands.UnSubscribeSlashCommand;
import de.klassenserver7b.k7bot.tu.commands.slash.TUNavigateSlashCommand;
import de.klassenserver7b.k7bot.util.commands.slash.ClearSlashCommand;
import de.klassenserver7b.k7bot.util.commands.slash.MemesChannelSlashCommand;
import de.klassenserver7b.k7bot.util.commands.slash.ReactRolesSlashCommand;
import de.klassenserver7b.k7bot.util.commands.slash.ToEmbedSlashCommand;
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

        List<TopLevelSlashCommand> registerschedule = new ArrayList<>();

        registerschedule.add(new HelpSlashCommand());
        registerschedule.add(new ClearSlashCommand());
        registerschedule.add(new Shutdownslashcommand());
        registerschedule.add(new PingSlashCommand());
        registerschedule.add(new ToEmbedSlashCommand());
        registerschedule.add(new ReactRolesSlashCommand());
        registerschedule.add(new PlaySlashCommandSplitter());
        registerschedule.add(new ChartsSlashCommand());
        registerschedule.add(new SubscribeSlashCommand());
        registerschedule.add(new UnSubscribeSlashCommand());
        registerschedule.add(new EqualizerSlashCommand());
        registerschedule.add(new VotingCommand());
        registerschedule.add(new TUNavigateSlashCommand());
        registerschedule.add(new SpeedChangeCommand());
        registerschedule.add(new ClearAudioFilterFilterSlashCommand());
        registerschedule.add(new StableDiffusionCommand());
        registerschedule.add(new TanzmusikAISlashCommand());
        registerschedule.add(new MemesChannelSlashCommand());
        registerschedule.add(new LoggingConfigSlashCommand());
        registerschedule.add(new SystemChannelSlashCommand());

        for (JDA shard : K7Bot.getInstance().getShardManager().getShards()) {
            CommandListUpdateAction commup = shard.updateCommands();

            for (TopLevelSlashCommand command : registerschedule) {
                SlashCommandData cdata = command.getCommandData();
                this.commands.put(cdata.getName(), command);
                //noinspection ResultOfMethodCallIgnored
                commup.addCommands(cdata);
            }

            commup.complete();

        }
    }

    public boolean perform(SlashCommandInteraction event) {
        TopLevelSlashCommand cmd;
        if ((cmd = this.commands.get(event.getName().toLowerCase())) != null) {

            String guild = "PRIVATE";
            if (event.getGuild() != null) {
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
        return false;
    }
}
