/**
 *
 */
package de.klassenserver7b.k7bot.music.commands.slash;

import com.github.natanbc.lavadsp.timescale.TimescalePcmAudioFilter;
import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.types.TopLevelSlashCommand;
import de.klassenserver7b.k7bot.music.utilities.BotAudioEffectsManager;
import de.klassenserver7b.k7bot.music.utilities.BotAudioEffectsManager.FilterTypes;
import de.klassenserver7b.k7bot.music.utilities.MusicUtil;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import de.klassenserver7b.k7bot.util.GenericMessageSendHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author K7
 */
public class SpeedChangeCommand implements TopLevelSlashCommand {

    /**
     *
     */
    public SpeedChangeCommand() {
    }

    @Override
    public void performSlashCommand(SlashCommandInteraction event) {

        InteractionHook hook = event.deferReply(true).complete();

        Member m = event.getMember();

        assert m != null;
        assert event.getGuild() != null;

        AudioChannel vc = MusicUtil.getMembVcConnection(m);
        if (MusicUtil.membFailsDefaultConditions(new GenericMessageSendHandler(hook), m)) {
            return;
        }

        OptionMapping speedmap = event.getOption("speedfactor");
        assert speedmap != null;
        double speedrate = speedmap.getAsDouble();

        OptionMapping pitchmap = event.getOption("changepitch");
        boolean changepitch = (pitchmap == null || pitchmap.getAsBoolean());

        BotAudioEffectsManager effman = BotAudioEffectsManager.getAudioEffectsManager(
                K7Bot.getInstance().getPlayerUtil().getController(Objects.requireNonNull(vc).getGuild().getIdLong()).getPlayer());

        if (speedrate == 1.0) {
            effman.removeAudioFilterFunction(FilterTypes.SPEED);
            hook.sendMessageEmbeds(EmbedUtils
                            .getSuccessEmbed("Successfully removed speed change", event.getGuild().getIdLong()).build())
                    .queue();
            return;
        }

        effman.addAudioFilterFunction(FilterTypes.SPEED, ((track, format, output) -> {

            TimescalePcmAudioFilter timefilter = new TimescalePcmAudioFilter(output, format.channelCount,
                    format.sampleRate);

            if (changepitch) {
                timefilter.setRate(speedrate);
            } else {
                timefilter.setSpeed(speedrate);
            }

            return timefilter;
        }));

        hook.sendMessageEmbeds(
                        EmbedUtils.getSuccessEmbed("Successfully applied speed change", event.getGuild().getIdLong()).build())
                .queue();
    }

    @NotNull
    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("speedchange", "changes the speed of the currently played audio track").addOptions(
                        new OptionData(OptionType.NUMBER, "speedfactor", "factor to multyply the speed with e.g. 1.5", true)
                                .setRequiredRange(0, 2),
                        new OptionData(OptionType.BOOLEAN, "changepitch", "whether the pitch should be changed default: true",
                                false))
                .setGuildOnly(true);
    }

}
