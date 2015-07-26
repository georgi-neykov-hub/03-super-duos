package it.jaschke.alexandria.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import java.util.Map;

import de.greenrobot.event.EventBus;
import it.jaschke.alexandria.AlexandriaApplication;
import it.jaschke.alexandria.api.pojo.BookQueryResponse;
import it.jaschke.alexandria.api.pojo.Item;
import it.jaschke.alexandria.api.pojo.VolumeInfo;
import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.data.Book;
import it.jaschke.alexandria.events.BookAddFailedEvent;
import it.jaschke.alexandria.events.BookAddedEvent;
import it.jaschke.alexandria.events.BookDeletedEvent;
import retrofit.RetrofitError;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class BookService extends IntentService {

    private static final String TAG = BookService.class.getSimpleName();

    public static final String ACTION_BOOK_QUERY = "BookService.ACTION_BOOK_QUERY";
    public static final String ACTION_ADD_BOOK = "BookService.ACTION_ADD_BOOK";
    public static final String ACTION_DELETE_BOOK = "BookService.ACTION_DELETE_BOOK";

    public static final String EXTRA_EAN = "BookService.EXTRA_EAN";
    public static final String EXTRA_RESULT_RECEIVER = "BookService.EXTRA_RESULT_RECEIVER";
    public static final String EXTRA_QUERY_STATUS = "BookService.EXTRA_QUERY_STATUS";
    public static final String EXTRA_DELETE_RESULT = "BookService.EXTRA_DELETE_RESULT";
    public static final String EXTRA_BOOK = "BookService.EXTRA_BOOK";

    public static final int STATUS_SUCCESS = 200;
    public static final int STATUS_CONNECTION_ERROR = 100;
    public static final int STATUS_UNKNOWN_ERROR = 500;
    public static final int STATUS_ILLEGAL_PARAMETERS = 401;
    public static final int STATUS_NOT_FOUND = 404;
    public static final int STATUS_ALREADY_ADDED = 300;

    public BookService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final ResultReceiver receiver = intent.getParcelableExtra(EXTRA_RESULT_RECEIVER);
            final String action = intent.getAction();
            if (ACTION_BOOK_QUERY.equals(action)) {
                final String ean = intent.getStringExtra(EXTRA_EAN);
                executeBookQuery(ean, receiver);
            } else if(ACTION_ADD_BOOK.equals(action)) {
                handleAddAction(intent);
            } else if (ACTION_DELETE_BOOK.equals(action)) {
                handleDeleteAction(intent, receiver);
            }
        }
    }

    private void handleDeleteAction(Intent intent, ResultReceiver receiver) {
        String ean;
        if(intent.hasExtra(EXTRA_BOOK)){
            Book bookToDelete = intent.getParcelableExtra(EXTRA_BOOK);
            ean = bookToDelete.getEan();
        }else if(intent.hasExtra(EXTRA_EAN)){
            ean = intent.getStringExtra(EXTRA_EAN);
        }else {
            throw new IllegalArgumentException("No Book provided or book EAN code provided.");
        }

        if(ean == null){
            throw new IllegalArgumentException("Null EAN code.");
        }

        long eanNumber = Long.parseLong(ean);
        int rowsDeleted = getContentResolver().delete(AlexandriaContract.BookEntry.buildBookUri(eanNumber), null, null);

        notifyResultReceiver(receiver, rowsDeleted>0? STATUS_SUCCESS : STATUS_NOT_FOUND);
        EventBus.getDefault().postSticky(new BookDeletedEvent(eanNumber));
    }

    private void handleAddAction(Intent intent) {
        Book bookToAdd = intent.getParcelableExtra(EXTRA_BOOK);
        if(bookToAdd == null){
            throw new IllegalArgumentException("No Book provided.");
        }else if(bookToAdd.getEan() == null){
            throw new IllegalArgumentException("The book has null EAN number.");
        }

        if(!bookIsInDatabase(bookToAdd.getEan())){
            addBookToDatabase(bookToAdd);
            EventBus.getDefault().postSticky(new BookAddedEvent(bookToAdd));
        }else {
            EventBus.getDefault().postSticky(new BookAddFailedEvent(BookAddFailedEvent.Reason.ALREADY_ADDED));
        }
    }


    /**
     * Handle action executeBookQuery in the provided background thread with the provided
     * parameters.
     */
    private void executeBookQuery(String ean, ResultReceiver receiver) {
        if (TextUtils.isEmpty(ean) || ean.length() != 13) {
            notifyResultReceiver(receiver, STATUS_ILLEGAL_PARAMETERS);
            return;
        }

        if(bookIsInDatabase(ean)){
            notifyResultReceiver(receiver, STATUS_ALREADY_ADDED);
            return;
        }

        BookQueryResponse response;
        try {
            Map<String, String> args = new ArrayMap<>(10);
            args.put("q", "isbn:" + ean);
            args.put("orderBy", "newest");
            args.put("projection", "full");
            args.put("maxResults", "1");
            response = AlexandriaApplication.getInstance().getGoogleBooksAPI().queryBooks(args);
        } catch (RetrofitError error) {
            handleRetrofitError(error, receiver);
            return;
        }

        if (response.getTotalItems() == 0) {
            notifyResultReceiver(receiver, STATUS_NOT_FOUND);
        } else {
            Item item = response.getItems().get(0);

            Book foundBook = createBookFromVolume(ean, item);
            Bundle args = new Bundle();
            args.putParcelable(EXTRA_BOOK, foundBook);
            notifyResultReceiver(receiver, STATUS_SUCCESS, args);
        }
    }

    private boolean bookIsInDatabase(String ean) {
        boolean alreadyAdded = false;
        Cursor bookEntry = null;
        try {
            bookEntry = getContentResolver().query(
                    AlexandriaContract.BookEntry.buildBookUri(Long.parseLong(ean)),
                    null, // leaving "columns" null just returns all the columns.
                    null, // cols for "where" clause
                    null, // values for "where" clause
                    null  // sort order
            );
            alreadyAdded = bookEntry.getCount()>0;
        } finally {
            if(bookEntry != null){
                bookEntry.close();
            }
        }
        return alreadyAdded;
    }

    private Book createBookFromVolume(String ean, Item bookItem){
        VolumeInfo info = bookItem.getVolumeInfo();
        return new Book(ean,
                info.getTitle(),
                info.getSubtitle(),
                info.getDescription(),
                info.getImageLinks().getThumbnail(),
                info.getAuthors(),
                info.getCategories());
    }

    private void addBookToDatabase(Book book) {
        writeBackBook(book.getEan(),
                book.getTitle(),
                book.getSubtitle(),
                book.getDescription(),
                book.getCoverImageUrl());

        if(book.getAuthors() != null) {
            writeBackAuthors(book.getEan(), book.getAuthors());
        }

        if(book.getCategories() != null){
            writeBackCategories(book.getEan(), book.getCategories());
        }
    }

    private void handleRetrofitError(RetrofitError error, ResultReceiver receiver) {
        int resultCode;
        switch (error.getKind()){
            case NETWORK:
            case HTTP:
            case CONVERSION:
                resultCode = STATUS_CONNECTION_ERROR;
            break;
            default:
                resultCode = STATUS_UNKNOWN_ERROR;
                break;
        }
        notifyResultReceiver(receiver, resultCode);
    }

    private void notifyResultReceiver(ResultReceiver receiver, int resultCode){
        this.notifyResultReceiver(receiver, resultCode, null);
    }

    private void notifyResultReceiver(ResultReceiver receiver, int resultCode, Bundle options){
        if(receiver != null){
            receiver.send(resultCode, options);
        }
    }

    private void writeBackBook(String ean, String title, String subtitle, String desc, String imgUrl) {
        ContentValues values= new ContentValues();
        values.put(AlexandriaContract.BookEntry._ID, ean);
        values.put(AlexandriaContract.BookEntry.TITLE, title);
        values.put(AlexandriaContract.BookEntry.IMAGE_URL, imgUrl);
        values.put(AlexandriaContract.BookEntry.SUBTITLE, subtitle);
        values.put(AlexandriaContract.BookEntry.DESC, desc);
        getContentResolver().insert(AlexandriaContract.BookEntry.CONTENT_URI,values);
    }

    private void writeBackAuthors(String ean, String[] authors){
        ContentValues values= new ContentValues();
        for (String author : authors) {
            values.put(AlexandriaContract.AuthorEntry._ID, ean);
            values.put(AlexandriaContract.AuthorEntry.AUTHOR, author);
            getContentResolver().insert(AlexandriaContract.AuthorEntry.CONTENT_URI, values);
            values = new ContentValues();
        }
    }

    private void writeBackCategories(String ean, String[] categories) {
        ContentValues values= new ContentValues();
        for (String category : categories) {
            values.put(AlexandriaContract.CategoryEntry._ID, ean);
            values.put(AlexandriaContract.CategoryEntry.CATEGORY, category);
            getContentResolver().insert(AlexandriaContract.CategoryEntry.CONTENT_URI, values);
            values = new ContentValues();
        }
    }
 }