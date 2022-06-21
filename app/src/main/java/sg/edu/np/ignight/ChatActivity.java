package sg.edu.np.ignight;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.transition.Slide;
import android.transition.Transition;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import sg.edu.np.ignight.Chat.MediaAdapter;
import sg.edu.np.ignight.Chat.MessageAdapter;
import sg.edu.np.ignight.Chat.MessageObject;
import sg.edu.np.ignight.Objects.TimestampObject;
import sg.edu.np.ignight.Objects.UserObject;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView messageRV, mediaRV;
    private RecyclerView.Adapter messageAdapter, mediaAdapter;
    private LinearLayoutManager messageLayoutManager, mediaLayoutManager;

    private ArrayList<MessageObject> messageList;
    private ArrayList<String> mediaUriList;
    private String currentUserUID, targetUserID, chatID;
    private int PICK_IMAGE_INTENT = 1;

    private DatabaseReference rootDB, chatDB;

    private ImageView removeMediaButton, scrollToChatBottomButton;
    private EditText messageInput;
    private ImageButton addMediaButton, sendMessageButton, backButton;
    private TextView userOnlineStatus;
    private ProgressBar sendMessageProgressBar;
    private LinearLayout messageLayoutHeaderUserInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // initialize Fresco (library to view images full screen)
        Fresco.initialize(this);

        Bundle bundle = getIntent().getExtras();
        chatID = bundle.getString("chatID");
        String chatName = bundle.getString("chatName");
        targetUserID = bundle.getString("targetUserID");

        currentUserUID = FirebaseAuth.getInstance().getUid();

        rootDB = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
        chatDB = rootDB.child("chat").child(chatID);

        sendMessageButton = findViewById(R.id.sendMessage);
        addMediaButton = findViewById(R.id.addMedia);
        backButton = findViewById(R.id.messageLayoutHeaderBack);
        removeMediaButton = findViewById(R.id.removeMedia);
        messageInput = findViewById(R.id.messageInput);
        scrollToChatBottomButton = findViewById(R.id.scrollToChatBottom);
        userOnlineStatus = findViewById(R.id.userOnlineStatus);
        sendMessageProgressBar = findViewById(R.id.sendMessageProgressBar);
        messageLayoutHeaderUserInfo = findViewById(R.id.messageLayoutHeaderUserInfo);

        TextView headerChatName = findViewById(R.id.messageLayoutHeaderChatName);
        headerChatName.setText(chatName);

        showTargetUserStatus();
        getChatMessages();
        initializeMessage();
        initializeMedia();

        // send message
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        // add media
        addMediaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        // go back to MainMenuActivity
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainMenuActivity.class);
                intent.putExtra("showFrag", "chatlist");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        // remove media
        removeMediaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeMedia();
            }
        });

        // scroll to the bottom of the messages
        scrollToChatBottomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                messageLayoutManager.smoothScrollToPosition(messageRV, new RecyclerView.State(), messageList.size() - 1);
                enableScrollToChatBottomButton(false);
            }
        });

        // view profile of user
        messageLayoutHeaderUserInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ProfileViewActivity.class);
                DatabaseReference targetUserDB = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("user").child(targetUserID);
                targetUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String uid = targetUserID;
                        ArrayList<String> dateLocList = new ArrayList<>();
                        ArrayList<String> interestList = new ArrayList<>();

                        String phone = snapshot.child("phone").getValue().toString();
                        String aboutMe = snapshot.child("About Me").getValue().toString();
                        String gender = snapshot.child("Gender").getValue().toString();
                        String genderPref = snapshot.child("Gender Preference").getValue().toString();
                        String profilePicUrl = snapshot.child("Profile Picture").getValue().toString();
                        String relationshipPref = snapshot.child("Relationship Preference").getValue().toString();
                        String username = snapshot.child("username").getValue().toString();
                        String profileCreated = snapshot.child("profileCreated").getValue().toString();
                        int age = Integer.parseInt(snapshot.child("Age").getValue().toString());

                        for (DataSnapshot dateLocSnapshot : snapshot.child("Date Location").getChildren()) {
                            dateLocList.add(dateLocSnapshot.getKey());
                        }
                        for (DataSnapshot interestSnapshot : snapshot.child("Interest").getChildren()) {
                            interestList.add(interestSnapshot.getKey());
                        }

                        UserObject user = new UserObject(uid, aboutMe, age, dateLocList, gender, genderPref, interestList, profilePicUrl, relationshipPref, phone, profileCreated, username);

                        intent.putExtra("user", user);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "onCancelled: " + error.getMessage());
                    }
                });
            }
        });

        // show button to scroll to bottom of chat when the recyclerview is scrolled
        messageRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int lastChildPosition = layoutManager.findLastCompletelyVisibleItemPosition();
                enableScrollToChatBottomButton(lastChildPosition < layoutManager.getItemCount() - 1);
            }
        });

    }

    // update status of other user
    private void showTargetUserStatus() {
        DatabaseReference userPresenceDB = rootDB.child("presence").child(targetUserID);

        userPresenceDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DataSnapshot connectionSnapshot = snapshot.child("connection");
                    DataSnapshot lastOnlineSnapshot = snapshot.child("lastOnline");

                    if (connectionSnapshot.getValue().toString().equals("true")) {
                        userOnlineStatus.setText("Online");
                    }
                    else {
                        try {
                            String today = new TimestampObject(new Date().toString()).getDate();
                            TimestampObject lastSeen = new TimestampObject(lastOnlineSnapshot.getValue().toString());

                            if (today.equals(lastSeen.getDate())) {
                                userOnlineStatus.setText("Last seen today at " + lastSeen.getTime());
                            }
                            else {
                                userOnlineStatus.setText("Last seen at " + lastSeen.getDateTime());
                            }

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    userOnlineStatus.setVisibility(View.VISIBLE);
                }
                else {
                    userOnlineStatus.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                userOnlineStatus.setVisibility(View.GONE);
                Log.e(TAG, "onCancelled: " + error.getMessage());
            }
        });
    }

    // show/hide button to scroll to bottom of chat
    private void enableScrollToChatBottomButton(boolean toEnable) {
        scrollToChatBottomButton.setVisibility((toEnable)?View.VISIBLE: View.GONE);
        scrollToChatBottomButton.setClickable(toEnable);
    }

    // get list of chat messages
    private void getChatMessages() {
        chatDB.child("messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ArrayList<String> messageIDList = new ArrayList<>();

                if (snapshot.exists()) {
                    if (!messageIDList.contains(snapshot.getKey())) {

                        messageIDList.add(snapshot.getKey());

                        String creatorId = snapshot.child("creator").getValue().toString();
                        String timestamp = snapshot.child("timestamp").getValue().toString();

                        String text = "";
                        ArrayList<String> mediaUrlList = new ArrayList<>();

                        if (snapshot.child("text").getValue() != null) {
                            text = snapshot.child("text").getValue().toString();
                        }

                        if (snapshot.child("media").hasChildren()) {
                            for (DataSnapshot mediaSnapshot : snapshot.child("media").getChildren()) {
                                mediaUrlList.add(mediaSnapshot.getValue().toString());
                            }
                        }

                        MessageObject message = null;

                        try {
                            message = new MessageObject(chatID, snapshot.getKey(), creatorId, text, timestamp, mediaUrlList);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        message.setFirstMessage(messageList);
                        message.setSent(true);

                        messageList.add(message);
                        messageLayoutManager.smoothScrollToPosition(messageRV, new RecyclerView.State(), messageList.size() - 1);

                        messageAdapter.notifyDataSetChanged();

                        MessageObject finalMessage = message;
                        ValueEventListener messageIsSeenListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.getValue() == null) {
                                    finalMessage.setSeen(false);
                                }
                                else {
                                    finalMessage.setSeen(snapshot.getValue().toString().equals("true"));
                                }

                                messageAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "onCancelled: " + error.getMessage());
                            }
                        };

                        chatDB.child("messages").child(snapshot.getKey()).child("isSeen").addValueEventListener(messageIsSeenListener);

                        message.setDbRef(chatDB.child("messages").child(snapshot.getKey()).child("isSeen"));
                        message.setListener(messageIsSeenListener);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error.getMessage());
            }
        });
    }

    // show loading progress bar for sending messages
    private void showSendMessageProgressBar(boolean show) {
        sendMessageButton.setVisibility(show?View.GONE:View.VISIBLE);
        sendMessageProgressBar.setVisibility(show?View.VISIBLE:View.GONE);
    }

    // send message
    private void sendMessage() {

        String messageText = messageInput.getText().toString().trim();
        boolean validText = !messageText.isEmpty();

        ArrayList<String> mediaUriListCopy = new ArrayList<>(mediaUriList);

        if (validText || !mediaUriListCopy.isEmpty()) {
            messageInput.setText(null);
            mediaUriList.clear();
            hideSendMediaLayout();

            mediaAdapter.notifyDataSetChanged();

            showSendMessageProgressBar(true);
        }

        String messageId = chatDB.push().getKey();
        DatabaseReference newMessageDB = chatDB.child("messages").child(messageId);

        final Map newMessageMap = new HashMap<>();

        if (validText) {
            newMessageMap.put("text", messageText);
        }
        newMessageMap.put("creator", currentUserUID);
        newMessageMap.put("timestamp", new Date().toString());

        newMessageMap.put("isSeen", false);

        ArrayList<String> mediaUrlList = new ArrayList<>();

        if (!mediaUriListCopy.isEmpty()) {
            final int[] totalMediaUploaded = {0};
            ArrayList<String> mediaIdList = new ArrayList<>();

            for (String mediaUri : mediaUriListCopy) {
                String mediaId = newMessageDB.child("media").push().getKey();
                mediaIdList.add(mediaId);
                final StorageReference filePath = FirebaseStorage.getInstance("gs://madignight.appspot.com").getReference().child("chat").child(chatID).child(messageId).child(mediaId);

                UploadTask uploadTask = filePath.putFile(Uri.parse(mediaUri));

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                mediaUrlList.add(uri.toString());

                                newMessageMap.put("/media/" + mediaIdList.get(totalMediaUploaded[0]) + "/" , uri.toString());

                                totalMediaUploaded[0] += 1;

                                if (totalMediaUploaded[0] == mediaUriListCopy.size()) {
                                    updateDatabaseWithNewMessage(newMessageDB, newMessageMap);
                                }
                            }
                        });
                    }
                });
            }
        }
        else {
            if (validText) {
                updateDatabaseWithNewMessage(newMessageDB, newMessageMap);
            }
        }
    }

    // update database with new message (from sendMessage())
    private void updateDatabaseWithNewMessage(DatabaseReference newMessageDb, Map newMessageMap) {
        newMessageDb.updateChildren(newMessageMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                showSendMessageProgressBar(false);
            }
        });
    }

    // open gallery to add pictures
    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture(s)"), PICK_IMAGE_INTENT);
    }

    // process the pictures received
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == RESULT_OK) {
                if (requestCode == PICK_IMAGE_INTENT) {
                    if (data.getClipData() == null) {
                        mediaUriList.add(data.getData().toString());
                    }
                    else {
                        for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                            mediaUriList.add(data.getClipData().getItemAt(i).getUri().toString());
                        }
                    }

                    showSendMediaLayout();

                    mediaAdapter.notifyDataSetChanged();
                }
            }
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // clear pictures selected
    private void removeMedia() {
        mediaUriList.clear();
        hideSendMediaLayout();
        mediaAdapter.notifyDataSetChanged();
    }

    // hide layout of pictures selected
    private void hideSendMediaLayout() {
        Transition transition = new Slide(Gravity.BOTTOM);
        transition.addTarget(mediaRV);

        removeMediaButton.setVisibility(View.GONE);
        mediaRV.setVisibility(View.GONE);
        mediaRV.setPadding(0, 0, 0, 0);
        addMediaButton.setEnabled(true);
    }

    // show layout of pictures selected
    private void showSendMediaLayout() {
        Transition transition = new Slide(Gravity.BOTTOM);
        transition.addTarget(mediaRV);

        removeMediaButton.setVisibility(View.VISIBLE);
        mediaRV.setVisibility(View.VISIBLE);
        mediaRV.setPadding(0, 10, 0, 10);
        addMediaButton.setEnabled(false);
    }

    // initialize media recyclerview
    private void initializeMedia() {
        mediaUriList = new ArrayList<>();
        mediaRV = findViewById(R.id.mediaRV);
        mediaRV.setNestedScrollingEnabled(false);
        mediaLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        mediaRV.setLayoutManager(mediaLayoutManager);
        mediaAdapter = new MediaAdapter(getApplicationContext(), mediaUriList);
        mediaRV.setAdapter(mediaAdapter);
        mediaRV.setItemAnimator(new DefaultItemAnimator());
    }

    // initialize messages recyclerview
    private void initializeMessage() {
        messageList = new ArrayList<>();
        messageRV = findViewById(R.id.messageRV);
        messageRV.setNestedScrollingEnabled(false);
        messageRV.setHasFixedSize(false);
        messageLayoutManager = new LinearLayoutManager(getApplicationContext());
        messageLayoutManager.setStackFromEnd(true);
        messageRV.setLayoutManager(messageLayoutManager);
        messageAdapter = new MessageAdapter(getApplicationContext(), messageList);
        messageRV.setAdapter(messageAdapter);
        messageRV.setItemAnimator(new DefaultItemAnimator());
    }
}