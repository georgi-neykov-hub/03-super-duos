package it.jaschke.alexandria.events;

public class BookDeletedEvent {

    private long eanCode;

    public BookDeletedEvent(long eanCode) {
        this.eanCode = eanCode;
    }

    public long getEanCode() {
        return eanCode;
    }
}
