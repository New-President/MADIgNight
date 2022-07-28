package sg.edu.np.ignight.ChatNotifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.Person;
import androidx.core.app.RemoteInput;
import androidx.core.graphics.drawable.IconCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import sg.edu.np.ignight.BlogActivity;
import sg.edu.np.ignight.ChatActivity;
import sg.edu.np.ignight.ChatRequestActivity;
import sg.edu.np.ignight.CommentSectionActivity;
import sg.edu.np.ignight.R;
import sg.edu.np.ignight.SettingsActivity;

public class ChatNotificationService extends FirebaseMessagingService {

    private DatabaseReference rootDB = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
    private Context context;
    private SharedPreferences sharedPreferences;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        // get data
        Map<String, String> data = message.getData();
        context = this;

        String purpose = data.get("purpose");
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (purpose != null) {
            if (purpose.equals("message")) {
                displayChatNotification(data);
            }
            else if (purpose.equals("request")) {
                displayChatRequestNotification(data);
            }
            else if(purpose.equals("blog")){
                displayBlogNotification(data);
            }
            else if(purpose.equals("comment")){
                displayCommentNotification(data);
            }
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        // update database with new fcmtoken
        DatabaseReference userDB = rootDB.child("user").child(FirebaseAuth.getInstance().getUid());
        Map tokenMap = new HashMap<>();
        tokenMap.put("fcmToken", token);
        userDB.updateChildren(tokenMap);
    }

