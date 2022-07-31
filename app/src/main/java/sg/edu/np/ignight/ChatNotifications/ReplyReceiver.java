package sg.edu.np.ignight.ChatNotifications;

import static android.content.ContentValues.TAG;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.Person;
import androidx.core.app.RemoteInput;
import androidx.core.graphics.drawable.IconCompat;
import androidx.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import sg.edu.np.ignight.ChatActivity;
import sg.edu.np.ignight.R;
import sg.edu.np.ignight.SettingsActivity;

// broadcast receiver to send reply to other user and update notification with reply
public class ReplyReceiver extends BroadcastReceiver {

    private String senderID;
    private String chatID;
    private String chatName;
    private Person myself;
    private int notificationID;
    private String tag;
    private SharedPreferences sharedPreferences;
    boolean highPriority;
    private String ringtone;
    private long[] vibrationPattern;
    private Bitmap myBitmap;
    private String myName;
    private String channelId = "IgnightChat";

    DatabaseReference rootDB, chatDB;

    @Override
    public void onReceive(Context context, Intent intent) {
        // get user preferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        highPriority = sharedPreferences.getBoolean(SettingsActivity.KEY_MESSAGE_NOTIFICATION_PRIORITY, true);
        ringtone = sharedPreferences.getString(SettingsActivity.KEY_MESSAGE_NOTIFICATION_RINGTONE, Settings.System.DEFAULT_NOTIFICATION_URI.toString());
        String vibration = sharedPreferences.getString(SettingsActivity.KEY_MESSAGE_NOTIFICATION_VIBRATION, context.getResources().getStringArray(R.array.vibration_preferences_values)[2]);

        String[] vibrationPatternString = vibration.split(",");

        vibrationPattern = new long[vibrationPatternString.length];

        for (int i = 0; i < vibrationPatternString.length; i++) {
            vibrationPattern[i] = Long.parseLong(vibrationPatternString[i]);
        }

        senderID = intent.getStringExtra("senderID");
        chatID = intent.getStringExtra("chatID");
        chatName = intent.getStringExtra("chatName");
        myName = intent.getStringExtra("myName");
        notificationID = intent.getIntExtra("notificationID", 1);
        tag = intent.getStringExtra("tag");

        byte[] byteArray = intent.getByteArrayExtra("bitmapBA");
        myBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        myself = new Person.Builder().setIcon(IconCompat.createWithBitmap(myBitmap)).setName(myName).build();

        rootDB = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
        chatDB = rootDB.child("chat").child(chatID);

        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);

