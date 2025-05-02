/**
 *
 */
package de.klassenserver7b.k7bot.util;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.FluentRestAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author K7
 */
public class GenericMessageSendHandler {

    private final InteractionHook hook;
    private final GuildMessageChannel channel;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final int HookId = 1;
    private static final int ChannelId = 2;
    private final int selectedid;

    /**
     * @param hook InteractionHook
     */
    public GenericMessageSendHandler(@NotNull InteractionHook hook) {
        Objects.requireNonNull(hook, "@NotNull required parameter is null: GuildMessageChannel");
        this.hook = hook;
        this.channel = null;
        selectedid = HookId;
    }

    /**
     * @param channel GuildMessageChannel
     */
    public GenericMessageSendHandler(@NotNull GuildMessageChannel channel) {
        Objects.requireNonNull(channel, "@NotNull required parameter is null: GuildMessageChannel");
        this.channel = channel;
        this.hook = null;
        selectedid = ChannelId;
    }

    public FluentRestAction<Message, ?> sendMessage(@NotNull CharSequence data) {
        try (MessageCreateData messdata = new MessageCreateBuilder().addContent(data.toString()).build()) {
            return sendMessage(messdata);
        }
    }

    public FluentRestAction<Message, ?> sendMessage(@NotNull MessageCreateData data) {
        try {
            switch (selectedid) {
                case HookId -> {
                    assert hook != null;
                    return hook.sendMessage(data);
                }
                case ChannelId -> {
                    assert channel != null;
                    return channel.sendMessage(data);
                }
            }
        } catch (NullPointerException e) {
            onNPE(e);
        }
        return null;
    }

    public FluentRestAction<Message, ?> sendMessageEmbeds(@NotNull MessageEmbed embed) {
        List<MessageEmbed> embedlist = new ArrayList<>();
        embedlist.add(embed);
        return sendMessageEmbeds(embedlist);
    }

    public FluentRestAction<Message, ?> sendMessageEmbeds(@NotNull MessageEmbed... embeds) {
        List<MessageEmbed> embedlist = Arrays.asList(embeds);
        return sendMessageEmbeds(embedlist);
    }

    public FluentRestAction<Message, ?> sendMessageEmbeds(@NotNull Collection<? extends MessageEmbed> embeds) {
        try {
            switch (selectedid) {
                case HookId -> {
                    assert hook != null;
                    return hook.sendMessageEmbeds(embeds);
                }
                case ChannelId -> {
                    assert channel != null;
                    return channel.sendMessageEmbeds(embeds);
                }
            }
        } catch (NullPointerException e) {
            onNPE(e);
        }
        return null;
    }

    public FluentRestAction<Message, ?> sendFiles(@NotNull FileUpload file, @NotNull FileUpload... files) {
        List<FileUpload> list = Arrays.asList(files);
        list.addFirst(file);
        return sendFiles(list);
    }

    public FluentRestAction<Message, ?> sendFiles(@NotNull Collection<? extends FileUpload> files) {
        try {
            switch (selectedid) {
                case HookId -> {
                    assert hook != null;
                    return hook.sendFiles(files);
                }
                case ChannelId -> {
                    assert channel != null;
                    return channel.sendFiles(files);
                }
            }
        } catch (NullPointerException e) {
            onNPE(e);
        }
        return null;
    }

    public FluentRestAction<Message, ?> sendMessageFormat(@NotNull String format, @NotNull Object... objects) {
        try {
            switch (selectedid) {
                case HookId -> {
                    assert hook != null;
                    return hook.sendMessageFormat(format, objects);
                }
                case ChannelId -> {
                    assert channel != null;
                    return channel.sendMessageFormat(format, objects);
                }
            }
        } catch (NullPointerException e) {
            onNPE(e);
        }
        return null;
    }

    public void sendTyping() {

        if (selectedid == ChannelId) {
            assert channel != null;
            channel.sendTyping().queue();
        }
    }

    public Guild getGuild() {

        switch (selectedid) {
            case HookId -> {
                assert hook != null;
                return hook.getInteraction().getGuild();
            }
            case ChannelId -> {
                assert channel != null;
                return channel.getGuild();
            }

            default -> {
                return null;
            }
        }
    }

    public void onNPE(NullPointerException e) {
        log.error(e.getMessage(), e);
    }

    public Class<?> getSelectedClass() {
        switch (selectedid) {
            case HookId -> {
                assert hook != null;
                return hook.getClass();
            }
            case ChannelId -> {
                assert channel != null;
                return channel.getClass();
            }
            default -> {
                return null;
            }
        }
    }

}
