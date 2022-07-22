package sg.edu.np.ignight.ChatNotifications;

import static android.content.ContentValues.TAG;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.Person;
import androidx.core.app.RemoteInput;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import sg.edu.np.ignight.ChatActivity;
import sg.edu.np.ignight.R;

public class ReplyReceiver extends BroadcastReceiver {

    private String senderID;
    private String chatID;
    private String chatName;
    private Person myself;

    DatabaseReference rootDB, chatDB;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        myself = new Gson().fromJson(bundle.getString("person"), Person.class);
        senderID = bundle.getString("senderID");
        chatID = bundle.getString("chatID");
        chatName = bundle.getString("chatName");

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

    // update notification with replied text
    private void updateNotification(String replyText, Context context) {
        NotificationCompat.MessagingStyle.Message newMessage = new NotificationCompat.MessagingStyle.Message(replyText, System.currentTimeMillis(), myself);

        NotificationCompat.MessagingStyle messagingStyle = getMessagingStyle(context, senderID);

        if (messagingStyle != null) {
            messagingStyle.addMessage(newMessage);
        }

        NotificationCompat.Builder builder = createBuilder(context);

        if (builder != null) {
            builder.setStyle(messagingStyle);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            // set notification channel for api level > 26
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                String channelId = "IgnightChat";
                NotificationChannel channel = new NotificationChannel(channelId, "Ignight Chat", NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
                builder.setChannelId(channelId);
            }

            // send notification
            notificationManager.notify(senderID, 999, builder.build());
        }
    }

    // recreate builder to update notification
    private NotificationCompat.Builder createBuilder(Context context) {
        try {
            // start to create notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Ignight");

            // general settings for notification
            builder.setSmallIcon(R.mipmap.ic_launcher_round);  // set icon
            builder.setAutoCancel(true);
            builder.setOnlyAlertOnce(true);

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

            Bundle replyBundle = new Bundle();
            replyBundle.putString("person", new Gson().toJson(myself));  // send myself (Person object) with the intent
            replyBundle.putString("tag", senderID);  // send senderID with the intent
            replyIntent.putExtras(replyBundle);

            PendingIntent replyPendingIntent = PendingIntent.getBroadcast(context, 0, replyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

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

    private NotificationCompat.MessagingStyle getMessagingStyle(Context context, String tag) {
        NotificationManager notificationmanager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        StatusBarNotification[] activeNotifications = notificationmanager.getActiveNotifications();
        for (StatusBarNotification notification : activeNotifications) {
            if (notification.getTag().equals(tag)) {
                return NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification.getNotification());
            }
        }

        return null;
    }
}