        // send message if there is text input from the direct reply
        if (remoteInput != null) {
            String replyText = remoteInput.getString("direct_reply");

            sendMessage(replyText, context);
        }
    }

    // send message
    private void sendMessage(String message, Context context) {
        String messageID = chatDB.child("messages").push().getKey();

        Map newMessageMap = new HashMap<>();

        // put relevant fields and values in a map to be updated to the database
        newMessageMap.put("messages/" + messageID + "/text", message);

        String timestamp = new Date().toString();

        newMessageMap.put("messages/" + messageID + "/creator", FirebaseAuth.getInstance().getUid());
        newMessageMap.put("messages/" + messageID + "/timestamp", timestamp);
        newMessageMap.put("messages/" + messageID + "/isSeen", false);
        newMessageMap.put("lastUsed", timestamp);

        // update unread messages count for the other user
        chatDB.child("unread").child(senderID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int unreadCount = 0;
                if (snapshot.exists()) {
                    unreadCount = Integer.parseInt(snapshot.getValue().toString());
                }

                newMessageMap.put("unread/" + senderID, unreadCount + 1);

                // update database
                chatDB.updateChildren(newMessageMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        updateNotification(message, context);  // update notification with replied text
                        pushNotification(messageID, context);  // send notification to the other user as well
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error.getMessage());
            }
        });
    }

    // send notification to the other user
    private void pushNotification(String messageID, Context context) {
        rootDB.child("user").child(senderID).child("fcmToken").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String fcmToken = snapshot.getValue().toString();

                    ChatNotificationSender sender = new ChatNotificationSender(fcmToken, FirebaseAuth.getInstance().getUid(), chatID, messageID, context);
                    sender.sendNotification();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error.getMessage());
            }
        });
    }

    // update current notification with replied text
    private void updateNotification(String replyText, Context context) {
        // create new message
        NotificationCompat.MessagingStyle.Message newMessage = new NotificationCompat.MessagingStyle.Message(replyText, System.currentTimeMillis(), myself);

        // get current notification messaging style
        NotificationCompat.MessagingStyle messagingStyle = getMessagingStyle(context);

        if (messagingStyle != null) {
            messagingStyle.addMessage(newMessage);  // add message to messaging style
        }

        // create builder for notification
        NotificationCompat.Builder builder = createBuilder(context);

        if (builder != null) {
            builder.setStyle(messagingStyle);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            // set notification channel for api level > 26
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // create channel
                NotificationChannel channel = new NotificationChannel(channelId, "Ignight Chat", highPriority?NotificationManager.IMPORTANCE_HIGH:NotificationManager.IMPORTANCE_DEFAULT);

                notificationManager.createNotificationChannel(channel);

                // retrieve channel to make updates
                NotificationChannel targetChannel = notificationManager.getNotificationChannel(channelId);
                if (!ringtone.isEmpty()) {
                    targetChannel.setSound(Uri.parse(ringtone), new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN).build());
                }
                else {
                    targetChannel.setSound(null, null);
                }
                targetChannel.setShowBadge(true);
                targetChannel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PRIVATE);
                targetChannel.enableVibration(true);
                targetChannel.setVibrationPattern(vibrationPattern);
                targetChannel.setImportance(highPriority?NotificationManager.IMPORTANCE_HIGH:NotificationManager.IMPORTANCE_DEFAULT);
            }

            // send notification
            notificationManager.notify(tag, notificationID, builder.build());
        }
    }

    // recreate builder to update notification
    private NotificationCompat.Builder createBuilder(Context context) {
        try {
            // start to create notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);

            // general settings for notification
            builder.setSmallIcon(R.mipmap.ic_launcher_round);  // set icon
            builder.setAutoCancel(true);
            builder.setOnlyAlertOnce(true);
            builder.setPriority(highPriority?NotificationCompat.PRIORITY_MAX: NotificationCompat.PRIORITY_DEFAULT);
            if (!ringtone.isEmpty()) {
                builder.setSound(Uri.parse(ringtone));
            }
            else {
                builder.setSound(null);
            }
            builder.setVibrate(vibrationPattern);

            // to launch ChatActivity on click
            // set intent
            Intent intent = new Intent(context, ChatActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            // add data to pass to ChatActivity
            Bundle chatBundle = new Bundle();
            chatBundle.putString("chatID", chatID);
            chatBundle.putString("chatName", chatName);
            chatBundle.putString("targetUserID", senderID);

            intent.putExtras(chatBundle);

            // set pendingIntent
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);

            // send direct replies
            // create remoteInput
            RemoteInput remoteInput = new RemoteInput.Builder("direct_reply").setLabel("Reply").build();

            // create intents
            Intent replyIntent = new Intent(context, ReplyReceiver.class);

            replyIntent.putExtra("senderID", senderID);
            replyIntent.putExtra("chatID", chatID);
            replyIntent.putExtra("chatName", chatName);
            replyIntent.putExtra("myName", myName);
            replyIntent.putExtra("notificationID", notificationID);
            replyIntent.putExtra("tag", tag);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            myBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            replyIntent.putExtra("bitmapBA", stream.toByteArray());
            replyIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);

            PendingIntent replyPendingIntent = PendingIntent.getBroadcast(context, 1, replyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            // create action
            NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(R.drawable.ic_baseline_reply_24, "Reply", replyPendingIntent).addRemoteInput(remoteInput).build();

            // add action
            builder.addAction(replyAction);

            return builder;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // get the messaging style for the current notification
    private NotificationCompat.MessagingStyle getMessagingStyle(Context context) {
        NotificationManager notificationmanager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        StatusBarNotification[] activeNotifications = notificationmanager.getActiveNotifications();
        for (StatusBarNotification notification : activeNotifications) {
            if (notification.getId() == notificationID && notification.getTag().equals(tag)) {
                return NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification.getNotification());
            }
        }

        return null;
    }
}
