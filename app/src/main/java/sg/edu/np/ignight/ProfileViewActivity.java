package sg.edu.np.ignight;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.bumptech.glide.Glide;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import sg.edu.np.ignight.ChatNotifications.ChatRequestNotificationSender;
import sg.edu.np.ignight.Objects.UserObject;
import sg.edu.np.ignight.ProfileView.ProfileViewInterestsAdapter;

public class ProfileViewActivity extends AppCompatActivity {
    private String username;
    private String aboutMe;
    private String whatImLookingFor;
    private Integer age;
    private String nameAndAge1;
    private String currentUserUID, targetUserUID;
    private String profilePictureUrl;
    private String preferredGender;
    private ArrayList preferredDateLocation;

    private TextView nameAndAge, textView8, textView9, textView11;

    private Button ignightButton, viewBlogsBtn;
    private ImageButton backButton;

    private ImageView profilePicture;

    public ArrayList<String> interestsDisplay;

    private DatabaseReference chatDB, userDB;
    private FirebaseDatabase db;
    private Map userMap;
    private FirebaseAuth mAuth;

    private Uri imageUri;
    private String myUri;
    private StorageReference storageProfilePic;
    private UploadTask uploadTask;

    private Typeface amaranthFont,cormorantFont, poppinsFont, ropaFont, squarePegFont;
    TextView personalInfoText, nameAndAgeText,myInterestsText,aboutMeText,whatImLookingForText,myIdealDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

        // Get userObject from main menu to obtain user info
        UserObject user = (UserObject) getIntent().getSerializableExtra("user");

        Log.d("viewprofileuid", user.getUid());
        /*
        // for testing when there is no userObject. do not remove.
        /*
        userObject = new UserObject();
        userObject.setUsername("test");
        userObject.setAboutMe("test2");
        userObject.setAge(123);
        userObject.setRelationshipPref("test3");
        ArrayList<String> interestsDisplayTest = new ArrayList<>();
        interestsDisplayTest.add("test1");
        interestsDisplayTest.add("test2");
        userObject.setInterestList(interestsDisplayTest);
        userObject.setProfilePicUrl("https://m-cdn.phonearena.com/images/review/5269-wide_1200/Google-Pixel-6-review-big-brain-small-price.jpg");
        */

        // Firebase and database init
        db = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/");
        userDB = db.getReference().child("user");
        chatDB = db.getReference().child("chat");


        personalInfoText = (TextView) findViewById(R.id.PersonalInfo);
        nameAndAgeText = (TextView) findViewById(R.id.NameAndAgeTextView);;
        myInterestsText = (TextView) findViewById(R.id.textView);
        aboutMeText = (TextView) findViewById(R.id.textView7);
        whatImLookingForText = (TextView) findViewById(R.id.textView10);
        myIdealDate = (TextView) findViewById(R.id.textView6);

        // obtain user info and init
        currentUserUID = FirebaseAuth.getInstance().getUid();
        targetUserUID = user.getUid();
        interestsDisplay = new ArrayList<>();
        interestsDisplay = user.getInterestList();

        // custom font type init
        amaranthFont = Typeface.createFromAsset(getAssets(), "font/amaranth_bold.xml");
        cormorantFont = Typeface.createFromAsset(getAssets(), "font/cormorant.ttf");
        poppinsFont = Typeface.createFromAsset(getAssets(), "font/poppins.ttf");
        ropaFont = Typeface.createFromAsset(getAssets(), "font/ropa.ttf");
        squarePegFont = Typeface.createFromAsset(getAssets(), "font/squarepeg.ttf");

        // User customisation
        profileCustomisation();

        // Show user information
        ShowInformation(user);

