package it.jaschke.alexandria.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import it.jaschke.alexandria.R;
import it.jaschke.alexandria.data.Book;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        AddBookFragment.AddBookListener{

    public static final String MESSAGE_EVENT = "MESSAGE_EVENT";

    public static final String MESSAGE_KEY = "MESSAGE_EXTRA";
    public static final String INITIAL_FRAGMENT_TAG = "initialFragment";

    private BroadcastReceiver messageReciever;

    private Toolbar mToolbar;
    private DrawerLayout mNavigationDrawer;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private Fragment mInitialFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupNavigationDrawer();
        setupToolbar();
        setInitialScreen();

        mActionBarDrawerToggle.syncState();

        messageReciever = new MessageReciever();
        IntentFilter filter = new IntentFilter(MESSAGE_EVENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReciever, filter);
    }

    private void setInitialScreen() {
        mInitialFragment = getSupportFragmentManager().findFragmentByTag(INITIAL_FRAGMENT_TAG);
        if(mInitialFragment == null){
            String initialScreen = PreferenceManager.getDefaultSharedPreferences(this)
                    .getString("pref_startFragment", "0");
            mInitialFragment = initialScreen.equals("0")? BooksListContainerFragment.newInstance() : AddBookFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, mInitialFragment, INITIAL_FRAGMENT_TAG)
                    .commit();
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void setupToolbar(){
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

    }

    private void setupNavigationDrawer(){
        mNavigationDrawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        NavigationView navView = (NavigationView) findViewById(R.id.navigationView);
        navView.setNavigationItemSelectedListener(this);

        mActionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                mNavigationDrawer,
                mToolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close){
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                syncState();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                syncState();
                ViewUtils.hideSoftwareKeyboard(MainActivity.this);
            }
        };
        mNavigationDrawer.setDrawerListener(mActionBarDrawerToggle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mActionBarDrawerToggle.onOptionsItemSelected(item)
                || super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mActionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReciever);
        super.onDestroy();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.navigation_book_list:
                showBookListFragment();
                break;
            case R.id.navigation_enter_isbn:
                showAddBookFragment();
                break;
            case R.id.navigation_about:
                showAboutFragment();
                break;
            case R.id.navigation_settings:
                showSettingsActivity();
                break;
            default:
                return false;
        }

        menuItem.setChecked(true);
        mNavigationDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        Fragment listContainerFragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if(listContainerFragment != null
                && listContainerFragment.isResumed() &&
                listContainerFragment.getChildFragmentManager().getBackStackEntryCount()>0){
            listContainerFragment.getChildFragmentManager().popBackStack();
        }else {
            super.onBackPressed();
        }
    }

    private void showSettingsActivity() {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void showAboutFragment() {
        clearBackStack();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new AboutFragment(), AboutFragment.TAG)
                .addToBackStack(AboutFragment.TAG)
                .commit();
    }

    private void clearBackStack(){
        FragmentManager manager = getSupportFragmentManager();
        while (manager.getBackStackEntryCount()>0){
            manager.popBackStackImmediate();
        }
    }

    private void showAddBookFragment() {
        clearBackStack();
        if(!(mInitialFragment instanceof AddBookFragment)){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, AddBookFragment.newInstance(), AddBookFragment.TAG)
                    .addToBackStack(AddBookFragment.TAG)
                    .commit();
        }
    }

    private void showBookListFragment() {
        clearBackStack();
        if(!(mInitialFragment instanceof BooksListContainerFragment)){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, BooksListContainerFragment.newInstance(), BooksListContainerFragment.TAG)
                    .addToBackStack(BooksListContainerFragment.TAG)
                    .commit();
        }
    }

    @Override
    public void onBookAdded(Book book) {
        showBookListFragment();
    }

    private class MessageReciever extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getStringExtra(MESSAGE_KEY)!=null){
                Toast.makeText(MainActivity.this, intent.getStringExtra(MESSAGE_KEY), Toast.LENGTH_LONG).show();
            }
        }
    }
}