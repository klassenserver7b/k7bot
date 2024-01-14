package de.klassenserver7b.k7bot.logging;

import java.util.ArrayList;
import java.util.List;

public class LoggingBlocker {

    private static LoggingBlocker instance;

    private final List<Long> blockedIds;

    public LoggingBlocker() {
        this.blockedIds = new ArrayList<>();
    }

    public boolean isBlocked(long id) {
        return blockedIds.contains(id);
    }

    public void block(long id) {
        blockedIds.add(id);
    }

    public void unblock(long id) {
        blockedIds.remove(id);
    }

    public static LoggingBlocker getInstance() {
        if (instance == null) {
            instance = new LoggingBlocker();
        }
        return instance;
    }

}
