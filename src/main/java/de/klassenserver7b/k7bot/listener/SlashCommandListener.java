package de.klassenserver7b.k7bot.listener;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.common.HelpCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class SlashCommandListener extends ListenerAdapter {
    HelpCommand help = new HelpCommand();

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

        if (!K7Bot.getInstance().getslashMan().perform(event)) {
            event.getChannel().sendMessage("`unbekannter Slash-Command`").complete().delete().queueAfter(10L,
                    TimeUnit.SECONDS);
        }
    }
}
