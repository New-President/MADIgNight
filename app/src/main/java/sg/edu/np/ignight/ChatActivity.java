package sg.edu.np.ignight;

import static android.content.ContentValues.TAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.format.DateFormat;
import android.transition.Slide;
import android.transition.Transition;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.facebook.drawee.drawable.ProgressBarDrawable;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
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
import com.stfalcon.frescoimageviewer.ImageViewer;

import org.jitsi.meet.sdk.BroadcastEvent;
import org.jitsi.meet.sdk.BroadcastIntentHelper;
import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import sg.edu.np.ignight.Chat.MediaAdapter;
import sg.edu.np.ignight.Chat.MessageAdapter;
import sg.edu.np.ignight.Chat.MessageObject;
import sg.edu.np.ignight.ChatNotifications.ChatNotificationSender;
import sg.edu.np.ignight.Objects.TimestampObject;
import sg.edu.np.ignight.Objects.UserObject;
import timber.log.Timber;

public class ChatActivity extends AppCompatActivity {

    public static String currentChatID = "";

    private RecyclerView messageRV, mediaRV;
    private RecyclerView.Adapter messageAdapter, mediaAdapter;
    private LinearLayoutManager messageLayoutManager, mediaLayoutManager;

    private ArrayList<MessageObject> messageList;
    private ArrayList<String> mediaUriList;
    private String currentUserUID, targetUserID, chatID, chatName;
    private final int PICK_IMAGE_INTENT = 1;
    private final int count = 1;

    private DatabaseReference rootDB, chatDB;

    private ImageView removeMediaButton, scrollToChatBottomButton, profilePicture;
    private EditText messageInput;
    private ImageButton addMediaButton, sendMessageButton, backButton;
    private TextView userOnlineStatus;
    private ProgressBar sendMessageProgressBar;
    private LinearLayout messageLayoutHeaderUserInfo;
    private Button ProposeDateBtn, acceptButton, declineButton;
    private ViewStub proposeDateViewStub;


    private ChildEventListener messagesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Bundle bundle = getIntent().getExtras();
        chatID = bundle.getString("chatID");
        chatName = bundle.getString("chatName");
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
        profilePicture = findViewById(R.id.chatActivityProfilePicture);
        proposeDateViewStub = findViewById(R.id.proposeDateViewStub);


        TextView headerChatName = findViewById(R.id.messageLayoutHeaderChatName);
        headerChatName.setText(chatName);

        showTargetUserStatus();
        initMessagesListener();
        initializeMessage();
        initializeMedia();

        // send message
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(false, null, null, 0, 0);
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
                DatabaseReference targetUserDB = rootDB.child("user").child(targetUserID);
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
                        String profilePicUrl = snapshot.child("profileUrl").getValue().toString();
                        String relationshipPref = snapshot.child("Relationship Preference").getValue().toString();
                        String username = snapshot.child("username").getValue().toString();
                        String profileCreated = snapshot.child("profileCreated").getValue().toString();
                        int age = Integer.parseInt(snapshot.child("Age").getValue().toString());

                        for (DataSnapshot dateLocSnapshot : snapshot.child("Date Location").getChildren()) {
                            dateLocList.add(dateLocSnapshot.getValue().toString());
                        }
                        for (DataSnapshot interestSnapshot : snapshot.child("Interest").getChildren()) {
                            interestList.add(interestSnapshot.getValue().toString());
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

        // show profile picture of user
        DatabaseReference targetUserDB = rootDB.child("user").child(targetUserID);
        targetUserDB.child("profileUrl").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String profilePictureUrl = snapshot.getValue().toString();
                Glide.with(getApplicationContext()).load(profilePictureUrl).placeholder(R.drawable.ic_baseline_image_24).into(profilePicture);

                // show pictures in full screen when user clicks on the picture
                profilePicture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        GenericDraweeHierarchyBuilder hierarchyBuilder = GenericDraweeHierarchyBuilder.newInstance(getApplicationContext().getResources())
                                .setFailureImage(R.drawable.ic_baseline_error_outline_24)
                                .setProgressBarImage(new ProgressBarDrawable())
                                .setPlaceholderImage(R.drawable.ic_baseline_image_24);

                        new ImageViewer.Builder(view.getContext(), Collections.singletonList(profilePictureUrl))
                                .setStartPosition(0)
                                .hideStatusBar(false)
                                .allowZooming(true)
                                .allowSwipeToDismiss(true)
                                .setCustomDraweeHierarchyBuilder(hierarchyBuilder)
                                .show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error.getMessage());
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

