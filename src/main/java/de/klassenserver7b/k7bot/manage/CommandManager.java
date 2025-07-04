package de.klassenserver7b.k7bot.manage;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.common.*;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.logging.commands.common.SystemChannelCommand;
import de.klassenserver7b.k7bot.moderation.commands.common.*;
import de.klassenserver7b.k7bot.music.commands.common.*;
import de.klassenserver7b.k7bot.util.commands.common.*;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        this.commands.add(new RestartCommand());
        this.commands.add(new ShutdownCommand());

        // Util Commands
        this.commands.add(new ClearCommand());
        this.commands.add(new ReactRolesCommand());
        this.commands.add(new AddReactionCommand());
        this.commands.add(new MessagetoEmbedCommand());
        this.commands.add(new MemberInfoCommand());
        this.commands.add(new StatsCategoryCommand());

        // Moderation Commands
        this.commands.add(new WarnCommand());
        this.commands.add(new KickCommand());
        this.commands.add(new BanCommand());
        this.commands.add(new ModLogsCommand());
        this.commands.add(new MemberLogsCommand());
        this.commands.add(new TimeoutCommand());
        this.commands.add(new StopTimeoutCommand());

        // Musik Commands
        this.commands.add(new PlayCommand());
        this.commands.add(new StopCommand());
        this.commands.add(new PauseCommand());
        this.commands.add(new ResumeCommand());
        this.commands.add(new PlayNextCommand());
        this.commands.add(new AddQueueTrackCommand());
        this.commands.add(new SkipCommand());
        this.commands.add(new VolumeCommand());
        this.commands.add(new LoopCommand());
        this.commands.add(new UnLoopCommand());
        this.commands.add(new ShuffleCommand());
        this.commands.add(new QueuelistCommand());
        this.commands.add(new ClearQueueCommand());
        this.commands.add(new CurrentTrackInfoCommand());
        this.commands.add(new SeekCommand());
        this.commands.add(new SkipForwardCommand());
        this.commands.add(new SkipBackCommand());
        this.commands.add(new LyricsCommand());
        this.commands.add(new OverallChartsCommand());
        this.commands.add(new EqualizerCommand());
        this.commands.add(new NightcoreCommand());

        // Private
        this.commands.add(new UebersteuerungAdmin());

        if (K7Bot.getInstance().isDevMode()) {
            this.commands.add(new TestCommand());
        }

        commands.forEach(command -> {
            command.enableCommand();
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
     * @return 1 if command was found and executed, 0 if command is disabled, -1 if command was not found
     */
    public int perform(String command, Member m, GuildMessageChannel channel, Message message) {
        ServerCommand cmd;
        if ((cmd = this.mappedCommands.get(command.toLowerCase())) != null) {

            if (!cmd.isEnabled()) {
                return 0;
            }

            message.delete().queue();

            commandlog.info("see next lines:\n\nMember: {} | \nGuild: {} | \nChannel: {} | \nMessage: {}\n", m.getEffectiveName(), channel.getGuild().getName(), channel.getName(), message.getContentRaw());

            cmd.performCommand(m, channel, message);

            return 1;
        }

        return -1;
    }

    public boolean disableCommand(String command) {

        return disableCommand(mappedCommands.get(command));

    }

    public boolean disableCommand(ServerCommand command) {
        if (command == null || !command.isEnabled()) {
            return false;
        }

        command.disableCommand();
        return true;
    }

    public boolean disableCommandsByClass(Class<?> command) {

        List<ServerCommand> rem = getCommandsByClass(command);

        for (ServerCommand c : rem) {
            c.disableCommand();
        }

        return !rem.isEmpty();
    }

    public boolean enableCommand(String command) {
        return enableCommand(mappedCommands.get(command));
    }

    public boolean enableCommand(ServerCommand command) {

        if (command == null || command.isEnabled()) {
            return false;
        }

        command.enableCommand();
        return true;
    }

    public boolean enableCommandsByClass(Class<?> command) {

        List<ServerCommand> add = getCommandsByClass(command);

        for (ServerCommand c : add) {
            c.enableCommand();
        }

        return !add.isEmpty();
    }

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

    public String getNearestCommand(String str) {

        LevenshteinDistance dist = LevenshteinDistance.getDefaultInstance();
        String comm = "";
        int l = Integer.MAX_VALUE;

        for (String s : mappedCommands.keySet()) {

            Integer distance = dist.apply(s, str);

            if (distance < l) {

                l = distance;
                comm = s;

            }

        }

        return comm;

    }

    public ArrayList<ServerCommand> getCommands() {
        return commands;
    }
}
