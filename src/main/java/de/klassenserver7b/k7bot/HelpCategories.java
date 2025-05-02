/**
 *
 */
package de.klassenserver7b.k7bot;

import org.jetbrains.annotations.NotNull;

/**
 * @author Klassenserver7b
 */
public enum HelpCategories {

    /**
     * This is the default value for the category. It is used when the category is not known.
     */
    UNKNOWN(-1),

    /**
     * This category is used for the overview of all commands. It is used to give a general overview of all commands.
     */
    OVERVIEW(0),

    /**
     * This category is used for all generic commands. These are commands that do not fit into any other category.
     */
    GENERIC(1),

    /**
     * This category is used for all tools commands. These are commands that are used to help the user with various tasks.
     */
    TOOLS(2),

    /**
     * This category is used for all moderation commands. These are commands that are used to moderate the server.
     */
    MODERATION(3),

    /**
     * This category is used for all music commands. These are commands that are used to play music.
     */
    MUSIC(4);

    /**
     * The id of the category.
     */
    private final int id;

    /**
     * Constructor for the {@link HelpCategories} enum.
     *
     * @param id The id of the category.
     */
    HelpCategories(int id) {

        this.id = id;

    }

    /**
     * Used to retrieve the id of the category Can be used to spimplify the category
     * and to retrieve the corresponding {@link HelpCategories} by using
     * {@link #fromId(int)}
     *
     * @return The static id of the category
     */
    public int getId() {
        return this.id;
    }

    /**
     * Static accessor for retrieving a category based on its K7Bot id key.
     *
     * @param id The id key of the requested category.
     * @return The {@link HelpCategories} that is referred to by the provided key.
     * If the id key is unknown, {@link #UNKNOWN} is returned.
     */
    @NotNull
    public static HelpCategories fromId(int id) {
        for (HelpCategories type : values()) {
            if (type.id == id)
                return type;
        }
        return UNKNOWN;
    }
}
