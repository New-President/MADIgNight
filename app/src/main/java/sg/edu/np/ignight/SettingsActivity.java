package sg.edu.np.ignight;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    // static key values for various preference fields
    public static final String KEY_MESSAGE_NOTIFICATION_ENABLED = "messageNotificationsEnabled";
    public static final String KEY_MESSAGE_NOTIFICATION_RINGTONE = "messageNotificationRingtone";
    public static final String KEY_MESSAGE_NOTIFICATION_VIBRATION = "messageNotificationVibration";
    public static final String KEY_MESSAGE_NOTIFICATION_PRIORITY = "messageNotificationPriority";
    public static final String KEY_CHAT_REQUEST_NOTIFICATION_ENABLED = "chatRequestNotificationsEnabled";
    public static final String KEY_CHAT_REQUEST_NOTIFICATION_RINGTONE = "chatRequestNotificationRingtone";
    public static final String KEY_CHAT_REQUEST_NOTIFICATION_VIBRATION = "chatRequestNotificationVibration";
    public static final String KEY_CHAT_REQUEST_NOTIFICATION_PRIORITY = "chatRequestNotificationPriority";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        // go back to main menu when back button is clicked
        ImageButton backButton = findViewById(R.id.chatNotificationSettingsBack);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainMenuActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        if (savedInstanceState == null) {  // load notification settings fragment
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settingsFrame, new NotificationSettingsFragment())
                    .commit();
        }
    }
}