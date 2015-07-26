package it.jaschke.alexandria.events;

import it.jaschke.alexandria.data.Book;

public abstract class BaseBookEvent {

    private final Book book;

    public BaseBookEvent(Book book) {
        this.book = book;
    }

    public Book getBook() {
        return book;
    }
}
