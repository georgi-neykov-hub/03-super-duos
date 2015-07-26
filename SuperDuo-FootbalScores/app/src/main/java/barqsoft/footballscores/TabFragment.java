package barqsoft.footballscores;

import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A placeholder fragment containing a simple view.
 */
public class TabFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, ScoresAdapter.OnScoreItemClickListener {

    private static final String KEY_LIST_VIEW_STATE = "TabFragment.KEY_LIST_VIEW_STATE";
    private static final String ARG_TARGET_DATE = "TabFragment.ARG_TARGET_DATE";
    private static final int SCORES_LOADER = 0x987;

    public static TabFragment newInstance(@NonNull Date targetDate) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_TARGET_DATE, targetDate);
        TabFragment fragment = new TabFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private Date mTargetDate;
    private ScoresAdapter mAdapter;
    private ListView mListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTargetDate = (Date) getArguments().getSerializable(ARG_TARGET_DATE);
        mAdapter = new ScoresAdapter(getActivity(), null, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_scores_tab, container, false);
        mListView = (ListView) rootView.findViewById(R.id.scores_list);
        mListView.setAdapter(mAdapter);
        if (savedInstanceState != null) {
            mListView.onRestoreInstanceState(savedInstanceState.getParcelable(KEY_LIST_VIEW_STATE));
        }
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getLoaderManager().initLoader(SCORES_LOADER, null, this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mListView = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.setOnScoresItemClickListener(this);
    }

    @Override
    public void onPause() {
        mAdapter.setOnScoresItemClickListener(null);
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_LIST_VIEW_STATE, mListView.onSaveInstanceState());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String[] where = {dateFormat.format(mTargetDate)};
        return new CursorLoader(getActivity(),
                DatabaseContract.ScoresItem.buildScoreWithDate(),
                null,
                null,
                where,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onShareClick(String homeTeamName, String awayTeamName, String matchScore) {
        Intent shareIntent = createShareIntent(homeTeamName, awayTeamName, matchScore);
        if (shareIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(shareIntent);
        }
    }

    @NonNull
    private Intent createShareIntent(String homeTeamName, String awayTeamName, String matchScore) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //noinspection deprecation
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        } else {
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        }
        shareIntent.setType("text/plain");

        //noinspection StringBufferReplaceableByString
        String shareText = new StringBuilder()
                .append(homeTeamName)
                .append(' ')
                .append(matchScore)
                .append(' ')
                .append(awayTeamName)
                .append(' ')
                .append(getString(R.string.share_hashtag))
                .toString();
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        return shareIntent;
    }
}
