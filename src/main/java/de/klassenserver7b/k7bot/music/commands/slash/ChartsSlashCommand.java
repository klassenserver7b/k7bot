package de.klassenserver7b.k7bot.music.commands.slash;

import de.klassenserver7b.k7bot.commands.types.TopLevelSlashCommand;
import de.klassenserver7b.k7bot.music.commands.generic.GenericChartsCommand;
import de.klassenserver7b.k7bot.music.utilities.ChartList;
import de.klassenserver7b.k7bot.util.GenericMessageSendHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChartsSlashCommand extends GenericChartsCommand implements TopLevelSlashCommand {

    @Override
    public void performSlashCommand(SlashCommandInteraction event) {
        InteractionHook hook = event.deferReply(false).complete();
        OptionMapping timeopt = event.getOption("time");
        OptionMapping timeunitopt = event.getOption("timeunit");
        OptionMapping guildopt = event.getOption("guild");

        HashMap<String, Long> charts = provideOptionselectedCharts(timeopt, timeunitopt, guildopt, event.getGuild(),
                hook);

        if (charts != null) {
            if (charts.isEmpty()) {
                hook.sendMessage(
                                "There are no Charts for the selected Options! (or something went wrong but who could think this 😅)")
                        .queue();
            } else {
                sendMessage(new GenericMessageSendHandler(hook), charts);
            }
        }

    }

    private HashMap<String, Long> provideOptionselectedCharts(OptionMapping timeopt, OptionMapping timeunitopt,
                                                              OptionMapping guildopt, Guild guild, InteractionHook hook) {

        HashMap<String, Long> sheduledcharts = new HashMap<>();
        ChartList chartlist = new ChartList();

        if (guildopt != null && guildopt.getAsBoolean()) {

            if (timeopt != null) {

                if (timeunitopt != null) {

                    Long time = timeopt.getAsLong();

                    try {

                        ChronoUnit u = ChronoUnit.valueOf(timeunitopt.getAsString());

                        sheduledcharts = chartlist.getcharts(guild, time, u);

                    } catch (Exception e) {
                        hook.sendMessage("Using the option \"time\" requires a valid TimeUnit").queue();
                    }

                }

            } else {
                sheduledcharts = chartlist.getcharts(guild);
            }

        } else {

            if (timeopt != null) {

                if (timeunitopt != null) {

                    Long time = timeopt.getAsLong();

                    try {
                        ChronoUnit u = ChronoUnit.valueOf(timeunitopt.getAsString().toLowerCase());

                        sheduledcharts = chartlist.getcharts(time, u);

                    } catch (Exception e) {
                        hook.sendMessage("Using the option \"time\" requires a valid TimeUnit").queue();
                    }
                }
            } else {

                sheduledcharts = chartlist.getcharts();

            }

        }

        return sheduledcharts;

    }

    @Override
    public @NotNull SlashCommandData getCommandData() {

        List<Choice> choices = new ArrayList<>();
        for (ChronoUnit t : ChronoUnit.values()) {

            if (t == ChronoUnit.YEARS || t == ChronoUnit.MONTHS || t == ChronoUnit.DAYS || t == ChronoUnit.WEEKS) {
                choices.add(new Choice(t.toString(), t.toString()));
            }

        }

        return Commands.slash("charts", "Liefert die Bot-Music Charts für die gewählten Parameter")
                .addOption(OptionType.BOOLEAN, "guild",
                        "true wenn nur die charts für die aktuelle guild angefordert werden sollen")
                .addOption(OptionType.INTEGER, "time",
                        "REQUIRES TIMEUNIT! - Wie viele TimeUnits soll der Bot zur Chartbestimmung berücksichtigen")
                .addOptions(new OptionData(OptionType.STRING, "timeunit", "see choices").addChoices(choices))
                .setGuildOnly(true);
    }

}
