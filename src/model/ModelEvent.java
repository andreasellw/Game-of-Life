package model;

/**
 * @author Andreas Ellwanger, Christian Reiner, Lisa Stephan
 */
public class ModelEvent {
    private final EventTypes myEvent;

    /**
     * Constructor
     *
     * @param theEvent event type (enum)
     */
    public ModelEvent(final EventTypes theEvent) {
        myEvent = theEvent;
    }

    /**
     * Get EventType
     *
     * @return event
     */
    public EventTypes getType() {
        return myEvent;
    }
}
