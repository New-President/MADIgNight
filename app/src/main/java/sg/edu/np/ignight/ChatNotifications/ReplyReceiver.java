package sg.edu.np.ignight.ChatNotifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;

import androidx.core.app.NotificationCompat;
import androidx.core.app.Person;
import androidx.core.app.RemoteInput;

import com.google.gson.Gson;

import sg.edu.np.ignight.ChatActivity;
import sg.edu.np.ignight.R;

public class ReplyReceiver extends BroadcastReceiver {

    private String senderID;
    private String chatID;
    private String chatName;
    private Person myself;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        myself = new Gson().fromJson(bundle.getString("person"), Person.class);
        senderID = bundle.getString("senderID");
        chatID = bundle.getString("chatID");
        chatName = bundle.getString("chatName");

        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);

        if (remoteInput != null) {
            String replyText = remoteInput.getString("direct_reply");
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
                // set tag as senderID for easy retrieval
                notificationManager.notify(senderID, 999, builder.build());
            }
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
