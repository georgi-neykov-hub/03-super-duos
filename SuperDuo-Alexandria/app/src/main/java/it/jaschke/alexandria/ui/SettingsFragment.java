package it.jaschke.alexandria.ui;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import it.jaschke.alexandria.R;


public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName("AlexandriaPreferences");
        addPreferencesFromResource(R.xml.preferences);
    }
}
