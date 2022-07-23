package sg.edu.np.ignight.ChatNotifications;

import static android.content.ContentValues.TAG;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MarkAsReadReceiver extends BroadcastReceiver {

    private String tag;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        String chatID = bundle.getString("chatID");
        String messageID = bundle.getString("messageID");
        tag = bundle.getString("tag");

        dismissNotification(context);

        DatabaseReference chatDB = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("chat").child(chatID);
        DatabaseReference messageDB = chatDB.child("messages").child(messageID);

        // update seen status of messages received
        messageDB.child("isSeen").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    boolean isSeen = snapshot.getValue().toString().equals("true");

                    if (!isSeen) {
                        messageDB.child("isSeen").setValue(true);

                        String myUID = FirebaseAuth.getInstance().getUid();
                        chatDB.child("unread").child(myUID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                chatDB.child("unread").child(myUID).setValue(0);  // set unread count to 0
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "onCancelled: " + error.getMessage());
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error.getMessage());
            }
        });
    }

    // cancel notification
    private void dismissNotification(Context context) {
        NotificationManager notificationmanager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationmanager.cancel(tag, 999);
    }
}
