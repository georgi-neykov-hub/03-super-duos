package it.jaschke.alexandria.events;

public class BookAddFailedEvent {
    public enum Reason{
        ALREADY_ADDED, INVALID_ARGUMENTS, OTHER
    }

    private Reason reason;

    public BookAddFailedEvent(Reason reason) {
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
