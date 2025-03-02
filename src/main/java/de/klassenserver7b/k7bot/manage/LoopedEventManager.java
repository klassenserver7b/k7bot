/**
 *
 */
package de.klassenserver7b.k7bot.manage;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.util.InternalStatusCodes;
import de.klassenserver7b.k7bot.util.customapis.DBAutodelete;
import de.klassenserver7b.k7bot.util.customapis.GourmettaInteractions;
import de.klassenserver7b.k7bot.util.customapis.LernsaxInteractions;
import de.klassenserver7b.k7bot.util.customapis.Stundenplan24Vplan;
import de.klassenserver7b.k7bot.util.customapis.types.LoopedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Klassenserver7b
 */
public class LoopedEventManager {

    private final List<LoopedEvent> registeredEvents;
    private final List<LoopedEvent> activeEvents;
    private final List<LoopedEvent> erroredEvents;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     *
     */
    public LoopedEventManager() {
        registeredEvents = new ArrayList<>();
        activeEvents = new ArrayList<>();
        erroredEvents = new ArrayList<>();
    }

    /**
     *
     */
    public void checkForUpdates() {

        List<LoopedEvent> change = new ArrayList<>();

        for (LoopedEvent erroredEvent : erroredEvents) {

            if (erroredEvent.isAvailable()) {
                log.info("{} is available again", erroredEvent.getIdentifier());
                change.add(erroredEvent);
            }

        }

        erroredEvents.removeAll(change);
        activeEvents.addAll(change);
        change.clear();

        for (LoopedEvent activeEvent : activeEvents) {
            int status = activeEvent.checkforUpdates();

            if (status != InternalStatusCodes.SUCCESS) {
                log.warn("{} had an error - will be checked next time only", activeEvent.getIdentifier());
                change.add(activeEvent);
            }
        }

        activeEvents.removeAll(change);
        erroredEvents.addAll(change);
        change.clear();
    }

    /**
     * @param event the event to be removed
     */
    public void removeEvent(LoopedEvent event) {
        registeredEvents.remove(event);
    }

    /**
     * @param identifier the identifier of the event to be removed
     */
    public void enableEvent(String identifier) {

        LoopedEvent selectedevent = null;

        for (LoopedEvent event : registeredEvents) {

            if (event.getIdentifier().equalsIgnoreCase(identifier)) {
                selectedevent = event;
                break;
            }

        }

        if (selectedevent == null) {
            return;
        }

        if (!activeEvents.contains(selectedevent)) {
            activeEvents.add(selectedevent);
        }

    }

    /**
     * @param identifier the identifier of the event to be removed
     */
    public void disableEvent(String identifier) {
        LoopedEvent selectedevent = null;

        for (LoopedEvent event : registeredEvents) {

            if (event.getIdentifier().equalsIgnoreCase(identifier)) {
                selectedevent = event;
                break;
            }

        }

        if (selectedevent == null) {
            return;
        }

        activeEvents.remove(selectedevent);
    }

    /**
     * @param event  the event to be registered
     * @param enable if the event should be enabled
     */
    public void registerEvent(LoopedEvent event, boolean enable) {
        registeredEvents.add(event);

        if (enable) {
            activeEvents.add(event);
        }
    }

    /**
     * @param events the events to be registered
     * @param enable wether the api should be enabled
     */
    public void registerEvents(Collection<? extends LoopedEvent> events, boolean enable) {
        registeredEvents.addAll(events);

        if (enable) {
            activeEvents.addAll(events);
        }
    }

    /**
     * @param identifier the identifier of the event to be removed
     */
    public void removeEvent(String identifier) {
        List<LoopedEvent> change = new ArrayList<>();

        for (LoopedEvent event : registeredEvents) {

            if (event.getIdentifier().equalsIgnoreCase(identifier)) {
                change.add(event);
            }

        }

        registeredEvents.removeAll(change);
        activeEvents.removeAll(change);
    }

    /**
     * @param identifiers the identifiers of the events to be removed
     */
    public void removeEvents(@Nonnull String... identifiers) {
        removeEvents(Arrays.asList(identifiers));
    }

    public void removeEvents(@Nonnull Collection<? extends String> identifiers) {

        List<LoopedEvent> change = new ArrayList<>();

        for (LoopedEvent event : registeredEvents) {

            if (identifiers.contains(event.getIdentifier())) {
                change.add(event);
            }

        }

        registeredEvents.removeAll(change);
        activeEvents.removeAll(change);
    }

    /**
     * restarts only all ACTIVE {@link LoopedEvent LoopedEvents}
     */
    public void restart() {

        for (LoopedEvent activeEvent : activeEvents) {
            activeEvent.restart();
        }

    }

    /**
     * restarts every registered {@link LoopedEvent LoopedEvents}
     */
    public void restartAll() {

        for (LoopedEvent event : registeredEvents) {
            event.restart();
        }

    }

    /**
     *
     */
    public void shutdownLoopedEvents() {

        try {

            for (LoopedEvent event : registeredEvents) {

                event.shutdown();

            }

            registeredEvents.clear();

        } catch (Exception e) {
            log.warn("Forced APIs to shut down");
        }

    }

    /**
     *
     */
    public void initializeDefaultEvents() {

        log.info("Await API-ready");
        PropertiesManager propMgr = K7Bot.getInstance().getPropertiesManager();

        if (propMgr.isApiEnabled("lernsax")) {
            registerEvent(new LernsaxInteractions(), true);
            log.info("LernsaxAPI registered");
        }

        if (propMgr.isApiEnabled("vplan")) {
            registerEvent(new Stundenplan24Vplan("JG12"), true);
            log.info("VplanAPI registered");
        }

        if (propMgr.isApiEnabled("gourmetta")) {
            registerEvent(new GourmettaInteractions(), true);
            log.info("GourmettaAPI registered");
        }

        registerEvent(new DBAutodelete(), true);

        // registerAPI(new VVOInteractions());

        log.info("APIs are initialized");
    }

}
