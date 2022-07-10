package sg.edu.np.ignight.Menu;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import sg.edu.np.ignight.ChatActivity;
import sg.edu.np.ignight.ProfileViewActivity;
import sg.edu.np.ignight.R;
import sg.edu.np.ignight.Objects.UserObject;

public class MainMenuAdapter extends RecyclerView.Adapter<MainMenuViewHolder>{
    private ArrayList<UserObject> data;
    private Context c;
    private LinearLayoutManager layoutManager;
    private DatabaseReference myRef2;
    private FirebaseDatabase db;

    public MainMenuAdapter(Context c, ArrayList<UserObject> data, LinearLayoutManager layoutManager){
        this.c = c;
        this.data = data;
        this.layoutManager = layoutManager;
    }

    @NonNull
    @Override
    public MainMenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_menu_layout, parent, false );
        return new MainMenuViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull MainMenuViewHolder holder, int position) {
        UserObject user = data.get(position);
        db = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/");
        myRef2 = db.getReference("user");
        holder.Name.setText(user.getUsername());
        Button next = holder.Reject;
        Button ignight = holder.Accept;
        ImageView profile = holder.ProfilePic_menu;

        Glide.with(c.getApplicationContext()).load(user.getProfilePicUrl()).placeholder(R.drawable.ic_baseline_image_24).into(profile);

        next.setOnClickListener(new View.OnClickListener() {  // goes to the next user in the list or first user (if currently showing user is the last user)
            @Override
            public void onClick(View view) {
                if (holder.getAdapterPosition() != data.size() - 1) {
                    layoutManager.scrollToPosition(holder.getAdapterPosition() + 1);
                }
                else {
                    layoutManager.scrollToPosition(0);
                }

                Toast.makeText(c,"Thank you for your feedback! we will try not to recommend you this type of people next time!",Toast.LENGTH_SHORT ).show();
            }
        });

        ignight.setOnClickListener(new View.OnClickListener() { // start chat with the target user
            @Override
            public void onClick(View view) {
                DatabaseReference rootDB = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
                DatabaseReference chatDB = rootDB.child("chat");
                DatabaseReference chatRequestDB = rootDB.child("chatRequest");
                DatabaseReference userDB = rootDB.child("user");

                String currentUserUID = FirebaseAuth.getInstance().getUid();
                String targetUserUID = user.getUid();

                userDB.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean chatExists = false;
                        String existingChatID = "";

                        // check if a chat exists between the two users
                        if (snapshot.child(currentUserUID).child("chats").exists()) {
                            for (DataSnapshot chatIdSnapshot : snapshot.child(currentUserUID).child("chats").getChildren()) {
                                if (chatIdSnapshot.getValue().toString().equals(targetUserUID)) {
                                    chatExists = true;
                                    existingChatID = chatIdSnapshot.getKey();
                                    break;
                                }
                            }
                        }

                        if (!chatExists) {  // chat does not exist between the two users
                            boolean requestSent = false;
                            String sentRequestID = "";

                            // check if current user sent any requests to target user and get the latest requestID (if there are multiple requests sent)
                            if (snapshot.child(currentUserUID).child("chatRequests").child("sent").exists()) {
                                for (DataSnapshot sentSnapshot : snapshot.child(currentUserUID).child("chatRequests").child("sent").getChildren()) {
                                    if (sentSnapshot.getValue().toString().equals(targetUserUID)) {
                                        requestSent = true;
                                        sentRequestID = sentSnapshot.getKey();
                                    }
                                }
                            }

                            boolean requestReceived = false;
                            String receivedRequestID = "";

                            // check if current user received any requests from target user
                            if (snapshot.child(currentUserUID).child("chatRequests").child("received").exists()) {
                                for (DataSnapshot receivedSnapshot : snapshot.child(currentUserUID).child("chatRequests").child("received").getChildren()) {
                                    if (receivedSnapshot.getValue().toString().equals(targetUserUID)) {
                                        requestReceived = true;
                                        receivedRequestID = receivedSnapshot.getKey();
                                        break;
                                    }
                                }
                            }

                            if (requestSent) {  // at least one request has been sent to the target user

                                // check if the request is pending
                                boolean finalRequestReceived = requestReceived;  // to use in inner class
                                String finalReceivedRequestID = receivedRequestID;  // to use in inner class

                                chatRequestDB.child(sentRequestID).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        boolean pending = dataSnapshot.child("pending").getValue().toString().equals("true");

                                        if (pending) {  // request is sent and waiting for response
                                            // use toast to show that request has already been sent
                                            Toast.makeText(c, "Request already sent.", Toast.LENGTH_SHORT).show();
                                        }
                                        else {  // request is sent and already responded to (inactive)
                                            if (finalRequestReceived) {  // request is sent (inactive) and a request is received
                                                // accept the request and start chat
                                                Map dataMap = new HashMap<>();

                                                dataMap.put("userDB", userDB);
                                                dataMap.put("chatDB", chatDB);
                                                dataMap.put("chatRequestDB", chatRequestDB);
                                                dataMap.put("snapshot", snapshot);
                                                dataMap.put("targetUserUID", targetUserUID);
                                                dataMap.put("requestID", finalReceivedRequestID);
                                                dataMap.put("chatName", user.getUsername());
                                                dataMap.put("view", view);

                                                startChat(dataMap);
                                            }
                                            else {  // request is sent (inactive) and no request is received
                                                // send a new request
                                                sendNewRequest(userDB, chatRequestDB, snapshot, targetUserUID);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.e(TAG, "onCancelled: " + error.getMessage());
                                    }
                                });
                            }
                            else {  // no requests have been sent to the target user
                                if (requestReceived) {  // a request is received
                                    // accept the request and start chat
                                    Map dataMap = new HashMap<>();

                                    dataMap.put("userDB", userDB);
                                    dataMap.put("chatDB", chatDB);
                                    dataMap.put("chatRequestDB", chatRequestDB);
                                    dataMap.put("snapshot", snapshot);
                                    dataMap.put("targetUserUID", targetUserUID);
                                    dataMap.put("requestID", receivedRequestID);
                                    dataMap.put("chatName", user.getUsername());
                                    dataMap.put("view", view);

                                    startChat(dataMap);
                                }
                                else {  // no request is received
                                    // send new request
                                    sendNewRequest(userDB, chatRequestDB, snapshot, targetUserUID);
                                }
                            }
                        }
                        else {  // go to the chat if it already exists
                            Intent intent = new Intent(view.getContext(), ChatActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("chatID", existingChatID);
                            bundle.putString("chatName", user.getUsername());
                            bundle.putString("targetUserID", targetUserUID);
                            intent.putExtras(bundle);
                            view.getContext().startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "onCancelled: " + error.getMessage());
                    }
                });
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {// go to profile view
            @Override
            public void onClick(View view) {
                Intent mainmenu_to_profileview = new Intent(c , ProfileViewActivity.class);
                mainmenu_to_profileview.putExtra("user", user);
                mainmenu_to_profileview.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                c.startActivity(mainmenu_to_profileview);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    // create new request and update database
    private void sendNewRequest(DatabaseReference userDB, DatabaseReference chatRequestDB, DataSnapshot snapshot, String targetUserUID) {
        String currentUserUID = FirebaseAuth.getInstance().getUid();
        String newRequestID = chatRequestDB.push().getKey();
        Map newRequestMap = new HashMap<>();
        newRequestMap.put("creatorId", currentUserUID);
        newRequestMap.put("creatorName", snapshot.child(currentUserUID).child("username").getValue().toString());
        newRequestMap.put("creatorProfile", snapshot.child(currentUserUID).child("profileUrl").getValue().toString());
        newRequestMap.put("receiverId", targetUserUID);
        newRequestMap.put("receiverName", snapshot.child(targetUserUID).child("username").getValue().toString());
        newRequestMap.put("receiverProfile", snapshot.child(targetUserUID).child("profileUrl").getValue().toString());
        newRequestMap.put("createTimestamp", new Date().toString());
        newRequestMap.put("pending", true);

        userDB.child(currentUserUID).child("chatRequests").child("sent").child(newRequestID).setValue(targetUserUID);
        userDB.child(targetUserUID).child("chatRequests").child("received").child(newRequestID).setValue(currentUserUID);

        chatRequestDB.child(newRequestID).updateChildren(newRequestMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    Toast.makeText(c, "Request sent.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // update existing request and start new chat
    private void startChat(Map dataMap) {
        DatabaseReference chatDB = (DatabaseReference) dataMap.get("chatDB");
        DatabaseReference userDB = (DatabaseReference) dataMap.get("userDB");
        DatabaseReference chatRequestDB = (DatabaseReference) dataMap.get("chatRequestDB");
        DataSnapshot snapshot = (DataSnapshot) dataMap.get("snapshot");
        String targetUserUID = (String) dataMap.get("targetUserUID");
        String requestID = (String) dataMap.get("requestID");
        View view = (View) dataMap.get("view");
        String chatName = (String) dataMap.get("chatName");

        String currentUserUID = FirebaseAuth.getInstance().getUid();
        String newChatID = chatDB.push().getKey();
        String currentTimestamp = new Date().toString();

        Map newChatMap = new HashMap<>();
        newChatMap.put("users/" + currentUserUID, snapshot.child(currentUserUID).child("username").getValue().toString());
        newChatMap.put("users/" + targetUserUID, snapshot.child(targetUserUID).child("username").getValue().toString());
        newChatMap.put("newChat/" + currentUserUID, true);
        newChatMap.put("newChat/" + targetUserUID, true);
        newChatMap.put("lastUsed", currentTimestamp);

        Map updateRequestMap = new HashMap<>();
        updateRequestMap.put("responseTimestamp", currentTimestamp);
        updateRequestMap.put("pending", false);
        updateRequestMap.put("accepted", true);

        Map updateUserMap = new HashMap<>();
        updateUserMap.put(currentUserUID + "/chats/" + newChatID, targetUserUID);
        updateUserMap.put(currentUserUID + "/chatRequests/received/" + requestID, null);
        updateUserMap.put(targetUserUID + "/chats/" + newChatID, currentUserUID);

        chatRequestDB.child(requestID).updateChildren(updateRequestMap);

        chatDB.child(newChatID).updateChildren(newChatMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    userDB.updateChildren(updateUserMap);

                    Toast.makeText(c, "Request accepted, Chat created.", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(view.getContext(), ChatActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("chatID", newChatID);
                    bundle.putString("chatName", chatName);
                    bundle.putString("targetUserID", targetUserUID);
                    intent.putExtras(bundle);
                    view.getContext().startActivity(intent);
                }
            }
        });
    }
}