    private void displayChatRequestNotification(Map<String, String> data) {

        // get sharedPreferences for chat request notification settings
        boolean pushNotifications = sharedPreferences.getBoolean(SettingsActivity.KEY_CHAT_REQUEST_NOTIFICATION_ENABLED, true);

        if (!pushNotifications) {  // don't send notifications if user disabled them from settings
            return;
        }

        boolean highPriority = sharedPreferences.getBoolean(SettingsActivity.KEY_CHAT_REQUEST_NOTIFICATION_PRIORITY, true);
        String ringtone = sharedPreferences.getString(SettingsActivity.KEY_CHAT_REQUEST_NOTIFICATION_RINGTONE, Settings.System.DEFAULT_NOTIFICATION_URI.toString());
        String vibration = sharedPreferences.getString(SettingsActivity.KEY_CHAT_REQUEST_NOTIFICATION_VIBRATION, context.getResources().getStringArray(R.array.vibration_preferences_values)[2]);

        String[] vibrationPatternString = vibration.split(",");

        long[] vibrationPattern = new long[vibrationPatternString.length];

        for (int i = 0; i < vibrationPatternString.length; i++) {
            vibrationPattern[i] = Long.parseLong(vibrationPatternString[i]);
        }


        String requestID = data.get("requestID");
        boolean pending = data.get("pending").equals("true");
        boolean accepted = false;
        if (!pending) {
            accepted = data.get("response").equals("true");
        }

        DatabaseReference requestDB = rootDB.child("chatRequest");
        if (requestID != null) {
            boolean finalAccepted = accepted;  // to use within inner class
            requestDB.child(requestID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        try {
                            String profile;
                            String username;
                            if (!pending) {  // not pending means responded to (show receiver details)
                                profile = snapshot.child("receiverProfile").getValue().toString();
                                username = snapshot.child("receiverName").getValue().toString();
                            }
                            else {
                                profile = snapshot.child("creatorProfile").getValue().toString();
                                username = snapshot.child("creatorName").getValue().toString();
                            }

                            // get profile picture as bitmap
                            ExecutorService executorService = Executors.newFixedThreadPool(2);

                            Future<Bitmap> futureSenderProfile = executorService.submit(new UrlToBitmap(new URL(profile)));

                            // do other tasks while waiting for bitmap

                            // start to create notification
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Ignight");

                            // general settings for notification
                            builder.setSmallIcon(R.mipmap.ic_launcher_round);  // set icon
                            builder.setAutoCancel(true);
                            builder.setOnlyAlertOnce(true);
                            builder.setPriority(highPriority?NotificationCompat.PRIORITY_HIGH: NotificationCompat.PRIORITY_DEFAULT);
                            if (!ringtone.isEmpty()) {
                                builder.setSound(Uri.parse(ringtone));
                            }
                            else {
                                builder.setSound(null);
                            }
                            builder.setVibrate(vibrationPattern);

                            builder.setContentTitle(username);

                            if (pending) {
                                builder.setContentText("Sent you a chat request. Tap to view.");
                            }
                            else {
                                if (finalAccepted) {
                                    builder.setContentText("Accepted your chat request. Tap to view.");
                                }
                                else {
                                    builder.setContentText("Rejected your chat request. Tap to view.");
                                }
                            }

                            // open chatRequestActivity on click
                            // set intent
                            Intent intent = new Intent(context, ChatRequestActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                            // add data to pass to ChatRequestActivity
                            intent.putExtra("position", pending?0:1);

                            // set pendingIntent
                            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                            builder.setContentIntent(pendingIntent);

                            builder.setLargeIcon(futureSenderProfile.get());

                            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

                            // set notification channel for api level > 26
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                String channelId = "IgnightChatRequest";
                                NotificationChannel channel = new NotificationChannel(channelId, "Ignight Chat Request", highPriority?NotificationManager.IMPORTANCE_HIGH:NotificationManager.IMPORTANCE_DEFAULT);
                                if (!ringtone.isEmpty()) {
                                    channel.setSound(Uri.parse(ringtone), new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN).build());
                                }
                                else {
                                    channel.setSound(null, null);
                                }
                                channel.setShowBadge(true);
                                channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
                                channel.enableVibration(true);
                                channel.setVibrationPattern(vibrationPattern);

                                notificationManager.createNotificationChannel(channel);
                                builder.setChannelId(channelId);
                            }

                            // send notification
                            notificationManager.notify(1000, builder.build());
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("onMessageReceived", "onCancelled: " + error.getMessage());
                }
            });
        }
    }

    private void displayChatNotification(Map<String, String> data) {

        // get sharedPreferences for chat notification settings
        boolean pushNotifications = sharedPreferences.getBoolean(SettingsActivity.KEY_MESSAGE_NOTIFICATION_ENABLED, true);

        if (!pushNotifications) {  // don't send notifications if user disabled them from settings
            return;
        }

        boolean highPriority = sharedPreferences.getBoolean(SettingsActivity.KEY_MESSAGE_NOTIFICATION_PRIORITY, true);
        String ringtone = sharedPreferences.getString(SettingsActivity.KEY_MESSAGE_NOTIFICATION_RINGTONE, Settings.System.DEFAULT_NOTIFICATION_URI.toString());
        String vibration = sharedPreferences.getString(SettingsActivity.KEY_MESSAGE_NOTIFICATION_VIBRATION, context.getResources().getStringArray(R.array.vibration_preferences_values)[2]);

        String[] vibrationPatternString = vibration.split(",");

        long[] vibrationPattern = new long[vibrationPatternString.length];

        for (int i = 0; i < vibrationPatternString.length; i++) {
            vibrationPattern[i] = Long.parseLong(vibrationPatternString[i]);
        }

        // get data
        String senderID = data.get("senderID");
        String chatID = data.get("chatID");
        String messageID = data.get("messageID");

        if (ChatActivity.currentChatID.equals(chatID)) {  // don't send notifications if user is in ChatActivity with the received chatID
            return;
        }

        if (!(senderID == null) && !(chatID == null) && !(messageID == null)) {
            String myID = FirebaseAuth.getInstance().getUid();

            rootDB.child("chat").child(chatID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String senderName = snapshot.child("users").child(senderID).getValue().toString();  // get sender name

                    DataSnapshot messageSnapshot = snapshot.child("messages").child(messageID);
                    String text = "";

                    if (messageSnapshot.child("text").exists()) {
                        text = messageSnapshot.child("text").getValue().toString();
                    }

                    String imageUrl = "";
                    if (messageSnapshot.child("media").exists()) {
                        for (DataSnapshot mediaSnapshot : messageSnapshot.child("media").getChildren()) {
                            imageUrl = mediaSnapshot.getValue().toString();
                            break;
                        }
                    }

                    String finalText = text;
                    String finalImageUrl = imageUrl;
                    rootDB.child("user").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String senderProfile = dataSnapshot.child(senderID).child("profileUrl").getValue().toString();  // get sender profile picture
                            String myProfile = dataSnapshot.child(myID).child("profileUrl").getValue().toString();  // get my profile picture

                            try {
                                // get profile pictures as bitmaps
                                ExecutorService executorService = Executors.newFixedThreadPool(2);

                                Future<Bitmap> futureSenderProfile = executorService.submit(new UrlToBitmap(new URL(senderProfile)));
                                Future<Bitmap> futureMyProfile = executorService.submit(new UrlToBitmap(new URL(myProfile)));

                                // do other tasks while waiting for bitmaps

                                // start to create notification
                                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Ignight");

                                // general settings for notification
                                builder.setSmallIcon(R.mipmap.ic_launcher_round);  // set icon
                                builder.setAutoCancel(true);
                                builder.setOnlyAlertOnce(true);
                                builder.setCategory(NotificationCompat.CATEGORY_MESSAGE);
                                builder.setPriority(highPriority?NotificationCompat.PRIORITY_HIGH: NotificationCompat.PRIORITY_DEFAULT);
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
                                chatBundle.putString("chatName", senderName);
                                chatBundle.putString("targetUserID", senderID);

                                intent.putExtras(chatBundle);

                                // set pendingIntent
                                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                                builder.setContentIntent(pendingIntent);

                                // mark as read quick action
                                // create intents
                                Intent markAsReadIntent = new Intent(context, MarkAsReadReceiver.class);

                                // add data to pass to receiver
                                Bundle markAsReadBundle = new Bundle();
                                markAsReadBundle.putString("chatID", chatID);
                                markAsReadBundle.putString("tag", senderID);

                                markAsReadIntent.putExtras(markAsReadBundle);

                                PendingIntent markAsReadPendingIntent = PendingIntent.getBroadcast(context, 0, markAsReadIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                                // create action
                                NotificationCompat.Action markAsReadAction = new NotificationCompat.Action(R.drawable.ic_baseline_read_icon_24, "Mark As Read", markAsReadPendingIntent);

                                // add action
                                builder.addAction(markAsReadAction);

                                // create Persons
                                String myDisplayName = "You";
                                Bitmap myProfileBitmap = futureMyProfile.get();
                                Person sender = new Person.Builder().setName(senderName).setIcon(IconCompat.createWithBitmap(futureSenderProfile.get())).build();
                                Person myself = new Person.Builder().setName(myDisplayName).setIcon(IconCompat.createWithBitmap(myProfileBitmap)).build();

                                // send direct replies
                                // create remoteInput
                                RemoteInput remoteInput = new RemoteInput.Builder("direct_reply").setLabel("Reply").build();

                                // create intents
                                Intent replyIntent = new Intent(context, ReplyReceiver.class);

                                // add data to pass to receiver
                                replyIntent.putExtra("senderID", senderID);
                                replyIntent.putExtra("chatID", chatID);
                                replyIntent.putExtra("chatName", senderName);
                                replyIntent.putExtra("myName", myDisplayName);

                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                myProfileBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                replyIntent.putExtra("bitmapBA", stream.toByteArray());
                                replyIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);

                                PendingIntent replyPendingIntent = PendingIntent.getBroadcast(context, 1, replyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                                // create action
                                NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(R.drawable.ic_baseline_reply_24, "Reply", replyPendingIntent).addRemoteInput(remoteInput).build();

                                // add action
                                builder.addAction(replyAction);

                                // create messagingStyle
                                NotificationCompat.MessagingStyle messagingStyle = new NotificationCompat.MessagingStyle(myself);

                                // set title
                                messagingStyle.setConversationTitle(senderName);

                                // create message
                                NotificationCompat.MessagingStyle.Message notificationMessage = new NotificationCompat.MessagingStyle.Message(finalText, System.currentTimeMillis(), sender);
                                if (!finalImageUrl.equals("")) {
                                    // add image if there is image attached
                                    Uri imageUri = Uri.parse(finalImageUrl).buildUpon().build();
                                    notificationMessage.setData("Image/", imageUri);
                                }

                                // add message
                                messagingStyle.addMessage(notificationMessage);

                                // set messagingStyle
                                builder.setStyle(messagingStyle);

                                NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

                                // set notification channel for api level > 26
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    String channelId = "IgnightChat";
                                    NotificationChannel channel = new NotificationChannel(channelId, "Ignight Chat", highPriority?NotificationManager.IMPORTANCE_HIGH:NotificationManager.IMPORTANCE_DEFAULT);
                                    if (!ringtone.isEmpty()) {
                                        channel.setSound(Uri.parse(ringtone), new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN).build());
                                    }
                                    else {
                                        channel.setSound(null, null);
                                    }
                                    channel.setShowBadge(true);
                                    channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
                                    channel.enableVibration(true);
                                    channel.setVibrationPattern(vibrationPattern);

                                    notificationManager.createNotificationChannel(channel);
                                    builder.setChannelId(channelId);
                                }

                                // send notification
                                // set tag as senderID for easy retrieval
                                notificationManager.notify(senderID, 999, builder.build());

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("onMessageReceived", "onCancelled: " + error.getMessage());
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("onMessageReceived", "onCancelled: " + error.getMessage());
                }
            });
        }
    }

    private void displayBlogNotification(Map<String, String> data) {
        FirebaseUser firebaseAuth = FirebaseAuth.getInstance().getCurrentUser();
        String uid = firebaseAuth.getUid();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        String senderID = data.get("senderID");
        String blogID = data.get("blogID");

        rootDB.child("user").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String imgID = snapshot.child(uid).child("blog").child(blogID).child("imgID").getValue().toString();
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Ignight");
                builder.setSmallIcon(R.mipmap.ic_launcher_round);

                String senderUsername = snapshot.child(senderID).child("username").getValue().toString();

                try{
                    StorageReference storageReference = storage.getReference("blog").child(uid).child(imgID);
                    File localfile = File.createTempFile("tempfile", ".png");
                    storageReference.getFile(localfile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Bitmap bitmap = BitmapFactory.decodeFile(localfile.getAbsolutePath());
                            builder.setLargeIcon(bitmap);
                            builder.setContentTitle("IgNight");
                            builder.setContentText(senderUsername + " liked you blog.");
                            builder.setAutoCancel(true);


                            Intent intent = new Intent(context, BlogActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                            PendingIntent pendingIntent = PendingIntent.getActivity(
                                    context,
                                    100,
                                    intent,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );

                            builder.setContentIntent(pendingIntent);
                            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                String channelId = "IgnightBlogs";
                                NotificationChannel channel = new NotificationChannel(channelId, "Blogs", NotificationManager.IMPORTANCE_HIGH);
                                channel.setShowBadge(true);
                                channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
                                channel.enableVibration(true);

                                mNotificationManager.createNotificationChannel(channel);
                                builder.setChannelId(channelId);
                            }


                            mNotificationManager.notify(999, builder.build());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Failed to retrieve blog image", Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
                catch (Exception ex){
                    Log.d("Load Image Error", "Failed to load image");
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void displayCommentNotification(Map<String, String> data){
        FirebaseUser firebaseAuth = FirebaseAuth.getInstance().getCurrentUser();
        String uid = firebaseAuth.getUid();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        String senderID = data.get("senderID");
        String blogID = data.get("blogID");
        String message = data.get("message");

        rootDB.child("user").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String imgID = snapshot.child(uid).child("blog").child(blogID).child("imgID").getValue().toString();
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Ignight");
                builder.setSmallIcon(R.mipmap.ic_launcher_round);

                String senderUsername = snapshot.child(senderID).child("username").getValue().toString();

                try{
                    StorageReference storageReference = storage.getReference("blog").child(uid).child(imgID);
                    File localfile = File.createTempFile("tempfile", ".png");
                    storageReference.getFile(localfile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Bitmap bitmap = BitmapFactory.decodeFile(localfile.getAbsolutePath());
                            builder.setLargeIcon(bitmap);
                            builder.setContentTitle("IgNight");
                            builder.setContentText(senderUsername + " commented on you blog: " + message);
                            builder.setAutoCancel(true);

                            Intent intent = new Intent(context, CommentSectionActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                            Bundle commentBundle = new Bundle();
                            commentBundle.putString("blogOwnerUid", uid);
                            commentBundle.putString("blogID", blogID);
                            commentBundle.putString("imgID", imgID);

                            intent.putExtras(commentBundle);

                            PendingIntent pendingIntent = PendingIntent.getActivity(
                                    context,
                                    100,
                                    intent,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );

                            builder.setContentIntent(pendingIntent);
                            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                String channelId = "IgnightBlogs";
                                NotificationChannel channel = new NotificationChannel(channelId, "Blogs", NotificationManager.IMPORTANCE_HIGH);
                                channel.setShowBadge(true);
                                channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
                                channel.enableVibration(true);

                                mNotificationManager.createNotificationChannel(channel);
                                builder.setChannelId(channelId);
                            }


                            mNotificationManager.notify(999, builder.build());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Failed to retrieve blog image", Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
                catch (Exception ex){
                    Log.d("Load Image Error", "Failed to load image");
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}

