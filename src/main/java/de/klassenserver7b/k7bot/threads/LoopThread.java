/* (C)2026 */
package de.klassenserver7b.k7bot.threads;

import de.klassenserver7b.k7bot.K7Bot;
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

	private final Logger log;
	private final String[] status = new String[] { "-help", "-getprefix", "YouTube", "Spotify", "SlashCommands",
			"Logging" };
	private ScheduledFuture<?> refreshTask;

	public LoopThread() {
		log = LoggerFactory.getLogger(this.getClass());
	}

	public void start() {
		refreshTask = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this, 0, 10,
				java.util.concurrent.TimeUnit.MINUTES);
	}

	@Override
	public void run() {
		K7Bot INSTANCE = K7Bot.getInstance();

		INSTANCE.getLoopedEventManager().checkForUpdates();

		int i = new Random().nextInt(this.status.length);

		INSTANCE.getShardManager().getShards()
				.forEach(jda -> jda.getPresence().setActivity(Activity.listening(this.status[i])));
	}

	public void restart() {
		refreshTask.cancel(true);
		K7Bot.getInstance().getLoopedEventManager().restartAll();
		start();
		log.info("restarted");
	}

	public void stopLoop() {
		refreshTask.cancel(true);
		log.info("interrupted");
	}
}
