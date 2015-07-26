package it.jaschke.alexandria.ui;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.jaschke.alexandria.R;

public class BooksListContainerFragment extends Fragment implements BookListFragment.OnBookSelectedListener{

    public static final String TAG = BooksListContainerFragment.class.getSimpleName();

    public static BooksListContainerFragment newInstance(){
        return new BooksListContainerFragment();
    }

    private boolean mDualPane;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDualPane = getResources().getBoolean(R.bool.has_two_panes);
        if (savedInstanceState!= null){
            repositionChildrenIfNeeded();
        }
    }

    private void repositionChildrenIfNeeded() {
        FragmentManager fm = getChildFragmentManager();

        int targetContainerId = mDualPane ? R.id.detail_frame : R.id.master_frame;
        BookDetailFragment detailFragment = (BookDetailFragment) fm.findFragmentByTag(BookDetailFragment.TAG);
        if (detailFragment != null &&detailFragment.getId() != targetContainerId){
            Parcelable state = fm.saveFragmentInstanceState(detailFragment);
            fm.beginTransaction()
                    .remove(detailFragment)
                    .add(targetContainerId, detailFragment, BookDetailFragment.TAG)
                    .commit();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int layoutId = mDualPane? R.layout.layout_dual_pane_container : R.layout.layout_single_pane_container;
        return inflater.inflate(layoutId, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(savedInstanceState == null){
            showListFragment();
        }
    }

    private void showListFragment(){
        getChildFragmentManager().beginTransaction()
                .replace(R.id.master_frame, BookListFragment.newInstance(), BookListFragment.TAG)
                .commit();
        getChildFragmentManager().executePendingTransactions();
    }

    @Override
    public void onBookSelected(String bookEan) {
        BookDetailFragment fragment = BookDetailFragment.newInstance(bookEan);

        int id = mDualPane? R.id.detail_frame : R.id.master_frame;
        getChildFragmentManager().beginTransaction()
                .replace(id, fragment)
                .addToBackStack(BookDetailFragment.TAG)
                .commit();
    }


}
