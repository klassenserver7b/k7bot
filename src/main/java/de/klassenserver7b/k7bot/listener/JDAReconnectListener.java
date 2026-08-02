/* (C)2026 */
package de.klassenserver7b.k7bot.listener;

import org.jetbrains.annotations.NotNull;

import de.klassenserver7b.k7bot.K7Bot;
import net.dv8tion.jda.api.events.session.SessionRecreateEvent;
import net.dv8tion.jda.api.events.session.SessionResumeEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * @author K7
 */
public class JDAReconnectListener extends ListenerAdapter {

	@Override
	public void onSessionRecreate(@NotNull SessionRecreateEvent event) {
		K7Bot.getInstance().restart();
	}

	@Override
	public void onSessionResume(@NotNull SessionResumeEvent event) {
		K7Bot.getInstance().restart();
	}
}
