/**
 *
 */
package de.klassenserver7b.k7bot.threads;

import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.slash.StableDiffusionCommand;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.sql.LiteSQL;
import de.klassenserver7b.k7bot.util.StatsCategoryUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Klassenserver7b
 */
public class ConsoleReadThread implements Runnable {

    private final Thread t;
    private final Logger log;
    private final Console console;

    public ConsoleReadThread() {
        log = LoggerFactory.getLogger(this.getClass());

        console = System.console();

        t = new Thread(this, "ConsoleReadThread");
        t.start();
    }

    @Override
    public void run() {

        while (!t.isInterrupted()) {
            try {

                String line;
                if ((line = console.readLine()) != null) {
                    interpretConsoleContent(line);
                }

            } catch (IOException e) {

                if (e.getMessage().equalsIgnoreCase("Stream closed")) {
                    t.interrupt();
                    break;
                }
                log.info("ConsoleRead Thread interrupted");
            }
        }

    }

    public void interpretConsoleContent(String s) throws IOException {

        String[] commandargs = s.split(" ");

        switch (commandargs[0].toLowerCase()) {
            case "exit", "stop" -> {

                K7Bot.getInstance().setExit(true);
                t.interrupt();
                this.onShutdown();
            }

            case "enablecommand" -> changeCommandState(true, commandargs[1]);

            case "disablecommand" -> changeCommandState(false, commandargs[1]);

            case "addaiuser" -> addAIUser(commandargs[1]);

            case "rmaiuser" -> removeAIUser(commandargs[1]);

            default -> System.out.println("Use exit/stop to Shutdown");

        }

    }

    public void changeCommandState(boolean enable, String command_OR_CommandClassName) {

        Class<?> insertedClassName = getClassFromString(command_OR_CommandClassName);

        if (insertedClassName != null) {

            try {

                if (!ServerCommand.class.isAssignableFrom(insertedClassName)) {
                    log.warn("Invalid CommandClassName");
                    return;
                }

                if (enable) {
                    K7Bot.getInstance().getCmdMan().enableCommandsByClass(insertedClassName);
                } else {
                    K7Bot.getInstance().getCmdMan().disableCommandsByClass(insertedClassName);
                }

            } catch (IllegalArgumentException | SecurityException e) {
                log.error(e.getMessage(), e);
            }

        } else {

            if (enable) {
                enableCommandByStr(command_OR_CommandClassName);
            } else {
                disableCommandByStr(command_OR_CommandClassName);
            }
        }
    }

    public void onShutdown() {

        log.info("Bot is shutting down!");

        ShardManager shardMgr = K7Bot.getInstance().getShardManager();

        for (AudioSourceManager m : K7Bot.getInstance().getAudioPlayerManager().getSourceManagers()) {
            m.shutdown();
        }

        if (shardMgr != null) {

            ArrayList<Object> listeners = new ArrayList<>();

            for (JDA jda : shardMgr.getShards()) {
                listeners.addAll(jda.getEventManager().getRegisteredListeners());
            }

            shardMgr.removeEventListener(listeners.toArray());

            K7Bot.getInstance().stopLoop();

            K7Bot.getInstance().getLoopedEventManager().shutdownLoopedEvents();

            StatsCategoryUtil.onShutdown(K7Bot.getInstance().isDevMode());

            shardMgr.setStatus(OnlineStatus.OFFLINE);

            shardMgr.shutdown();
            log.info("Bot offline");

            LiteSQL.disconnect();
            t.interrupt();
            return;

        }

        log.info("ShardMan was null!");

    }

    protected void addAIUser(String uid) {
        Long userid = Long.valueOf(uid);
        StableDiffusionCommand.addAIUser(userid);
        log.info("successfully added {}to ai allowlist", uid);
    }

    protected void removeAIUser(String uid) {
        Long userid = Long.valueOf(uid);
        StableDiffusionCommand.removeAIUser(userid);
        log.info("successfully removed {}from ai allowlist", uid);
    }

    protected void disableCommandByStr(String name) {
        if (K7Bot.getInstance().getCmdMan().disableCommand(name)) {
            log.info("successfully disabled {}", name);
            return;
        }

        log.warn("failed to disable {}", name);
    }

    protected void enableCommandByStr(String name) {
        if (K7Bot.getInstance().getCmdMan().enableCommand(name)) {
            log.info("successfully enabled {}", name);
            return;
        }

        log.warn("failed to enable {}", name);
    }


    public Class<?> getClassFromString(String s) {
        try {
            return Class.forName(s);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    @SuppressWarnings("unused")
    protected static Set<Class<?>> getAllExtendedOrImplementedInterfacesRecursively(Class<?> clazz) {

        Set<Class<?>> res = new HashSet<>();
        Class<?>[] interfaces = clazz.getInterfaces();

        if (interfaces.length > 0) {
            res.addAll(Arrays.asList(interfaces));

            for (Class<?> interfaze : interfaces) {
                res.addAll(getAllExtendedOrImplementedInterfacesRecursively(interfaze));
            }
        }

        return res;
    }

}
