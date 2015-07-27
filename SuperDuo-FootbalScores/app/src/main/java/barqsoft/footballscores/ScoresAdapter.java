package barqsoft.footballscores;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;

public class ScoresAdapter extends CursorAdapter {

    public interface OnScoreItemClickListener {
        void onShareClick(String homeTeamName, String awayTeamName, String matchScore);
    }

    private WeakReference<OnScoreItemClickListener> mListenerRef;

    public ScoresAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.scores_list_item, parent, false);
        final ViewHolder viewHolder = new ViewHolder(item);
        viewHolder.mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String homeTeamName = viewHolder.mHomeTeamNameView.getText().toString();
                String awayTeamName = viewHolder.mAwayTeamNameView.getText().toString();
                String matchScore = viewHolder.mMatchScoreTextView.getText().toString();
                notifyScoreItemClicked(homeTeamName, awayTeamName, matchScore);
            }
        });
        item.setTag(viewHolder);
        return item;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        final ViewHolder mHolder = (ViewHolder) view.getTag();
        String homeTeamName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ScoresItem.HOME_NAME));
        mHolder.mHomeTeamNameView.setText(homeTeamName);
        mHolder.mHomeTeamNameView.setContentDescription(
                context.getString(R.string.content_description_home_team_format, homeTeamName));
        String awayTeamName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ScoresItem.AWAY_NAME));
        mHolder.mAwayTeamNameView.setText(awayTeamName);
        mHolder.mAwayTeamNameView.setContentDescription(
                context.getString(R.string.content_description_away_team_format, awayTeamName));

        String matchTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ScoresItem.MATCH_TIME));
        mHolder.mMatchDateTextView.setText(matchTime);

        int homeGoals = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.ScoresItem.HOME_GOALS));
        int awayGoals = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.ScoresItem.AWAY_GOALS));
        mHolder.mMatchScoreTextView.setText(Utilies.getScores(homeGoals, awayGoals));
        mHolder.mMatchScoreTextView.setContentDescription(
                context.getString(R.string.content_description_score_format, homeGoals, awayGoals));

        mHolder.mHomeTeamCrestView.setImageResource(Utilies.getTeamCrestByTeamName(homeTeamName));
        mHolder.mAwayTeamCrestView.setImageResource(Utilies.getTeamCrestByTeamName(awayTeamName));

        int leagureId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.ScoresItem.LEAGUE_ID));
        mHolder.mLeagueNameView.setText(Utilies.getLeague(leagureId));
    }

    @SuppressWarnings("unused")
    public void setOnScoresItemClickListener(OnScoreItemClickListener listener) {
        mListenerRef = listener != null ? new WeakReference<>(listener) : null;
    }

    private void notifyScoreItemClicked(String homeTeamName, String awayTeamName, String matchScore) {
        OnScoreItemClickListener listener = mListenerRef != null ? mListenerRef.get() : null;
        if (listener != null) {
            listener.onShareClick(homeTeamName, awayTeamName, matchScore);
        }
    }

    private static class ViewHolder {

        private TextView mLeagueNameView;
        private View mShareButton;
        private TextView mHomeTeamNameView;
        private TextView mAwayTeamNameView;
        private TextView mMatchScoreTextView;
        private TextView mMatchDateTextView;
        private ImageView mHomeTeamCrestView;
        private ImageView mAwayTeamCrestView;

        private ViewHolder(View view) {
            mHomeTeamNameView = (TextView) view.findViewById(R.id.home_name);
            mAwayTeamNameView = (TextView) view.findViewById(R.id.away_name);
            mMatchScoreTextView = (TextView) view.findViewById(R.id.scores);
            mMatchDateTextView = (TextView) view.findViewById(R.id.data_textview);
            mHomeTeamCrestView = (ImageView) view.findViewById(R.id.home_crest);
            mAwayTeamCrestView = (ImageView) view.findViewById(R.id.away_crest);
            mLeagueNameView = (TextView) view.findViewById(R.id.league);
            mShareButton = view.findViewById(R.id.share_button);
        }
    }
}
