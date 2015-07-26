package it.jaschke.alexandria.ui;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import it.jaschke.alexandria.R;
import it.jaschke.alexandria.data.BookListAdapter;
import it.jaschke.alexandria.data.AlexandriaContract;


public class BookListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public interface OnBookSelectedListener {
        void onBookSelected(String bookEan);
    }

    public static final String TAG = BookListFragment.class.getSimpleName();
    private static final String KEY_LISTVIEW_STATE = "BookListFragment.KEY_LISTVIEW_STATE";
    private static final String ARG_QUERY_STRING = "BookListFragment.ARG_QUERY_STRING";
    private static final String ARG_QUERY_SELECTION = "BookListFragment.ARG_QUERY_SELECTION";
    private static final int LIST_ITEM_LOADER_ID = TAG.hashCode() + 1;

    public static BookListFragment newInstance() {
        return new BookListFragment();
    }

    private OnBookSelectedListener mSelectionListener;
    private BookListAdapter mBookListAdapter;
    private ListView mBookListView;
    private SearchView mSearchView;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnBookSelectedListener) {
            mSelectionListener = (OnBookSelectedListener) activity;
        }else if(getParentFragment() != null
                && getParentFragment() instanceof OnBookSelectedListener){
            mSelectionListener = (OnBookSelectedListener) getParentFragment();
        }
    }

    @Override
    public void onDetach() {
        mSelectionListener = null;
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list_of_books, container, false);
        initializeViewReferences(rootView);
        setEventListeners();
        configureListView();
        return rootView;
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState == null) {
            loadAllBookItems();
        } else {
            executeSearch(mSearchView.getQuery().toString());
        }
    }

    @Override
    public void onDestroyView() {
        mSearchView = null;
        mBookListView = null;
        mBookListAdapter.changeCursor(null);
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(TextUtils.isEmpty(mSearchView.getQuery())){
            loadAllBookItems();
        }else {
            executeSearch(mSearchView.getQuery().toString());
        }
    }

    private void initializeViewReferences(View rootView) {
        mBookListView = (ListView) rootView.findViewById(R.id.booksListView);
        mSearchView = (SearchView) rootView.findViewById(R.id.searchView);
    }

    private void setEventListeners() {
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                executeSearch(query);
                ViewUtils.hideSoftwareKeyboard(getActivity());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                executeSearch(newText);
                return true;
            }
        });
    }

    private void configureListView() {
        mBookListAdapter = new BookListAdapter(getActivity(), null, 0);
        mBookListView.setAdapter(mBookListAdapter);
        mBookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = mBookListAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    String bookEan = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry._ID));
                    notifyBookSelected(bookEan);
                    ViewUtils.hideSoftwareKeyboard(getActivity());
                }
            }
        });
    }

    private void notifyBookSelected(String bookEan) {
        if (mSelectionListener != null) {
            mSelectionListener.onBookSelected(bookEan);
        }
    }

    private void loadAllBookItems(){
        getLoaderManager().restartLoader(LIST_ITEM_LOADER_ID, null, this);
    }

    private void executeSearch(String query) {
        final String selection = AlexandriaContract.BookEntry.TITLE
                + " LIKE ? OR "
                + AlexandriaContract.BookEntry.SUBTITLE
                + " LIKE ? ";
        Bundle args = new Bundle();
        args.putString(ARG_QUERY_STRING, query);
        args.putString(ARG_QUERY_SELECTION, selection);
        getLoaderManager().restartLoader(LIST_ITEM_LOADER_ID, args, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String selection = null;
        String[] selectionArgs = null;
        if(args != null){
            String query = args.getString(ARG_QUERY_STRING);
            if(!TextUtils.isEmpty(query)){
                query = "%" + query + "%";
                selectionArgs = new String[]{query, query};

                selection = args.getString(ARG_QUERY_SELECTION);
                if(query != null && selection == null){
                    throw new IllegalArgumentException("No selection argument was provided, but there is a query string.");
                }
            }
        }

        return new CursorLoader(getActivity().getApplicationContext(),
                AlexandriaContract.BookEntry.CONTENT_URI,
                null,
                selection,
                selectionArgs,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mBookListAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mBookListAdapter.changeCursor(null);
    }
}
