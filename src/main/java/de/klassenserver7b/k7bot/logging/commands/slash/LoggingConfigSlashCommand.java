/**
 *
 */
package de.klassenserver7b.k7bot.logging.commands.slash;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.types.TopLevelSlashCommand;
import de.klassenserver7b.k7bot.logging.LoggingConfigEmbedProvider;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class LoggingConfigSlashCommand implements TopLevelSlashCommand {

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final Logger log;

    /**
     *
     */
    public LoggingConfigSlashCommand() {
        log = LoggerFactory.getLogger(getClass());
    }

    @Override
    public void performSlashCommand(SlashCommandInteraction event) {
        InteractionHook hook = event.deferReply().complete();

        K7Bot.getInstance().getShardManager().addEventListener(new LoggingConfigEmbedProvider(hook));

    }

    @NotNull
    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("loggingconfig", "get an embed to configure logging")
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED).setContexts(InteractionContextType.GUILD);
    }

}
