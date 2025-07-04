/**
 *
 */
package de.klassenserver7b.k7bot.music.commands.slash;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.types.TopLevelSlashCommand;
import de.klassenserver7b.k7bot.music.utilities.BotAudioEffectsManager;
import de.klassenserver7b.k7bot.music.utilities.MusicUtil;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import de.klassenserver7b.k7bot.util.GenericMessageSendHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public class ClearAudioFilterFilterSlashCommand implements TopLevelSlashCommand {

    /**
     *
     */
    public ClearAudioFilterFilterSlashCommand() {

    }

    @Override
    public void performSlashCommand(SlashCommandInteraction event) {

        InteractionHook hook = event.deferReply(false).complete();

        Member m = event.getMember();
        assert m != null;

        AudioChannel vc = MusicUtil.getMembVcConnection(m);

        if (MusicUtil.membFailsDefaultConditions(new GenericMessageSendHandler(hook), m) || vc == null) {
            return;
        }

        BotAudioEffectsManager effman = BotAudioEffectsManager.getAudioEffectsManager(
                K7Bot.getInstance().getPlayerUtil().getController(vc.getGuild().getIdLong()).getPlayer());

        effman.clearFilters();


        assert event.getGuild() != null;

        hook.sendMessageEmbeds(EmbedUtils
                        .getSuccessEmbed("Successfully removed all AudioFilters", event.getGuild().getIdLong()).build())
                .queue();

    }

    @NotNull
    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("audiofilterclear", "removes all audio filters from the current player")
                .setGuildOnly(true);
    }

}