        // Intents for buttons
        // Return back to main menu
        backButton = findViewById(R.id.profileViewBackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent backToMainMenu = new Intent(getApplicationContext(), MainMenuActivity.class);
                backToMainMenu.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(backToMainMenu);
                finish();
            }
        });

        // View the profile's blogs
        viewBlogsBtn = findViewById(R.id.ViewBlogsBtn);
        viewBlogsBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                // Pass user object to blog for blog to retrieve user info
                Intent blogPage = new Intent(ProfileViewActivity.this, BlogActivity.class);
                blogPage.putExtra("canEdit", false);
                blogPage.putExtra("user", user);
                startActivity(blogPage);
            }
        });

        // IgNight with the user (starts a chat with them)
        ignightButton = findViewById(R.id.button4);
        ignightButton.setEnabled(true);  // set enabled by default
        ignightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ignightButton.setEnabled(false);  // disable button
                DatabaseReference chatRequestDB = db.getReference().child("chatRequest");

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

                            // get the other user's fcm token
                            String fcmToken = null;
                            if (snapshot.child(targetUserUID).child("fcmToken").exists()) {
                                fcmToken = snapshot.child(targetUserUID).child("fcmToken").getValue().toString();
                            }

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

                                String finalFcmToken = fcmToken;  // to use within inner class
                                chatRequestDB.child(sentRequestID).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        boolean pending = dataSnapshot.child("pending").getValue().toString().equals("true");

                                        if (pending) {  // request is sent and waiting for response
                                            // use toast to show that request has already been sent
                                            Toast.makeText(getApplicationContext(), "Request already sent.", Toast.LENGTH_SHORT).show();
                                        }
                                        else {  // request is sent and already responded to (inactive)
                                            if (finalRequestReceived) {  // request is sent (inactive) and a request is received
                                                // accept the request and start chat
                                                Map dataMap = new HashMap<>();

                                                dataMap.put("chatRequestDB", chatRequestDB);
                                                dataMap.put("snapshot", snapshot);
                                                dataMap.put("requestID", finalReceivedRequestID);
                                                dataMap.put("chatName", user.getUsername());
                                                dataMap.put("view", view);

                                                startChat(dataMap, finalFcmToken);
                                            }
                                            else {  // request is sent (inactive) and no request is received
                                                // send a new request
                                                sendNewRequest(chatRequestDB, snapshot, finalFcmToken);
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

                                    dataMap.put("chatRequestDB", chatRequestDB);
                                    dataMap.put("snapshot", snapshot);
                                    dataMap.put("requestID", receivedRequestID);
                                    dataMap.put("chatName", user.getUsername());
                                    dataMap.put("view", view);

                                    startChat(dataMap, fcmToken);
                                }
                                else {  // no request is received
                                    // send new request
                                    sendNewRequest(chatRequestDB, snapshot, fcmToken);
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

                        ignightButton.setEnabled(true);  // enable when finished
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "onCancelled: " + error.getMessage());
                    }
                });
            }
        });

        // RecyclerView for interests
        RecyclerView rv = findViewById(R.id.InterestsRecyclerView);
        ProfileViewInterestsAdapter adapter = new ProfileViewInterestsAdapter(ProfileViewActivity.this, interestsDisplay);
        LinearLayoutManager layout = new LinearLayoutManager(this);
        layout.setOrientation(LinearLayoutManager.HORIZONTAL);
        Log.d("test3", "rvtest");

        rv.setAdapter(adapter);
        rv.setLayoutManager(layout);
    }

    // Show information related to the user
    public void ShowInformation(UserObject userObject){
        // Init and sets profileView details to be displayed
        username = (String) userObject.getUsername();
        aboutMe = (String) userObject.getAboutMe();
        whatImLookingFor = (String) userObject.getRelationshipPref();
        age = (Integer) userObject.getAge();
        preferredGender = (String) userObject.getGenderPref();
        preferredDateLocation = (ArrayList) userObject.getDateLocList();

        // Changes preferred date locations to a string
        StringBuilder preferredDateLocationDisplay = new StringBuilder();
        for (Object s : preferredDateLocation)
        {
            preferredDateLocationDisplay.append(s);
            preferredDateLocationDisplay.append(", ");
        }
        preferredDateLocationDisplay.append(preferredGender);

        nameAndAge1 = username + ", " + age.toString();
        nameAndAge = (TextView) findViewById(R.id.NameAndAgeTextView);
        nameAndAge.setText(nameAndAge1);
        textView8 = (TextView) findViewById(R.id.textView8);
        textView8.setText(aboutMe);
        textView9 = (TextView) findViewById(R.id.textView9);
        textView9.setText(whatImLookingFor);
        textView11 = (TextView) findViewById(R.id.textView11);
        textView11.setText(preferredDateLocationDisplay);

        // display profile picture
        profilePicture = findViewById(R.id.imageView);
        Glide.with(getApplicationContext()).load(userObject.getProfilePicUrl()).placeholder(R.drawable.ic_baseline_image_24).into(profilePicture);
    }

    // create new request and update database
    private void sendNewRequest(DatabaseReference chatRequestDB, DataSnapshot snapshot, String fcmToken) {
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
                    if (fcmToken != null) {
                        ChatRequestNotificationSender sender = new ChatRequestNotificationSender(fcmToken, getApplicationContext(), newRequestID);
                        sender.sendNotification();
                    }

                    Toast.makeText(getApplicationContext(), "Request sent.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // update existing request and start new chat
    private void startChat(Map dataMap, String fcmToken) {
        DatabaseReference chatRequestDB = (DatabaseReference) dataMap.get("chatRequestDB");
        DataSnapshot snapshot = (DataSnapshot) dataMap.get("snapshot");
        String requestID = (String) dataMap.get("requestID");
        View view = (View) dataMap.get("view");
        String chatName = (String) dataMap.get("chatName");

        String newChatID = chatDB.push().getKey();
        String currentTimestamp = new Date().toString();

        Map newChatMap = new HashMap<>();
        newChatMap.put("users/" + currentUserUID, snapshot.child(currentUserUID).child("username").getValue().toString());
        newChatMap.put("users/" + targetUserUID, snapshot.child(targetUserUID).child("username").getValue().toString());
        newChatMap.put("newChat/" + currentUserUID, true);
        newChatMap.put("newChat/" + targetUserUID, true);
        newChatMap.put("lastUsed", currentTimestamp);
        newChatMap.put("onCall", false);

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
                    userDB.updateChildren(updateUserMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (fcmToken != null) {
                                ChatRequestNotificationSender sender = new ChatRequestNotificationSender(fcmToken, getApplicationContext(), requestID, true);
                                sender.sendNotification();
                            }
                        }
                    });

                    Toast.makeText(getApplicationContext(), "Request accepted, Chat created.", Toast.LENGTH_SHORT).show();

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

    // profile view customisation check & application of themes, if any
    private void profileCustomisation(){
        userDB.child(targetUserUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // If the target user chose to do font customisation

                if(snapshot.child("FontOption").exists()){
                    // Obtain the user's font
                    String targetUserFontOption = snapshot.child("FontOption")
                            .getValue()
                            .toString();

                    // Sets the font according to the option in the database
                    if(targetUserFontOption.equals("amaranthFont")){
                        personalInfoText.setTypeface(amaranthFont);
                        nameAndAgeText.setTypeface(amaranthFont);
                        viewBlogsBtn.setTypeface(amaranthFont);
                        myInterestsText.setTypeface(amaranthFont);
                        aboutMeText.setTypeface(amaranthFont);
                        whatImLookingForText.setTypeface(amaranthFont);
                        myIdealDate.setTypeface(amaranthFont);
                        ignightButton.setTypeface(amaranthFont);
                    }
                    else if(targetUserFontOption.equals("cormorantFont")){
                        personalInfoText.setTypeface(cormorantFont);
                        nameAndAgeText.setTypeface(cormorantFont);
                        viewBlogsBtn.setTypeface(cormorantFont);
                        myInterestsText.setTypeface(cormorantFont);
                        aboutMeText.setTypeface(cormorantFont);
                        whatImLookingForText.setTypeface(cormorantFont);
                        myIdealDate.setTypeface(cormorantFont);
                        ignightButton.setTypeface(cormorantFont);
                    }
                    else if(targetUserFontOption.equals("poppinsFont")){
                        personalInfoText.setTypeface(poppinsFont);
                        nameAndAgeText.setTypeface(poppinsFont);
                        viewBlogsBtn.setTypeface(poppinsFont);
                        myInterestsText.setTypeface(poppinsFont);
                        aboutMeText.setTypeface(poppinsFont);
                        whatImLookingForText.setTypeface(poppinsFont);
                        myIdealDate.setTypeface(poppinsFont);
                        ignightButton.setTypeface(poppinsFont);
                    }
                    else if(targetUserFontOption.equals("ropaFont")){
                        personalInfoText.setTypeface(ropaFont);
                        nameAndAgeText.setTypeface(ropaFont);
                        viewBlogsBtn.setTypeface(ropaFont);
                        myInterestsText.setTypeface(ropaFont);
                        aboutMeText.setTypeface(ropaFont);
                        whatImLookingForText.setTypeface(ropaFont);
                        myIdealDate.setTypeface(ropaFont);
                        ignightButton.setTypeface(ropaFont);
                    }
                    else if(targetUserFontOption.equals("squarePegFont")){
                        personalInfoText.setTypeface(squarePegFont);
                        nameAndAgeText.setTypeface(squarePegFont);
                        viewBlogsBtn.setTypeface(squarePegFont);
                        myInterestsText.setTypeface(squarePegFont);
                        aboutMeText.setTypeface(squarePegFont);
                        whatImLookingForText.setTypeface(squarePegFont);
                        myIdealDate.setTypeface(squarePegFont);
                        ignightButton.setTypeface(squarePegFont);
                    }

                }
                // If the target user chose to do accent theme customisation
                if(snapshot.child("AccentThemeOption").exists()){
                    // Obtain the user's font
                    String targetUserAccentThemeOption = snapshot.child("AccentThemeOption")
                            .getValue()
                            .toString();
                    // Set the target user's accent theme choice
                    if(targetUserAccentThemeOption.equals("Green")){
                        // Init color in hex
                        int green = Color.parseColor("#A7F432");
                        // Set color to all buttons
                        viewBlogsBtn.setBackgroundColor(green);
                        ignightButton.setBackgroundColor(green);
                    }
                    else if(targetUserAccentThemeOption.equals("Yellow")){
                        int yellow = Color.parseColor("#FCC267");
                        viewBlogsBtn.setBackgroundColor(yellow);
                        ignightButton.setBackgroundColor(yellow);

                    }
                    else if(targetUserAccentThemeOption.equals("Black")){
                        int black = Color.parseColor("#000000");
                        viewBlogsBtn.setBackgroundColor(black);
                        ignightButton.setBackgroundColor(black);
                    }
                    else if(targetUserAccentThemeOption.equals("Purple")){
                        int purple = Color.parseColor("#B24BF3");
                        viewBlogsBtn.setBackgroundColor(purple);
                        ignightButton.setBackgroundColor(purple);
                    }
                    else if(targetUserAccentThemeOption.equals("Blue")){
                        int blue = Color.parseColor("#1338BE");
                        viewBlogsBtn.setBackgroundColor(blue);
                        ignightButton.setBackgroundColor(blue);
                    }
                    else if(targetUserAccentThemeOption.equals("Red")){
                        int red = Color.parseColor("#FF2400");
                        viewBlogsBtn.setBackgroundColor(red);
                        ignightButton.setBackgroundColor(red);
                    }
                    else if(targetUserAccentThemeOption.equals("Brown")){
                        int brown = Color.parseColor("#4A2511");
                        viewBlogsBtn.setBackgroundColor(brown);
                        ignightButton.setBackgroundColor(brown);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileViewActivity.this,
                        "Error retrieving customisation information",
                        Toast.LENGTH_LONG)
                        .show();
            }
        });;
    }

    // Revert the target user's customisation settings once the activity is exited
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Reset the fonts and the accent themes
        personalInfoText.setTypeface(amaranthFont);
        nameAndAgeText.setTypeface(amaranthFont);
        viewBlogsBtn.setTypeface(amaranthFont);
        myInterestsText.setTypeface(amaranthFont);
        aboutMeText.setTypeface(amaranthFont);
        whatImLookingForText.setTypeface(amaranthFont);
        myIdealDate.setTypeface(amaranthFont);
        ignightButton.setTypeface(amaranthFont);

        int yellow = Color.parseColor("#FCC267");
        viewBlogsBtn.setBackgroundColor(yellow);
        ignightButton.setBackgroundColor(yellow);
    }
}