/* (C)2026 */
package de.klassenserver7b.k7bot.listener;

import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;

import de.klassenserver7b.k7bot.K7Bot;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SlashCommandListener extends ListenerAdapter {

	@Override
	public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

		if (!K7Bot.getInstance().getslashMan().perform(event)) {
			event.getChannel().sendMessage("`unbekannter Slash-Command`").complete().delete().queueAfter(10L,
					TimeUnit.SECONDS);
		}
	}
}
