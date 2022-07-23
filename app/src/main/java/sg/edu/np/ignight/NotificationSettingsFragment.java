package sg.edu.np.ignight;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

public class NotificationSettingsFragment extends PreferenceFragmentCompat {

    private final int PICK_RINGTONE_REQUEST_CODE = 1;
    private SharedPreferences sharedPreferences;
    private String currentRingtone;
    private SwitchPreference messageNotificationEnabled, highPriorityNotifications;
    private Preference ringtonePreference;
    private ListPreference vibrationPreference;
    Context context;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        context = getActivity().getApplicationContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);  // get sharedPreferences

        // get preferences
        messageNotificationEnabled = findPreference(SettingsActivity.KEY_CHAT_NOTIFICATION_ENABLED);
        ringtonePreference = findPreference(SettingsActivity.KEY_CHAT_NOTIFICATION_RINGTONE);
        vibrationPreference = findPreference(SettingsActivity.KEY_CHAT_NOTIFICATION_VIBRATION);
        highPriorityNotifications = findPreference(SettingsActivity.KEY_CHAT_NOTIFICATION_PRIORITY);

        // initialize fields (show ringtone/vibration picker if notifications are enabled)
        boolean notificationEnabled = sharedPreferences.getBoolean(SettingsActivity.KEY_CHAT_NOTIFICATION_ENABLED, true);
        showPreferences(notificationEnabled);

        // hide/show ringtone/vibration picker when push notification value is changed
        messageNotificationEnabled.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                showPreferences((boolean) newValue);

                return true;
            }
        });

        ringtonePreference.setDefaultValue(Settings.System.DEFAULT_NOTIFICATION_URI.toString());  // set default value to default ringtone

        // get existing ringtone and initialize ringtone picker summary
        currentRingtone = sharedPreferences.getString(SettingsActivity.KEY_CHAT_NOTIFICATION_RINGTONE, null);
        setRingtoneSummary(currentRingtone);

        // initialize summary
        String vibration = sharedPreferences.getString(SettingsActivity.KEY_CHAT_NOTIFICATION_VIBRATION, getResources().getStringArray(R.array.vibration_preferences_values)[2]);
        vibrationPreference.setSummary(getResources().getStringArray(R.array.vibration_preferences)[vibrationPreference.findIndexOfValue(vibration)]);

        // update summary of vibration picker when value changes
        vibrationPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                vibrationPreference.setSummary(getResources().getStringArray(R.array.vibration_preferences)[vibrationPreference.findIndexOfValue((String) newValue)]);

                return true;
            }
        });
    }

    private void showPreferences(boolean show) {
        ringtonePreference.setVisible(show);
        vibrationPreference.setVisible(show);
        highPriorityNotifications.setVisible(show);
    }

    // ringtone picker reference: https://issuetracker.google.com/issues/37057453?pli=1#comment3
    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == ringtonePreference) {

            // get current ringtone
            currentRingtone = sharedPreferences.getString(SettingsActivity.KEY_CHAT_NOTIFICATION_RINGTONE, null);

            // initialize ringtone picker
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, Settings.System.DEFAULT_NOTIFICATION_URI);

            if (currentRingtone != null) {
                if (currentRingtone.isEmpty()) {
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
                } else {
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(currentRingtone));
                }
            } else {
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
            }

            startActivityForResult(intent, PICK_RINGTONE_REQUEST_CODE);
            return true;
        }
        else {
            return super.onPreferenceTreeClick(preference);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_RINGTONE_REQUEST_CODE  && resultCode == Activity.RESULT_OK && data != null) {

            // update ringtone with received values
            Uri ringtone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

            if (ringtone != null) {
                sharedPreferences.edit().putString(SettingsActivity.KEY_CHAT_NOTIFICATION_RINGTONE, ringtone.toString()).apply();
            }
            else {
                sharedPreferences.edit().putString(SettingsActivity.KEY_CHAT_NOTIFICATION_RINGTONE, null).apply();
            }

            setRingtoneSummary((ringtone == null)?null:ringtone.toString());

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // update the summary of the ringtone picker with the name of the ringtone
    private void setRingtoneSummary(String uri) {
        if (uri != null) {
            if (uri.isEmpty()) {
                ringtonePreference.setSummary("Silent");  // set summary to Silent if there is no ringtone picked
            }
            else {
                ringtonePreference.setSummary(RingtoneManager.getRingtone(context, Uri.parse(uri)).getTitle(context));  // set summary to the name of the ringtone
            }
        }
        else {
            ringtonePreference.setSummary("Silent");  // set summary to Silent if there is no ringtone picked
        }
    }
}