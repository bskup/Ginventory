package com.bskup.ginbox;

import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class SettingsActivity extends AppCompatActivity {
    // Tag for log messages
    public static String LOG_TAG = SettingsActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Theme change based on preference
        String themeName = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("theme", "default");
        Log.v(LOG_TAG, "value for theme String in SettingsActivity oncreate: " + themeName);
        if (themeName.equals("AppThemeLight")) {
            setTheme(R.style.AppThemeLight);
        } else if (themeName.equals("AppThemeDark")) {
            setTheme(R.style.AppThemeDark);
        }
        setContentView(R.layout.activity_settings);
    }

    // Preference fragment
    public static class GinventoryPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {
        // Boolean whether preferences have changed or not
        public static Boolean mPreferencesChanged = false;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Log.v(LOG_TAG, "mPreferencesChanged in oncreate: " + mPreferencesChanged);
            addPreferencesFromResource(R.xml.settings_main);
            // Find the shared preferences and set listener on them
            // Listen for change of the theme preference
            SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
            sharedPreferences.registerOnSharedPreferenceChangeListener(this);
            Preference warningThreshold = findPreference(getString(R.string.settings_warning_threshold_key));
            bindPreferenceSummaryToValue(warningThreshold);
            Preference theme = findPreference(getString(R.string.settings_theme_key));
            bindPreferenceSummaryToValue(theme);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("theme")) {
                // If theme preference changed, do this
                // Recreate the activity
                getActivity().recreate();
            }
            mPreferencesChanged = true;
            Log.v(LOG_TAG, "onSharedPreferenceChanged called");
            Log.v(LOG_TAG, "mPreferencesChanged: " + mPreferencesChanged);
        }

        @Override
        public void onStop() {
            super.onStop();
            // Find the shared preferences and set listener on them
            // Listen for change of the theme preference
            SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int prefIndex = listPreference.findIndexOfValue(stringValue);
                if (prefIndex >= 0) {
                    CharSequence[] labels = listPreference.getEntries();
                    preference.setSummary(labels[prefIndex]);
                }
            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }

        // Set the current NewsItemPreferenceFragment instance as the listener on each preference
        // also read the current value of the preference stored in the SharedPreferences on the device,
        // and display that in the preference summary
        private void bindPreferenceSummaryToValue(Preference preference) {
            preference.setOnPreferenceChangeListener(this);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
            String preferenceString = preferences.getString(preference.getKey(), "");
            onPreferenceChange(preference, preferenceString);
        }
    }
}
