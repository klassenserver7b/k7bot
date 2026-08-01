package de.klassenserver7b.k7bot.util;

import org.jetbrains.annotations.NotNull;

public enum BotState {

    STARTING(1),
    RUNNING(2),
    FAILING(-1),
    STOPPING(3);

    private final int id;

    BotState(int id) {
        this.id = id;
    }

    /**
     * Static accessor for retrieving a state based on its K7Bot id key.
     *
     * @param id The id key of the requested category.
     * @return The {@link BotState} that is referred to by the provided key.
     * If the id key is unknown, {@link #FAILING} is returned.
     */
    @NotNull
    public static BotState fromId(int id) {
        for (BotState type : values()) {
            if (type.id == id)
                return type;
        }
        return FAILING;
    }

    /**
     * Used to retrieve the id of the state. Can be used to simplify the state
     * and to retrieve the corresponding {@link BotState} by using
     * {@link #fromId(int)}
     *
     * @return The static id of the category
     */
    public int getId() {
        return this.id;
    }

}
