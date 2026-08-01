/**
 *
 */
package de.klassenserver7b.k7bot.logging;

import de.klassenserver7b.k7bot.logging.listeners.*;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.channel.GenericChannelEvent;
import net.dv8tion.jda.api.events.emoji.GenericEmojiEvent;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.role.GenericRoleEvent;
import net.dv8tion.jda.api.events.thread.GenericThreadEvent;
import net.dv8tion.jda.api.events.user.GenericUserEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 */
public class LoggingFilter extends ListenerAdapter {

    private static LoggingFilter instance;

    private final LoggingBlocker loggingBlocker;

    private final List<EventListener> listeners = new LinkedList<>();

    private int queueEnableCount = 0;
    private final Queue<GenericEvent> eventQueue = new ConcurrentLinkedQueue<>();

    /**
     *
     */
    private LoggingFilter() {
        super();
        loggingBlocker = new LoggingBlocker();
        initDefault();
    }

    protected void initDefault() {

        listeners.add(new MemberLoggingListener());
        listeners.add(new ModerationLoggingListener());
        listeners.add(new RoleLoggingListener());
        listeners.add(new ChannelLoggingListener());
        listeners.add(new MessageLoggingListener());
        listeners.add(new VoiceLoggingListener());
        listeners.add(new InviteLoggingListener());
        listeners.add(new EmojiLoggingListener());
        listeners.add(new EventLoggingListener());

    }

    /**
     * Executes the event.
     * If the event execution is blocked, the event is added to the queue.
     * If not the event is executed directly.
     *
     * @param event the event to handle
     */
    @Override
    public void onGenericEvent(@NotNull GenericEvent event) {
        synchronized (eventQueue) {
            if (queueEnableCount > 0) {
                eventQueue.offer(event);
                return;
            }

            propagateEvent(event);
        }
    }

    /**
     * Executes the event that comes from the event queue or directly from the JDA.
     *
     * @param event the event to propagate
     */
    private void propagateEvent(@NotNull GenericEvent event) {

        switch (event) {
            case GenericGuildEvent channelEvent when checkAndUnblock(channelEvent.getGuild().getIdLong()) -> {
                return;
            }
            case GenericEmojiEvent channelEvent when checkAndUnblock(channelEvent.getEmoji().getIdLong()) -> {
                return;
            }
            case GenericRoleEvent channelEvent when checkAndUnblock(channelEvent.getRole().getIdLong()) -> {
                return;
            }
            case GenericMessageEvent channelEvent when checkAndUnblock(channelEvent.getMessageIdLong()) -> {
                return;
            }
            case GenericChannelEvent channelEvent when checkAndUnblock(channelEvent.getChannel().getIdLong()) -> {
                return;
            }
            case GenericThreadEvent channelEvent when checkAndUnblock(channelEvent.getThread().getIdLong()) -> {
                return;
            }
            case GenericUserEvent channelEvent when checkAndUnblock(channelEvent.getUser().getIdLong()) -> {
                return;
            }
            default -> {
            }
        }

        for (EventListener listener : listeners) {
            listener.onEvent(event);
        }
    }

    private boolean checkAndUnblock(long snowflake) {
        if (loggingBlocker.isBlocked(snowflake)) {
            loggingBlocker.unblock(snowflake);
            return true;
        }

        return false;
    }

    /**
     * Blocks the execution of events.
     *
     * @return an AutoCloseable object to unblock the event execution
     */
    public AutoCloseable blockEventExecution() {
        synchronized (eventQueue) {
            queueEnableCount++;
            return this::unblockEventExecution;
        }
    }

    public AutoCloseable blockEventExecution(long snowflake) {
        synchronized (eventQueue) {
            loggingBlocker.block(snowflake);
            queueEnableCount++;
            return this::unblockEventExecution;
        }
    }

    private void unblockEventExecution() {
        synchronized (eventQueue) {
            queueEnableCount--;
            if (queueEnableCount > 0) return;

            GenericEvent event;
            while ((event = eventQueue.poll()) != null) {
                propagateEvent(event);
            }
        }
    }

    public static LoggingFilter getInstance() {
        if (instance == null) {
            instance = new LoggingFilter();
        }
        return instance;
    }

    public LoggingBlocker getLoggingBlocker() {
        return loggingBlocker;
    }
}
