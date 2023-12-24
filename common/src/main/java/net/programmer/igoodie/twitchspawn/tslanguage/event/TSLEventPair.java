package net.programmer.igoodie.twitchspawn.tslanguage.event;

public class TSLEventPair {

    private final String eventType;
    private final String eventAccount;

    public TSLEventPair(String eventType, String eventAccount) {
        this.eventType = eventType;
        this.eventAccount = eventAccount;
    }

    public String getEventType() {
        return eventType;
    }

    public String getEventAccount() {
        return eventAccount;
    }

    @Override
    public int hashCode() {
        return eventType.hashCode() ^ eventAccount.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TSLEventPair))
            return false;

        TSLEventPair other = (TSLEventPair) o;

        return this.eventType.equalsIgnoreCase(other.eventType)
                && this.eventAccount.equalsIgnoreCase(other.eventAccount);
    }

    @Override
    public String toString() {
        return String.format("(%s, %s)", eventType, eventAccount);
    }
}
