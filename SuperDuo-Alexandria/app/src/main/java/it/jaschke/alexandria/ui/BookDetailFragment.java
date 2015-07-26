package it.jaschke.alexandria.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import de.greenrobot.event.EventBus;
import it.jaschke.alexandria.R;
import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.events.BookDeletedEvent;
import it.jaschke.alexandria.services.BookService;

public class BookDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = BookDetailFragment.class.getSimpleName();
    private static final int LOADER_ID = 10;
    public static final String ARG_BOOK = "BookDetailFragment.ARG_BOOK";
    private static final String KEY_BOOK_TITLE = "BookDetailFragment.KEY_BOOK_TITLE";

    public static BookDetailFragment newInstance(String bookEan) {
        if(bookEan == null){
            throw new IllegalArgumentException("Null book EAN code provided.");
        }

        Bundle args = new Bundle();
        args.putString(ARG_BOOK, bookEan);
        BookDetailFragment fragment = new BookDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private TextView mBookTitleView;
    private TextView mBookSubtitleView;
    private TextView mBookDescriptionView;
    private TextView mBookAuthorsView;
    private TextView mBookCategoriesView;
    private ImageView mBookCoverView;

    private String mBookEanCode;
    private String mBookTitle;
    private ShareActionProvider shareActionProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mBookEanCode = getArguments().getString(ARG_BOOK);
        if(savedInstanceState != null){
            mBookTitle = savedInstanceState.getString(KEY_BOOK_TITLE);
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_full_book, container, false);
        mBookTitleView = (TextView) rootView.findViewById(R.id.fullBookTitle);
        mBookSubtitleView = (TextView) rootView.findViewById(R.id.fullBookSubTitle);
        mBookDescriptionView = (TextView) rootView.findViewById(R.id.fullBookDesc);
        mBookAuthorsView = (TextView) rootView.findViewById(R.id.authors);
        mBookCategoriesView = (TextView) rootView.findViewById(R.id.categories);
        mBookCoverView = (ImageView) rootView.findViewById(R.id.fullBookCover);
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_BOOK_TITLE, mBookTitle);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        mBookTitleView = null;
        mBookSubtitleView = null;
        mBookDescriptionView = null;
        mBookAuthorsView = null;
        mBookCategoriesView = null;

        Picasso.with(getActivity()).cancelRequest(mBookCoverView);
        mBookCoverView = null;

        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    public void onDestroyOptionsMenu() {
        shareActionProvider = null;
        super.onDestroyOptionsMenu();
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(BookDeletedEvent event){
        EventBus.getDefault().removeStickyEvent(event);
        if(mBookEanCode.equals(String.valueOf(event.getEanCode()))){
            getFragmentManager().popBackStackImmediate();
        }
    }

    private void deleteBook() {
        Intent bookIntent = new Intent(getActivity(), BookService.class);
        bookIntent.putExtra(BookService.EXTRA_EAN, mBookEanCode);
        bookIntent.setAction(BookService.ACTION_DELETE_BOOK);
        getActivity().startService(bookIntent);
        getActivity().getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            Bundle args = new Bundle();
            args.putString(ARG_BOOK, mBookEanCode);
            getLoaderManager().restartLoader(LOADER_ID, args, this);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.book_detail, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        configureShareIntent(mBookTitle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_delete){
            deleteBook();
            return true;
        }else{
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        long bookId = Long.parseLong(args.getString(ARG_BOOK));
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(bookId),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        mBookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        if(shareActionProvider != null){
            configureShareIntent(mBookTitle);
        }
        updateViews(data);
    }

    private void updateViews(Cursor data){
        mBookTitleView.setText(mBookTitle);

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        mBookSubtitleView.setText(bookSubTitle);

        String desc = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.DESC));
        mBookDescriptionView.setText(desc);

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        if(authors != null){
            String[] authorsArr = authors.split(",");
            mBookAuthorsView.setLines(authorsArr.length);
            mBookAuthorsView.setText(authors.replace(",", "\n"));
        }else {
            mBookAuthorsView.setText(null);
        }

        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        mBookCategoriesView.setText(categories);

        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if(!TextUtils.isEmpty(imgUrl) && Patterns.WEB_URL.matcher(imgUrl).matches()){
            mBookCoverView.setVisibility(View.VISIBLE);
            Picasso.with(getActivity())
                    .load(imgUrl)
                    .fit()
                    .centerInside()
                    .noPlaceholder()
                    .into(mBookCoverView);
        }else {
            mBookCoverView.setVisibility(View.INVISIBLE);
        }
    }

    private void configureShareIntent(String bookTitle) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            //noinspection deprecation
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        }else {
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        }

        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text_format, bookTitle));
        shareActionProvider.setShareIntent(shareIntent);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }
}