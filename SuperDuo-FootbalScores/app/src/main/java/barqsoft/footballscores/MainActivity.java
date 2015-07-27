package barqsoft.footballscores;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import barqsoft.footballscores.service.ScoresFetchService;

public class MainActivity extends AppCompatActivity
{
    private static final String KEY_PAGER_STATE = "MainActivity.KEY_PAGER_STATE";

    public ViewPager mTabViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TabPageAdapter mPagerAdapter = new TabPageAdapter(this, getSupportFragmentManager());
        mTabViewPager = (ViewPager) findViewById(R.id.pager);
        mTabViewPager.setAdapter(mPagerAdapter);
        TabLayout mTabLayout = (TabLayout) findViewById(R.id.tabLayout);
        mTabLayout.setupWithViewPager(mTabViewPager);

        if(savedInstanceState == null){
            updateScores();
            mTabViewPager.setCurrentItem(2);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_PAGER_STATE, mTabViewPager.onSaveInstanceState());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mTabViewPager.onRestoreInstanceState(savedInstanceState.getParcelable(KEY_PAGER_STATE));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_about)
        {
            Intent start_about = new Intent(this,AboutActivity.class);
            startActivity(start_about);
            return true;
        }else if(id == R.id.action_refresh){
            updateScores();
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateScores() {
        Intent service_start = new Intent(this, ScoresFetchService.class);
        startService(service_start);
    }

    private static class TabPageAdapter extends FragmentStatePagerAdapter {
        public static final int NUM_PAGES = 5;

        private Date[] dates;
        private Context mContext;

        public TabPageAdapter(Context context, FragmentManager fm) {
            super(fm);
            mContext = context.getApplicationContext();
            long now = System.currentTimeMillis();
            dates = new Date[NUM_PAGES];
            for (int i = 0; i < NUM_PAGES; i++) {
                dates[i] = new Date(now + ((i - 2) * 86400000));
            }
        }

        @Override
        public Fragment getItem(int i) {
            return TabFragment.newInstance(dates[i]);
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return getDayName(mContext, dates[position]);
        }

        private String getDayName(Context context, Date date) {
            // If the mMatchDateTextView is today, return the localized version of "Today" instead of the actual
            // day name.
            if(DateUtils.isToday(date.getTime())){
                return context.getString(R.string.today);
            }else {
                Calendar calendar = Calendar.getInstance(Locale.US);
                calendar.setTime(date);
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
                return context.getResources().getStringArray(R.array.names_of_days)[dayOfWeek];
            }
        }
    }
}
