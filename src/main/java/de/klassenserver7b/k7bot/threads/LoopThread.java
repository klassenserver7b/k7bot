/**
 *
 */
package de.klassenserver7b.k7bot.threads;

import de.klassenserver7b.k7bot.Klassenserver7bbot;
import de.klassenserver7b.k7bot.util.StatsCategoryUtil;
import net.dv8tion.jda.api.entities.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;

/**
 * @author Klassenserver7b
 */
public class LoopThread implements Runnable {

    private ScheduledFuture<?> refreshTask;
    private final Logger log;
    private boolean hasstarted;
    private final String[] status = new String[]{"-help", "-getprefix", "YouTube", "Spotify", "SlashCommands",
            "Logging"};

    public LoopThread() {
        log = LoggerFactory.getLogger(this.getClass());
        hasstarted = false;


    }

    public void start() {
        refreshTask = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this, 0, 10, java.util.concurrent.TimeUnit.MINUTES);
    }

    @Override
    public void run() {
        Klassenserver7bbot INSTANCE = Klassenserver7bbot.getInstance();

        if ((!this.hasstarted)) {
            StatsCategoryUtil.onStartup(INSTANCE.isDevMode());
            this.hasstarted = true;
        }

        INSTANCE.getLoopedEventManager().checkForUpdates();

        int i = new Random().nextInt(this.status.length);

        INSTANCE.getShardManager().getShards().forEach(jda -> jda.getPresence().setActivity(Activity.listening(this.status[i])));
    }


    public void restart() {
        refreshTask.cancel(true);
        Klassenserver7bbot.getInstance().getLoopedEventManager().restartAll();
        start();
        log.info("restarted");
    }

    public void stopLoop() {
        refreshTask.cancel(true);
        log.info("interrupted");
    }

}
