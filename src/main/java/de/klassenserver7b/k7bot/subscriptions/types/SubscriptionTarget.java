package de.klassenserver7b.k7bot.subscriptions.types;

import de.klassenserver7b.k7bot.util.customapis.LernsaxInteractions;
import de.klassenserver7b.k7bot.util.customapis.Stundenplan24Vplan;
import org.jetbrains.annotations.NotNull;

/**
 * @author Klassenserver7b
 */
public enum SubscriptionTarget {

    /**
     * The Type for {@link Stundenplan24Vplan} Subscription, only when Vplan API
     * enabled.
     */
    VPLAN(1, true, true),

    /**
     * The Type for BotNews such as Updates and Fixes, only fully functional when
     * GitHub API enabled.
     */
    BOT_NEWS(2, false, false),

    /**
     * The Type for {@link LernsaxInteractions} especially 'Lernpläne', only when
     * Lernsax API enabled
     */
    LERNPLAN(3, true, true),

    /**
     * The Type for updates about the gourmetta foodplan of the day
     */
    GOURMETTA(4, true, false),

    /**
     * The Type for News about Hypixel Syblock
     */
    SKYBLOCKNEWS(5, true, false),

    /**
     * The Type for the dvb departure plan
     */
    DVB(6, true, false),

    /**
     * Unknown {@link SubscriptionTarget} type. Should never happen and would only
     * possibly happen if the K7Bot implemented a new API type and had yet to
     * implement channnel-support for it.
     */
    UNKNOWN(-1, false, false);

    private final boolean needsAPI;
    private final boolean privileged;
    private final int id;

    SubscriptionTarget(int id, boolean needsSpecialAPI, boolean isprivleged) {

        this.id = id;
        this.needsAPI = needsSpecialAPI;
        this.privileged = isprivleged;

    }

    /**
     * The K7Bot id key used to represent the {@link SubscriptionTarget}.
     *
     * @return The id key used by K7Bot for this target type.
     */
    public int getId() {
        return this.id;
    }

    /**
     * Whether this {@link SubscriptionTarget} needs an enabled API to run
     *
     * @return Whetherthis needs an enabled API to run.
     */
    public boolean needsApi() {
        return this.needsAPI;
    }

    /**
     * Whether this {@link SubscriptionTarget} needs special rights which are given
     * by the bot owner - e.g. access to Vplans
     *
     * @return Whether this needs special rights from the Admin
     */
    public boolean isprivileged() {
        return this.privileged;
    }

    /**
     * Static accessor for retrieving a {@link SubscriptionTarget} based on its
     * K7Bot id key.
     *
     * @param id The id key of the requested target type.
     * @return The {@link SubscriptionTarget} that is referred to by the provided
     * key. If the id key is unknown, {@link #UNKNOWN} is returned.
     */
    @NotNull
    public static SubscriptionTarget fromId(int id) {
        for (SubscriptionTarget type : values()) {
            if (type.id == id)
                return type;
        }
        return UNKNOWN;
    }

}
