package it.jaschke.alexandria.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.List;

import de.greenrobot.event.EventBus;
import it.jaschke.alexandria.R;
import it.jaschke.alexandria.data.Book;
import it.jaschke.alexandria.data.BookSearchAdapter;
import it.jaschke.alexandria.data.BookSearchLoader;
import it.jaschke.alexandria.data.LoaderResult;
import it.jaschke.alexandria.events.BookAddFailedEvent;
import it.jaschke.alexandria.events.BookAddedEvent;
import it.jaschke.alexandria.services.BookService;

import static android.support.v7.widget.LinearLayoutManager.VERTICAL;


public class AddBookFragment extends Fragment implements LoaderManager.LoaderCallbacks<LoaderResult<List<Book>>>,
        BookSearchAdapter.OnAddItemClickListener{

    public static final String TAG = AddBookFragment.class.getSimpleName();
    private static final String ARG_BOOK_ISBN = "AddBookFragment.ISBN Argument";
    private static final String KEY_ADAPTER_STATE = "AddBookFragment.KEY_ADAPTER_STATE";
    private static final String KEY_RECYCLER_VIEW_STATE = "AddBookFragment.KEY_RECYCLER_VIEW_STATE";
    private static final String KEY_EAN_QUERY = "AddBookFragment.KEY_EAN_QUERY";
    private static final int LOADER_ID = "AddBookFragment.SEARCH_LOADER".hashCode();
    private static final int REQUEST_CODE_SCAN = 0x234;

    public static AddBookFragment newInstance() {
        return new AddBookFragment();
    }

    public interface AddBookListener{
        void onBookAdded(Book book);
    }

    private SearchView mSearchView;
    private Button mScanButton;
    private RecyclerView mSearchResultsView;
    private RecyclerView.LayoutManager mLayoutManager;
    private BookSearchAdapter mBookAdapter;
    private View mEmptySearchTextView;
    private View mProgressView;

    private AddBookListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof AddBookListener){
            mListener = (AddBookListener) activity;
        }
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBookAdapter = new BookSearchAdapter();
        if(savedInstanceState != null){
            mBookAdapter.onRestoreInstanceState(savedInstanceState.getParcelable(KEY_ADAPTER_STATE));
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_book, container, false);
        initializeViewReferences(rootView);
        configureRecyclerView(savedInstanceState);

        mScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScanActivity();
            }
        });

        mSearchView.setSubmitButtonEnabled(true);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                executeSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(savedInstanceState == null && getArguments()!= null){
            String initalIsbn = getArguments().getString(ARG_BOOK_ISBN);
            if(initalIsbn != null){
                mSearchView.setQuery(initalIsbn, true);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_ADAPTER_STATE, mBookAdapter.onSaveInstanceState());
        if(mLayoutManager != null){
            outState.putParcelable(KEY_RECYCLER_VIEW_STATE, mLayoutManager.onSaveInstanceState());
        }
    }

    @Override
    public void onDestroyView() {
        mSearchView = null;
        mScanButton = null;
        mSearchResultsView = null;
        mEmptySearchTextView = null;
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        mBookAdapter.setAddItemClickListener(this);
        EventBus.getDefault().registerSticky(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        mBookAdapter.setAddItemClickListener(null);
        ViewUtils.hideSoftwareKeyboard(getActivity());
    }

    @Override
    public Loader<LoaderResult<List<Book>>> onCreateLoader(int id, Bundle args) {
        String eanQuery = args.getString(KEY_EAN_QUERY);
        return new BookSearchLoader(getActivity().getApplicationContext(), eanQuery);
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<List<Book>>> loader, LoaderResult<List<Book>> data) {
        toggleLoadingView(false);
        if(data.isLoadSuccessful()){
            List<Book> results = data.getData();
            if (!results.isEmpty()){
                mBookAdapter.addItems(results);
            }else{
                toggleEmptyResultsView(true);
            }
        }else {
            showSearchMessage(R.string.book_search_error);
        }
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<List<Book>>> loader) {
        mBookAdapter.clearItems();
    }

    @Override
    public void onAddClick(int position, Book book) {
        Intent addIntent = new Intent(getActivity(), BookService.class)
                .setAction(BookService.ACTION_ADD_BOOK)
                .putExtra(BookService.EXTRA_BOOK, book);
        getActivity().startService(addIntent);
        toggleLoadingView(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(REQUEST_CODE_SCAN == requestCode && resultCode == Activity.RESULT_OK){
            String bookCode = data.getStringExtra(ScanBookActivity.EXTRA_ISBN_CODE);
            mSearchView.setQuery(bookCode, true);
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(BookAddedEvent event){
        EventBus.getDefault().removeStickyEvent(event);
        toggleLoadingView(false);
        showSearchMessage(R.string.message_book_added);
        if(mListener != null){
            mListener.onBookAdded(event.getBook());
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(BookAddFailedEvent event){
        EventBus.getDefault().removeStickyEvent(event);
        toggleLoadingView(false);

        int messageId;
        switch (event.getReason()){

            case ALREADY_ADDED:
                messageId = R.string.message_book_add_failed_existing;
                break;
            default:
                messageId = R.string.message_book_add_failed;
                break;
        }
        showSearchMessage(messageId);
    }

    private void initializeViewReferences(View rootView){
        mSearchView = (SearchView) rootView.findViewById(R.id.eanSearch);
        mScanButton = (Button) rootView.findViewById(R.id.scanButton);
        mSearchResultsView = (RecyclerView) rootView.findViewById(R.id.searchResults);
        mEmptySearchTextView = rootView.findViewById(R.id.emptyResultsLabel);
        mProgressView = rootView.findViewById(R.id.progressIndicator);
    }

    private void configureRecyclerView(Bundle savedInstanceState){
        mLayoutManager = new LinearLayoutManager(getActivity(), VERTICAL, false);
        if (savedInstanceState != null) {
            mLayoutManager.onRestoreInstanceState(savedInstanceState.getParcelable(KEY_RECYCLER_VIEW_STATE));
        }

        mSearchResultsView.setLayoutManager(mLayoutManager);
        mSearchResultsView.setAdapter(mBookAdapter);
        mSearchResultsView.setItemAnimator(new DefaultItemAnimator());
        RecyclerView.ItemDecoration decoration = new SpaceItemDecoration(
                getResources(),
                R.dimen.rhythm_space_single,
                R.dimen.rhythm_space_single,
                R.dimen.rhythm_space_single,
                SpaceItemDecoration.VERTICAL);
        mSearchResultsView.addItemDecoration(decoration);
    }

    private void startScanActivity() {
        Intent scanIntent = new Intent(getActivity(), ScanBookActivity.class);
        startActivityForResult(scanIntent, REQUEST_CODE_SCAN);
    }

    private void toggleEmptyResultsView(boolean show){
        mEmptySearchTextView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    private void executeSearch(String eanQuery){
        ViewUtils.hideSoftwareKeyboard(getActivity());

        if(eanQuery.length()==10 && !eanQuery.startsWith("978")) {
            eanQuery = "978" + eanQuery;
        }

        if(!TextUtils.isDigitsOnly(eanQuery) || eanQuery.length() != 13 ){
            showSearchMessage(R.string.message_invalid_isbn);
            return;
        }

        mBookAdapter.clearItems();
        toggleEmptyResultsView(false);
        Bundle args = new Bundle();
        args.putString(KEY_EAN_QUERY, eanQuery);
        getLoaderManager().restartLoader(LOADER_ID, args, this).forceLoad();
        toggleLoadingView(true);
    }

    private void toggleLoadingView(boolean show){
        int visibility = show? View.VISIBLE : View.INVISIBLE;
        mProgressView.setVisibility(visibility);
    }

    private void showSearchMessage(@StringRes int messageResId){
        showSearchMessage(messageResId, 0, null);
    }

    private void showSearchMessage(@StringRes int messageResId, @StringRes int actionLabelResid, View.OnClickListener actionListener){
        Snackbar instance = Snackbar.make(getView(), messageResId, Snackbar.LENGTH_SHORT);
        if(actionLabelResid != 0 && actionListener != null){
            instance.setAction(actionLabelResid,actionListener);
            int colorRes = ViewUtils.getThemeColorAccent(getActivity().getTheme());
            instance.setActionTextColor(colorRes);
        }
        instance.show();
    }
}
