package net.programmer.igoodie.twitchspawn.tslanguage.event;

public class TSLEventPair {

    private final String eventType;
    private final String eventFor;

    public TSLEventPair(String eventType, String eventFor) {
        this.eventType = eventType;
        this.eventFor = eventFor;
    }

    public String getEventType() {
        return eventType;
    }

    public String getEventFor() {
        return eventFor;
    }

    @Override
    public int hashCode() {
        return eventType.hashCode() ^ eventFor.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TSLEventPair))
            return false;

        TSLEventPair other = (TSLEventPair) o;

        return this.eventType.equals(other.eventType)
                && this.eventFor.equals(other.eventFor);
    }

    @Override
    public String toString() {
        return String.format("(%s, %s)", eventType, eventFor);
    }
}
