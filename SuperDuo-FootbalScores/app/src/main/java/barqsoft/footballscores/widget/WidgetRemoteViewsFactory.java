package barqsoft.footballscores.widget;

import android.content.Context;
import android.database.Cursor;
import android.os.Binder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

public class WidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final String TAG = WidgetRemoteViewsFactory.class.getSimpleName();

    private Context mContext;
    private Cursor mDataCursor;

    public WidgetRemoteViewsFactory(Context context) {
        mContext = context;
        Log.d(TAG,"Creating new WidgetRemoteViewsFactory.");
    }

    @Override
    public void onCreate() {
        //queryScores();
    }

    private void queryScores() {
        closeDataCursor();

        // Revert back to our process' identity so we can work with our
        // content provider
        final long identityToken = Binder.clearCallingIdentity();

        Date currentDate = new Date(System.currentTimeMillis());
        SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String[] selection = {mDateFormat.format(currentDate)};
        mDataCursor = mContext.getContentResolver().query(
                DatabaseContract.ScoresItem.buildScoreWithDate(),
                null,
                null,
                selection,
                null);
        if(mDataCursor != null){
            //Ensure cursor is initialized.
            mDataCursor.getCount();
        }

        Binder.restoreCallingIdentity(identityToken);
    }

    private void closeDataCursor() {
        if (mDataCursor != null) {
            mDataCursor.close();
        }
    }

    @Override
    public void onDataSetChanged() {
        queryScores();
    }

    @Override
    public void onDestroy() {
        closeDataCursor();
    }

    @Override
    public int getCount() {
        return mDataCursor != null? mDataCursor.getCount() : 0;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        mDataCursor.moveToPosition(position);
        RemoteViews view = new RemoteViews(mContext.getPackageName(), R.layout.widget_list_item);
        String homeTeamName = mDataCursor.getString(mDataCursor.getColumnIndex(DatabaseContract.ScoresItem.HOME_NAME));
        view.setTextViewText(R.id.home_name, homeTeamName);

        String awayTeamName = mDataCursor.getString(mDataCursor.getColumnIndex(DatabaseContract.ScoresItem.AWAY_NAME));
        view.setTextViewText(R.id.away_name, awayTeamName);

        String matchDate = mDataCursor.getString(mDataCursor.getColumnIndex(DatabaseContract.ScoresItem.MATCH_TIME));
        view.setTextViewText(R.id.data_textview, matchDate);

        int homeGoals = mDataCursor.getInt(mDataCursor.getColumnIndex(DatabaseContract.ScoresItem.HOME_GOALS));
        int awayGoals = mDataCursor.getInt(mDataCursor.getColumnIndex(DatabaseContract.ScoresItem.AWAY_GOALS));
        view.setTextViewText(R.id.score_textview, Utilies.getScores(homeGoals, awayGoals));

        view.setImageViewResource(R.id.home_crest, Utilies.getTeamCrestByTeamName(homeTeamName));
        view.setImageViewResource(R.id.away_crest, Utilies.getTeamCrestByTeamName(awayTeamName));

        return view;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        int idIndex = mDataCursor.getColumnIndex(DatabaseContract.ScoresItem._ID);
        return mDataCursor.getLong(idIndex);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
