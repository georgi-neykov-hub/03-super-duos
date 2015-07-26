package barqsoft.footballscores;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import barqsoft.footballscores.DatabaseContract.ScoresItem;

/**
 * Created by yehya khaled on 2/25/2015.
 */
public class ScoresDBHelper extends SQLiteOpenHelper
{
    public static final String DATABASE_NAME = "Scores.db";
    private static final int DATABASE_VERSION = 2;
    public ScoresDBHelper(Context context)
    {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        final String CreateScoresTable = "CREATE TABLE " + DatabaseContract.SCORES_TABLE + " ("
                + ScoresItem._ID + " INTEGER PRIMARY KEY,"
                + ScoresItem.DATE + " TEXT NOT NULL,"
                + ScoresItem.MATCH_TIME + " INTEGER NOT NULL,"
                + ScoresItem.HOME_NAME + " TEXT NOT NULL,"
                + ScoresItem.AWAY_NAME + " TEXT NOT NULL,"
                + ScoresItem.LEAGUE_ID + " INTEGER NOT NULL,"
                + ScoresItem.HOME_GOALS + " TEXT NOT NULL,"
                + ScoresItem.AWAY_GOALS + " TEXT NOT NULL,"
                + ScoresItem.MATCH_ID + " INTEGER NOT NULL,"
                + ScoresItem.MATCH_DAY_ID + " INTEGER NOT NULL,"
                + " UNIQUE ("+ ScoresItem.MATCH_ID +") ON CONFLICT REPLACE"
                + " );";
        db.execSQL(CreateScoresTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        //Remove old values when upgrading.
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.SCORES_TABLE);
    }
}