        // Calling button for call function
        ImageView call_btn = findViewById(R.id.btn_call);
        call_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this, R.style.AlertDialogTheme);
                View view1 = LayoutInflater.from(ChatActivity.this).inflate(R.layout.call_confrimation_dialog, (ConstraintLayout)findViewById(R.id.layoutDialogContainer));
                builder.setView(view1);
                final AlertDialog alertDialog = builder.create(); //Display video or voice call dialog

                view1.findViewById(R.id.button_yes_delete).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DatabaseReference chatDB = rootDB.child("chat");
                        chatDB.child(chatID).child("onCall").setValue(true);
                        // Call
                        URL server;
                        try{
                            server = new URL("https://meet.jit.si");
                            JitsiMeetConferenceOptions defaultOptions= new JitsiMeetConferenceOptions.Builder()
                                    .setServerURL(server)
                                    .setFeatureFlag("welcomepage.enabled", false)
                                    .build();
                            JitsiMeet.setDefaultConferenceOptions(defaultOptions);

                        }catch (MalformedURLException e){
                            e.printStackTrace();
                        }
                        JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                                .setRoom(chatID)// Unique ID
                                .setFeatureFlag("welcomepage.enabled", false)
                                .build();
                        JitsiMeetActivity.launch(ChatActivity.this,options);
                        alertDialog.dismiss();
                        // Calling message notification
                        EditText call_text = findViewById(R.id.messageInput);
                        call_text.setText("Hey, I started a call come join me!");
                        sendMessage(false, null, null, 0, 0);
                        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
                            Integer total_count = 0;
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                total_count += 1;
                                if (total_count.equals(count)) {
                                    EditText call_text = findViewById(R.id.messageInput);
                                    call_text.setText("The call just ended, let's have another call next time!");
                                    Log.d("Call Ended","Yes");
                                    sendMessage(false, null, null, 0, 0);
                                    chatDB.child(chatID).child("onCall").setValue(false);
                                }
                            }
                        };
                        IntentFilter intentFilter = new IntentFilter();
                        intentFilter.addAction(BroadcastEvent.Type.CONFERENCE_TERMINATED.getAction());
                        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiver, intentFilter);
                        chatDB.child(chatID).child("onCall").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (Boolean.parseBoolean(snapshot.getValue().toString()) == false) {
                                    Intent hangupBroadcastIntent = BroadcastIntentHelper.buildHangUpIntent();
                                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(hangupBroadcastIntent);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Timber.e("onCancelled: " + error.getMessage());
                            }
                        });
                    }
                });

                view1.findViewById(R.id.button_cancel_delete).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Call
                        DatabaseReference chatDB = rootDB.child("chat");
                        chatDB.child(chatID).child("onCall").setValue(true);
                        URL server;
                        try{
                            server = new URL("https://meet.jit.si");
                            JitsiMeetConferenceOptions defaultOptions= new JitsiMeetConferenceOptions.Builder()
                                    .setServerURL(server)
                                    .setFeatureFlag("welcomepage.enabled", false)
                                    .build();
                            JitsiMeet.setDefaultConferenceOptions(defaultOptions);

                        }catch (MalformedURLException e){
                            e.printStackTrace();
                        }
                        JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                                .setRoom(chatID)// Unique ID
                                .setFeatureFlag("welcomepage.enabled", false)
                                .setVideoMuted(true)
                                .build();
                        JitsiMeetActivity.launch(ChatActivity.this,options);
                        alertDialog.dismiss();
                        // Calling message notification
                        EditText call_text = findViewById(R.id.messageInput);
                        call_text.setText("Hey, I started a call come join me!");
                        sendMessage(false,null, null, 0, 0);
                        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
                            Integer total_count = 0;
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                total_count += 1;
                                if (total_count.equals(count)) {
                                    EditText call_text = findViewById(R.id.messageInput);
                                    call_text.setText("The call just ended, let's have another call next time!");
                                    Log.d("Call Ended","Yes");
                                    sendMessage(false, null, null, 0, 0);
                                    chatDB.child(chatID).child("onCall").setValue(false);
                                }
                            }
                        };
                        IntentFilter intentFilter = new IntentFilter();
                        intentFilter.addAction(BroadcastEvent.Type.CONFERENCE_TERMINATED.getAction());
                        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiver, intentFilter);
                        chatDB.child(chatID).child("onCall").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (Boolean.parseBoolean(snapshot.getValue().toString()) == false) {
                                    Intent hangupBroadcastIntent = BroadcastIntentHelper.buildHangUpIntent();
                                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(hangupBroadcastIntent);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "onCancelled: " + error.getMessage());
                            }
                        });
                    }
                });

                if (alertDialog.getWindow() != null){
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                }
                alertDialog.show();
            }
        });

        ProposeDateBtn = findViewById(R.id.ProposeDate);
        ProposeDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent proposeDate = new Intent(ChatActivity.this, ProposeDateActivity.class);
                startActivityForResult(proposeDate, 1000);
            }
        });


       /* if (proposeDateViewStub.getParent() != null) {
            proposeDateViewStub.inflate();
        } else {
            View inflated =  proposeDateViewStub.inflate();
            Button declineButton = (Button) inflated.findViewById(R.id.declineButton);
            declineButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    messageInput.setText("Declined");
                    sendMessage(false, null, null, 0, 0);
                }
            });
        }*/

    }

    @Override
    public void onBackPressed() {  // override onBackPressed to go to chatlist and not close app (in case activity is started from notification)
        Intent intent = new Intent(getApplicationContext(), MainMenuActivity.class);
        intent.putExtra("showFrag", "chatlist");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        chatDB.child("messages").addChildEventListener(messagesListener);  // attach listener
        currentChatID = chatID;  // set currentChatID to received chatID
    }

    @Override
    protected void onPause() {
        super.onPause();

        chatDB.child("messages").removeEventListener(messagesListener);  // remove listener
        currentChatID = "";  // clear currentChatID
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

    // define child event listener to get new messages
    private void initMessagesListener() {
        ArrayList<String> messageIDList = new ArrayList<>();
        messagesListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {  // initialize messages and add to the list when new messages are sent

                if (snapshot.exists()) {
                    if (!messageIDList.contains(snapshot.getKey())) {

                        messageIDList.add(snapshot.getKey());

                        String creatorId = snapshot.child("creator").getValue().toString();
                        String timestamp = snapshot.child("timestamp").getValue().toString();
                        Boolean proposeDate = false;
                        String dateDescription = "";
                        String dateLocation = "";
                        long startDateTime = 0;
                        long endDateTime = 0;
                        try{
                            proposeDate = Boolean.parseBoolean(snapshot.child("proposeDate").getValue().toString());
                            dateDescription = snapshot.child("dateDescription").getValue().toString();
                            dateLocation = snapshot.child("dateLocation").getValue().toString();
                            startDateTime = Long.parseLong(snapshot.child("startDateTime").getValue().toString());
                            endDateTime = Long.parseLong(snapshot.child("endDateTime").getValue().toString());
                        }catch(Exception e) {
                            e.printStackTrace();
                        }
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
                            message = new MessageObject(chatID, snapshot.getKey(), creatorId, text, timestamp, mediaUrlList, proposeDate, dateDescription, dateLocation, startDateTime, endDateTime);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        message.setFirstMessage(messageList);
                        message.setSent(true);

                        boolean isSeen = snapshot.child("isSeen").getValue().toString().equals("true");

                        if (!creatorId.equals(currentUserUID)) {  // if message is received then set isSeen to true
                            if (!isSeen) {
                                snapshot.child("isSeen").getRef().setValue(true);
                                isSeen = true;
                            }
                        }

                        message.setSeen(isSeen);

                        messageList.add(message);
                        messageLayoutManager.smoothScrollToPosition(messageRV, new RecyclerView.State(), messageList.size() - 1);

                        messageAdapter.notifyDataSetChanged();

                        chatDB.child("unread").child(currentUserUID).setValue(0);  // set unread count to 0
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {  // updates the existing message with the relevant fields when it changes
                for (MessageObject messageIterator : messageList) {
                    if (messageIterator.getMessageId().equals(snapshot.getKey())) {
                        messageIterator.setSeen(snapshot.child("isSeen").getValue().toString().equals("true"));

                        messageAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error.getMessage());
            }
        };
    }

    // show loading progress bar for sending messages
    private void showSendMessageProgressBar(boolean show) {
        sendMessageButton.setVisibility(show?View.GONE:View.VISIBLE);
        sendMessageProgressBar.setVisibility(show?View.VISIBLE:View.GONE);
    }

    // send message
    public void sendMessage(Boolean proposeDate, String dateDescription, String dateLocation, long startDateTime, long endDateTime) {

        // checks if the text entered is valid (gets rid of leading and trailing spaces and checks length is not 0)
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

        String messageId = chatDB.child("messages").push().getKey();

        final Map newMessageMap = new HashMap<>();

        // put relevant fields and values in a map to be updated to the database
        if (validText) {
            newMessageMap.put("messages/" + messageId + "/text", messageText);
        }

        String timestamp = new Date().toString();

        newMessageMap.put("messages/" + messageId + "/creator", currentUserUID);
        newMessageMap.put("messages/" + messageId + "/timestamp", timestamp);
        newMessageMap.put("messages/" + messageId + "/isSeen", false);
        newMessageMap.put("messages/" + messageId + "/proposeDate", proposeDate);
        newMessageMap.put("messages/" + messageId + "/dateDescription", dateDescription);
        newMessageMap.put("messages/" + messageId + "/dateLocation", dateLocation);
        newMessageMap.put("messages/" + messageId + "/startDateTime", startDateTime);
        newMessageMap.put("messages/" + messageId + "/endDateTime", endDateTime);


        newMessageMap.put("lastUsed", timestamp);

        chatDB.child("unread").child(targetUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int unreadCount = 0;
                if (snapshot.exists()) {
                    unreadCount = Integer.parseInt(snapshot.getValue().toString());
                }

                newMessageMap.put("unread/" + targetUserID, unreadCount + 1);

                if (!mediaUriListCopy.isEmpty()) {
                    final int[] totalMediaUploaded = {0};
                    ArrayList<String> mediaIdList = new ArrayList<>();

                    for (String mediaUri : mediaUriListCopy) {
                        String mediaId = chatDB.child("messages/" + messageId + "media").push().getKey();
                        mediaIdList.add(mediaId);
                        // store the image in Firebase Storage
                        final StorageReference filePath = FirebaseStorage.getInstance("gs://madignight.appspot.com").getReference().child("chat").child(chatID).child(messageId).child(mediaId);

                        UploadTask uploadTask = filePath.putFile(Uri.parse(mediaUri));

                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        newMessageMap.put("messages/" + messageId + "/media/" + mediaIdList.get(totalMediaUploaded[0]) + "/" , uri.toString());

                                        totalMediaUploaded[0] += 1;

                                        if (totalMediaUploaded[0] == mediaUriListCopy.size()) {
                                            updateDatabaseWithNewMessage(chatDB, newMessageMap, messageId);
                                        }
                                    }
                                });
                            }
                        });
                    }
                }
                else {
                    if (validText) {
                        updateDatabaseWithNewMessage(chatDB, newMessageMap, messageId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error.getMessage());
            }
        });
    }

    // update database with new message (from sendMessage()) and remove progress bar when done
    private void updateDatabaseWithNewMessage(DatabaseReference newMessageDb, Map newMessageMap, String messageID) {
        newMessageDb.updateChildren(newMessageMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                showSendMessageProgressBar(false);
                pushNotification(messageID);
            }
        });
    }

    // send notification to the other user
    private void pushNotification(String messageID) {
        Context context = getApplicationContext();
        rootDB.child("user").child(targetUserID).child("fcmToken").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {  // send notification if user has fcmToken
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

        Boolean fromProposeDate= data.getBooleanExtra("dateMessage", false);

        if(fromProposeDate) {
            String dateDescription = data.getStringExtra("dateDescription");
            String dateLocation = data.getStringExtra("dateLocation");
            long startDateTime = data.getLongExtra("startDateTime", 0);
            long endDateTime = data.getLongExtra("endDateTime", 0);
            String dateString = DateFormat.format("dd/MM/yyyy | HH:mm", new Date(startDateTime)).toString();
            EditText call_text = findViewById(R.id.messageInput);
            call_text.setText("Date Description: " + dateDescription +"\nDate Location: " + dateLocation + "\nDate and Time: " + dateString);
            sendMessage(true, dateDescription, dateLocation, startDateTime, endDateTime);

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