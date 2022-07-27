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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MarkAsReadReceiver extends BroadcastReceiver {

    private String tag;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        String chatID = bundle.getString("chatID");
        tag = bundle.getString("tag");


        DatabaseReference chatDB = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("chat").child(chatID);

        String currentUserUID = FirebaseAuth.getInstance().getUid();

        // set all received messages as read
        Query query = chatDB.child("messages").orderByChild("isSeen");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        if (childSnapshot.child("isSeen").getValue().toString().equals("false")) {
                            if (!childSnapshot.child("creator").getValue().toString().equals(currentUserUID)) {
                                childSnapshot.getRef().child("isSeen").setValue(true);
                            }
                        }
                    }

                    chatDB.child("unread").child(currentUserUID).setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error.getMessage());
            }
        });

        dismissNotification(context);
    }

    // cancel notification
    private void dismissNotification(Context context) {
        NotificationManager notificationmanager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationmanager.cancel(tag, 999);
    }
}
