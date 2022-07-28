package sg.edu.np.ignight.Notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.window.SplashScreen;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.Person;
import androidx.core.app.RemoteInput;
import androidx.core.graphics.drawable.IconCompat;
import androidx.preference.PreferenceManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.json.Json;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import sg.edu.np.ignight.ChatActivity;
import sg.edu.np.ignight.ChatNotifications.MarkAsReadReceiver;
import sg.edu.np.ignight.ChatNotifications.ReplyReceiver;
import sg.edu.np.ignight.ChatNotifications.UrlToBitmap;
import sg.edu.np.ignight.NotificationActivity;
import sg.edu.np.ignight.NotificationHelper;
import sg.edu.np.ignight.R;
import sg.edu.np.ignight.SettingsActivity;

public class BlogNotificationService extends FirebaseMessagingService {

    NotificationManager mNotificationManager;
    private DatabaseReference rootDB = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d("TAG", "Hello" + remoteMessage);
        // get data
        Map<String, String> data = remoteMessage.getData();
        for (String key: data.keySet()){
            Log.d("TAG", "Hello" + key);
        }


        Map<String, String> data123 = remoteMessage.getData();
        Log.d("TAG", "Hello1" + data123);

        ObjectMapper mapper = new ObjectMapper();
        String json = "";
        try {
            json = mapper.writeValueAsString(data123);
            System.out.println(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        String body = data123.get("body");
        String title = data123.get("title");
        Log.d("TAG", "Hello1" + body);
        NotificationHelper.displayNotification(getApplicationContext(), title, body);


    }

    /*private void displayBlogNotification(Map<String, String> data) {

        // get data
        String senderID = data.get("senderID");
        String title = data.get("title");
        String body = data.get("body");

    }*/
}