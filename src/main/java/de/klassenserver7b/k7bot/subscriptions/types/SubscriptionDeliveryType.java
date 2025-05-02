package de.klassenserver7b.k7bot.subscriptions.types;

import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.jetbrains.annotations.NotNull;

/**
 * @author Klassenserver7b
 */
public enum SubscriptionDeliveryType {
    /**
     * A {@link GuildChannel GuildChannel}, Guild-Only.
     */
    TEXT_CHANNEL(1, true),

    /**
     * A {@link PrivateChannel PrivateChannel}
     */
    PRIVATE_CHANNEL(2, false),

    /**
     * A {@link NewsChannel NewsChannel}, Guild-Only.
     */
    NEWS(3, true),

    /**
     * A Type for subscriptions which are only used in DevMode
     */
    CANARY(5, false),

    /**
     * Unknown Subscription channel type. Should never happen and would only
     * possibly happen if Discord implemented a new channel type and JDA/K7Bot had
     * yet to implement support for it.
     */
    UNKNOWN(-1, false);

    private final int id;
    private final boolean isGuild;

    SubscriptionDeliveryType(int id, boolean isguild) {

        this.id = id;
        this.isGuild = isguild;

    }

    /**
     * The K7Bot id key used to represent the {@link SubscriptionDeliveryType}.
     *
     * @return The id key used by K7Bot for this channel type.
     */

    public int getId() {
        return id;
    }

    /**
     * Whether this ChannelType is present for a
     * {@link net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
     * GuildChannel}
     *
     * @return Whether this a {@link GuildChannel}.
     */
    public boolean isGuild() {
        return isGuild;
    }

    /**
     * Static accessor for retrieving a channel type based on its K7Bot id key.
     *
     * @param id The id key of the requested channel type.
     * @return The {@link SubscriptionDeliveryType} that is referred to by the
     * provided key. If the id key is unknown, {@link #UNKNOWN} is returned.
     */
    @NotNull
    public static SubscriptionDeliveryType fromId(int id) {
        for (SubscriptionDeliveryType type : values()) {
            if (type.id == id)
                return type;
        }
        return UNKNOWN;
    }
}