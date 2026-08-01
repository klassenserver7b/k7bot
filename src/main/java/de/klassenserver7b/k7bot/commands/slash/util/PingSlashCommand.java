package de.klassenserver7b.k7bot.commands.slash.util;

import de.klassenserver7b.k7bot.commands.types.TopLevelSlashCommand;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

public class PingSlashCommand implements TopLevelSlashCommand {

    @Override
    public void performSlashCommand(SlashCommandInteraction event) {

        InteractionHook hook = event.deferReply().complete();

        long gatewayPing = event.getJDA().getGatewayPing();
        long time = event.getJDA().getRestPing().complete();

        hook.sendMessageFormat("Pong! %dms", time, gatewayPing, "s").queue();

    }

    @Override
    public @NotNull SlashCommandData getCommandData() {
        return Commands.slash("ping", "Zeigt den aktuellen Ping des Bots");
    }

}
