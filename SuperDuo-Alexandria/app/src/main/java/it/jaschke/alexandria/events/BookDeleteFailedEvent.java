package it.jaschke.alexandria.events;

public class BookDeleteFailedEvent {

    public enum Reason{
        NOT_FOUND, INVALID_ARGUMENTS, OTHER
    }

    private Reason reason;

    public BookDeleteFailedEvent(Reason reason) {
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
