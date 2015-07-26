package it.jaschke.alexandria.events;

import it.jaschke.alexandria.data.Book;

public class BookAddedEvent extends BaseBookEvent{

    public BookAddedEvent(Book book) {
        super(book);
    }
}