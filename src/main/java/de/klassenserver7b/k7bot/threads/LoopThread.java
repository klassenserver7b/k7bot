/* (C)2026 */
package de.klassenserver7b.k7bot.threads;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.klassenserver7b.k7bot.K7Bot;
import net.dv8tion.jda.api.entities.Activity;

/**
 * @author Klassenserver7b
 */
public class LoopThread implements Runnable {

	private final Logger log;
	private final String[] status = new String[] { "-help", "-getprefix", "YouTube", "Spotify", "SlashCommands",
			"Logging" };
	private ScheduledExecutorService executorService;
	private ScheduledFuture<?> refreshTask;

	public LoopThread() {
		log = LoggerFactory.getLogger(this.getClass());
	}

	public synchronized void start() {
		if (executorService != null && !executorService.isShutdown()) {
			return;
		}
		executorService = Executors.newSingleThreadScheduledExecutor();
		refreshTask = executorService.scheduleAtFixedRate(this, 0, 10, TimeUnit.MINUTES);
	}

	@Override
	public void run() {
		try {
			K7Bot INSTANCE = K7Bot.getInstance();

			INSTANCE.getLoopedEventManager().checkForUpdates();

			int i = new Random().nextInt(this.status.length);

			INSTANCE.getShardManager().getShards()
					.forEach(jda -> jda.getPresence().setActivity(Activity.listening(this.status[i])));
		} catch (Exception e) {
			log.error("An error occurred during LoopThread execution", e);
		}
	}

	@SuppressWarnings("unused")
	public synchronized void restart() {
		stopLoop();
		K7Bot.getInstance().getLoopedEventManager().restartAll();
		start();
		log.info("restarted");
	}

	public synchronized void stopLoop() {
		if (refreshTask != null) {
			refreshTask.cancel(true);
		}
		if (executorService != null) {
			executorService.shutdownNow();
			executorService = null;
		}
		log.info("interrupted");
	}
}
