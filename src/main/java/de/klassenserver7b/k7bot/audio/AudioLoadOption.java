package de.klassenserver7b.k7bot.audio;

/**
 * @author K7
 */
public enum AudioLoadOption {

    /**
     * Append this track to the queuelist
     */
    APPEND(0),

    /**
     * Play this track as the next track
     */
    NEXT(1),

    /**
     * replaceTrack the current track with this track
     */
    REPLACE(2),

    /**
     * replaceTrack the current track with this track and clear the queue
     */
    REPLACE_QUEUE(3);

    private final int id;

    AudioLoadOption(int id) {
        this.id = id;
    }

    /**
     * @return the id of the AudioLoadOption
     */
    public int getId() {
        return id;
    }

    /**
     * @return the AudioLoadOption by the id
     */
    public static AudioLoadOption byId(int id) {

        for (AudioLoadOption l : values()) {
            if (l.getId() == id) {
                return l;
            }
        }

        return APPEND;

    }
}
