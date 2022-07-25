package sg.edu.np.ignight;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

public class NotificationSettingsFragment extends PreferenceFragmentCompat {

    private final int PICK_MESSAGE_RINGTONE_REQUEST_CODE = 1;
    private final int PICK_CHAT_REQUEST_RINGTONE_REQUEST_CODE = 2;
    private SharedPreferences sharedPreferences;
    private String currentMessageRingtone, currentChatRequestRingtone;
    private SwitchPreference messageNotificationEnabled, highPriorityMessageNotification, chatRequestNotificationEnabled, highPriorityChatRequestNotification;
    private Preference messageRingtonePreference, chatRequestRingtonePreference;
    private ListPreference messageVibrationPreference, chatRequestVibrationPreference;
    Context context;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        context = getActivity().getApplicationContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);  // get sharedPreferences

        // get preferences
        messageNotificationEnabled = findPreference(SettingsActivity.KEY_MESSAGE_NOTIFICATION_ENABLED);
        messageRingtonePreference = findPreference(SettingsActivity.KEY_MESSAGE_NOTIFICATION_RINGTONE);
        messageVibrationPreference = findPreference(SettingsActivity.KEY_MESSAGE_NOTIFICATION_VIBRATION);
        highPriorityMessageNotification = findPreference(SettingsActivity.KEY_MESSAGE_NOTIFICATION_PRIORITY);

        chatRequestNotificationEnabled = findPreference(SettingsActivity.KEY_CHAT_REQUEST_NOTIFICATION_ENABLED);
        chatRequestRingtonePreference = findPreference(SettingsActivity.KEY_CHAT_REQUEST_NOTIFICATION_RINGTONE);
        chatRequestVibrationPreference = findPreference(SettingsActivity.KEY_CHAT_REQUEST_NOTIFICATION_VIBRATION);
        highPriorityChatRequestNotification = findPreference(SettingsActivity.KEY_CHAT_REQUEST_NOTIFICATION_PRIORITY);

        // initialize fields (show ringtone/vibration picker if notifications are enabled)
        boolean messageNotificationEnabled = sharedPreferences.getBoolean(SettingsActivity.KEY_MESSAGE_NOTIFICATION_ENABLED, true);
        showMessagePreference(messageNotificationEnabled);

        boolean chatRequestNotificationEnabled = sharedPreferences.getBoolean(SettingsActivity.KEY_CHAT_REQUEST_NOTIFICATION_ENABLED, true);
        showChatRequestPreference(chatRequestNotificationEnabled);

        // hide/show ringtone/vibration picker when push notification value is changed
        this.messageNotificationEnabled.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                showMessagePreference((boolean) newValue);

                return true;
            }
        });

        this.chatRequestNotificationEnabled.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                showChatRequestPreference((boolean) newValue);

                return true;
            }
        });

        // set default value to default ringtone
        messageRingtonePreference.setDefaultValue(Settings.System.DEFAULT_NOTIFICATION_URI.toString());
        chatRequestRingtonePreference.setDefaultValue(Settings.System.DEFAULT_NOTIFICATION_URI.toString());

        // get existing ringtone and initialize ringtone picker summary
        currentMessageRingtone = sharedPreferences.getString(SettingsActivity.KEY_MESSAGE_NOTIFICATION_RINGTONE, null);
        setRingtoneSummary(currentMessageRingtone, messageRingtonePreference);

        currentChatRequestRingtone = sharedPreferences.getString(SettingsActivity.KEY_CHAT_REQUEST_NOTIFICATION_RINGTONE, null);
        setRingtoneSummary(currentChatRequestRingtone, chatRequestRingtonePreference);

        // initialize summary
        String messageVibration = sharedPreferences.getString(SettingsActivity.KEY_MESSAGE_NOTIFICATION_VIBRATION, getResources().getStringArray(R.array.vibration_preferences_values)[2]);
        messageVibrationPreference.setSummary(getResources().getStringArray(R.array.vibration_preferences)[messageVibrationPreference.findIndexOfValue(messageVibration)]);

        String chatRequestVibration = sharedPreferences.getString(SettingsActivity.KEY_CHAT_REQUEST_NOTIFICATION_VIBRATION, getResources().getStringArray(R.array.vibration_preferences_values)[2]);
        chatRequestVibrationPreference.setSummary(getResources().getStringArray(R.array.vibration_preferences)[chatRequestVibrationPreference.findIndexOfValue(chatRequestVibration)]);

        // update summary of vibration picker when value changes
        messageVibrationPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                messageVibrationPreference.setSummary(getResources().getStringArray(R.array.vibration_preferences)[messageVibrationPreference.findIndexOfValue((String) newValue)]);

                return true;
            }
        });

        chatRequestVibrationPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                chatRequestVibrationPreference.setSummary(getResources().getStringArray(R.array.vibration_preferences)[chatRequestVibrationPreference.findIndexOfValue((String) newValue)]);

                return true;
            }
        });
    }

    // show/hide message notification settings
    private void showMessagePreference(boolean show) {
        messageRingtonePreference.setVisible(show);
        messageVibrationPreference.setVisible(show);
        highPriorityMessageNotification.setVisible(show);
    }

    // show/hide chat request notification settings
    private void showChatRequestPreference(boolean show) {
        chatRequestRingtonePreference.setVisible(show);
        chatRequestVibrationPreference.setVisible(show);
        highPriorityChatRequestNotification.setVisible(show);
    }

    // ringtone picker reference: https://issuetracker.google.com/issues/37057453?pli=1#comment3
    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == messageRingtonePreference) {

            // get current ringtone
            currentMessageRingtone = sharedPreferences.getString(SettingsActivity.KEY_MESSAGE_NOTIFICATION_RINGTONE, null);

            Intent intent = initRingtonePickerIntent(currentMessageRingtone);

            startActivityForResult(intent, PICK_MESSAGE_RINGTONE_REQUEST_CODE);
            return true;
        }
        else if (preference == chatRequestRingtonePreference) {

            // get current ringtone
            currentChatRequestRingtone = sharedPreferences.getString(SettingsActivity.KEY_CHAT_REQUEST_NOTIFICATION_RINGTONE, null);

            Intent intent = initRingtonePickerIntent(currentChatRequestRingtone);

            startActivityForResult(intent, PICK_CHAT_REQUEST_RINGTONE_REQUEST_CODE);
            return true;
        }
        else {
            return super.onPreferenceTreeClick(preference);
        }
    }

    // set up intent to pick ringtone
    private Intent initRingtonePickerIntent(String currentRingtone) {
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

        return intent;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_MESSAGE_RINGTONE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {  // get result for message ringtone

            // update ringtone with received values
            Uri ringtone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

            if (ringtone != null) {
                sharedPreferences.edit().putString(SettingsActivity.KEY_MESSAGE_NOTIFICATION_RINGTONE, ringtone.toString()).apply();
            }
            else {
                sharedPreferences.edit().putString(SettingsActivity.KEY_MESSAGE_NOTIFICATION_RINGTONE, null).apply();
            }

            setRingtoneSummary((ringtone == null)?null:ringtone.toString(), messageRingtonePreference);

        }
        else if (requestCode == PICK_CHAT_REQUEST_RINGTONE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {  // get result for chat request ringtone

            // update ringtone with received values
            Uri ringtone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

            if (ringtone != null) {
                sharedPreferences.edit().putString(SettingsActivity.KEY_CHAT_REQUEST_NOTIFICATION_RINGTONE, ringtone.toString()).apply();
            }
            else {
                sharedPreferences.edit().putString(SettingsActivity.KEY_CHAT_REQUEST_NOTIFICATION_RINGTONE, null).apply();
            }

            setRingtoneSummary((ringtone == null)?null:ringtone.toString(), chatRequestRingtonePreference);
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // update the summary of the ringtone picker with the name of the ringtone
    private void setRingtoneSummary(String uri, Preference ringtonePreference) {
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