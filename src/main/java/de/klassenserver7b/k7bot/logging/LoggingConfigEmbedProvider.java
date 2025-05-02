/**
 *
 */
package de.klassenserver7b.k7bot.logging;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import de.klassenserver7b.k7bot.util.KAutoCloseable;
import de.klassenserver7b.k7bot.util.customapis.types.LoopedEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class LoggingConfigEmbedProvider extends ListenerAdapter {

    private InteractionHook hook;
    private final long guildId;
    private LoggingOptions category;
    private final LoopedEvent timeoutCheckEvent;

    /**
     *
     */
    public LoggingConfigEmbedProvider(InteractionHook hook) {
        this.hook = hook;
        this.guildId = hook.getInteraction().getGuild().getIdLong();

        try (KAutoCloseable ignored = LoggingFilter.getInstance().blockEventExecution()) {
            Message m = hook.sendMessageEmbeds(buildCatSelectEmbed()).setComponents(buildCatSelectActionRows()).complete();
            LoggingFilter.getInstance().getLoggingBlocker().block(m.getIdLong());
        }

        timeoutCheckEvent = new HookTimeoutLoop("logging-config-" + guildId + "-" + System.currentTimeMillis(), this);
        K7Bot.getInstance().getLoopedEventManager().registerEvent(timeoutCheckEvent, true);
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {

        if (!event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            return;
        }

        hook = event.deferEdit().complete();
        String compId = event.getComponentId();
        String matcher = compId.replaceAll("(.*)-(\\d+)?$", "$1");
        int optId = Integer.parseInt(event.getSelectedOptions().getFirst().getValue().replace("logging-catid-", ""));

        switch (matcher) {

            case "logging-single-select" -> LoggingConfigDBHandler.toggleOption(LoggingOptions.byId(optId), guildId);

            case "logging-choose-category" -> category = LoggingOptions.byId(optId);

            default -> {
                return;
            }
        }

        sendCatOptionsEmbed();

    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {

        if (!event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            return;
        }

        hook = event.deferEdit().complete();
        String compId = event.getComponentId();

        if (compId.equalsIgnoreCase("logging-conf-exit")) {
            exit();
            return;
        }

        if (!compId.startsWith("logging-cat-")) {
            return;
        }

        compId = compId.replace("logging-cat-", "");
        LoggingOptions option = LoggingOptions.byId(Integer.parseInt(compId.split("-")[1]));

        switch (compId.split("-")[0]) {

            case "disableall" -> changeCat(option, false);

            case "enableall" -> changeCat(option, true);

            case "back" -> {
                sendCatSelectEmbed();
                return;
            }

            default -> {
                return;
            }
        }

        sendCatOptionsEmbed();

    }

    protected void exit() {
        try (KAutoCloseable ignored = LoggingFilter.getInstance().blockEventExecution(hook.retrieveOriginal().complete().getIdLong())) {
            hook.deleteOriginal().queue();
        }
        K7Bot.getInstance().getShardManager().removeEventListener(this);
        K7Bot.getInstance().getLoopedEventManager().removeEvent(timeoutCheckEvent);
    }

    protected void sendCatSelectEmbed() {
        hook.editOriginalEmbeds(buildCatSelectEmbed()).setComponents(buildCatSelectActionRows()).queue();
    }

    protected MessageEmbed buildCatSelectEmbed() {
        EmbedBuilder embbuild = EmbedUtils.getDefault();
        embbuild.setTitle("Logging Config");
        embbuild.setColor(Color.blue);

        StringBuilder strbuild = new StringBuilder();
        strbuild.append("Please select the type of Logging you want to configure");
        strbuild.append("\n\n");
        strbuild.append("Available are:");
        strbuild.append("\n");

        for (LoggingOptions option : LoggingOptions.values()) {
            if (option.getId() % 10 == 0) {
                strbuild.append(option);
                strbuild.append(",");
                strbuild.append("\n");
            }
        }

        embbuild.setDescription(strbuild);

        return embbuild.build();
    }

    protected List<LayoutComponent> buildCatSelectActionRows() {

        List<LayoutComponent> rows = new LinkedList<>();
        List<ItemComponent> strSelect = new LinkedList<>();

        StringSelectMenu.Builder strSelectBuilder = StringSelectMenu.create("logging-choose-category");

        for (LoggingOptions option : LoggingOptions.values()) {
            if (option.getId() % 10 == 0) {
                strSelectBuilder.addOption(option.toString(), "logging-catid-" + option.getId());
            }
        }

        strSelect.add(strSelectBuilder.build());

        rows.add(ActionRow.of(strSelect));
        rows.add(ActionRow.of(Button.danger("logging-conf-exit", "Exit")));

        return rows;
    }

    protected void sendCatOptionsEmbed() {

        int idRange = category.getId();

        List<Integer> catids = Arrays.stream(LoggingOptions.values())
                .filter(opt -> (opt.getId() > idRange && opt.getId() < idRange + 10)).map(LoggingOptions::getId).toList();

        hook.editOriginalEmbeds(buildCatOptionsEmbed(catids)).setComponents(buildCatOptionsActionRows(catids)).queue();
    }

    protected MessageEmbed buildCatOptionsEmbed(List<Integer> catids) {

        String catname = category.toString();

        EmbedBuilder embbuild = EmbedUtils.getDefault();
        embbuild.setColor(Color.blue);
        embbuild.setTitle("LoggingConfig - " + catname);

        StringBuilder strbuild = new StringBuilder();
        strbuild.append("Option");

        strbuild.append(" ".repeat(30));

        strbuild.append(" - State");
        strbuild.append("\n\n");

        for (int catid : catids) {

            LoggingOptions opt = LoggingOptions.byId(catid);

            strbuild.append("`");
            strbuild.append(opt.toString());

            strbuild.append(" ".repeat(Math.max(0, 30 - opt.toString().toCharArray().length)));

            strbuild.append(" - ");
            strbuild.append("`");

            boolean enabled = !LoggingConfigDBHandler.isOptionDisabled(opt, guildId);
            strbuild.append((enabled ? ":white_check_mark:" : ":x:"));

            strbuild.append("\n");

        }

        embbuild.setDescription(strbuild);

        return embbuild.build();
    }

    protected List<LayoutComponent> buildCatOptionsActionRows(List<Integer> catIds) {

        int idRange = category.getId();

        List<ItemComponent> buttonRow = new LinkedList<>();

        buttonRow.add(Button.primary("logging-cat-enableall-" + idRange, "Enable All"));
        buttonRow.add(Button.primary("logging-cat-disableall-" + idRange, "Disable All"));
        buttonRow.add(Button.danger("logging-cat-back-00", "Back"));

        StringSelectMenu.Builder strSelectBuilder = StringSelectMenu.create("logging-single-select");

        for (int catid : catIds) {
            strSelectBuilder.addOption(LoggingOptions.byId(catid).toString(), "logging-catid-" + catid);
        }

        List<LayoutComponent> rows = new LinkedList<>();
        rows.add(ActionRow.of(buttonRow));
        rows.add(ActionRow.of(strSelectBuilder.build()));

        return rows;
    }

    protected void changeCat(LoggingOptions cat, boolean enable) {

        for (int optId = cat.getId() + 1; optId < cat.getId() + 10; optId++) {

            LoggingOptions option;
            if ((option = LoggingOptions.byId(optId)) != LoggingOptions.UNKNOWN) {

                if (enable) {
                    LoggingConfigDBHandler.enableOption(option, guildId);
                } else {
                    LoggingConfigDBHandler.disableOption(option, guildId);
                }
            }

        }

    }

    static class HookTimeoutLoop implements LoopedEvent {

        private final String identifier;
        private final LoggingConfigEmbedProvider listener;

        public HookTimeoutLoop(String identifier, LoggingConfigEmbedProvider listener) {
            this.identifier = identifier;
            this.listener = listener;

        }

        private void exit() {
            K7Bot.getInstance().getShardManager().removeEventListener(listener);
            K7Bot.getInstance().getLoopedEventManager().removeEvent(this);
        }

        @Override
        public int checkforUpdates() {

            if (listener.hook.isExpired()) {
                exit();
            }

            return 0;
        }

        @Override
        public boolean isAvailable() {
            return true;
        }

        @Override
        public void shutdown() {
            exit();
        }

        @Override
        public boolean restart() {
            return false;
        }

        @Override
        public String getIdentifier() {
            return identifier;
        }

    }

}
