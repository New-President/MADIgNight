package sg.edu.np.ignight;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

    private TextView nameAndAge, textView8, textView9;

    private Button ignightButton, viewBlogsButton;
    private ImageButton backButton;

    private ImageView profilePicture;

    public ArrayList<String> interestsDisplay;

    private DatabaseReference myRef, myRef2;
    private FirebaseDatabase db;
    private Map userMap;
    private FirebaseAuth mAuth;

    private Uri imageUri;
    private String myUri;
    private StorageReference storageProfilePic;
    private UploadTask uploadTask;

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
        myRef2 = db.getReference("user");
        myRef = db.getReference().child("chat");

        // obtain user info and init
        currentUserUID = FirebaseAuth.getInstance().getUid();
        targetUserUID = user.getUid();
        interestsDisplay = new ArrayList<>();
        interestsDisplay = user.getInterestList();

        // Show user information and display their profile picture
        ShowInformation(user);
        setProfilePicture(user);

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
        viewBlogsButton = findViewById(R.id.ViewBlogsBtn);
        viewBlogsButton.setOnClickListener(new View.OnClickListener() {
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
        ignightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Passes relevant info to chat based on whether the chat exists or not
                        boolean chatExists = false;
                        String existingChatID = "";
                        // Check if the chat with the target IgNighted user already exists
                        for (DataSnapshot chatIdSnapshot : snapshot.getChildren()) {
                            ArrayList<String> usersInChat = new ArrayList<>();
                            for (DataSnapshot userIdSnapshot : chatIdSnapshot.child("users").getChildren()) {
                                usersInChat.add(userIdSnapshot.getKey());
                            }
                            if (usersInChat.contains(currentUserUID) && usersInChat.contains(targetUserUID)) {
                                chatExists = true;
                                existingChatID = chatIdSnapshot.getKey();
                                break;
                            }
                        }
                        // Passes relevant info to the chat if the chat does not exist
                        if (!chatExists) {
                            String newChatID = myRef.push().getKey();
                            Map userMap = new HashMap<>();
                            userMap.put(currentUserUID, true);
                            userMap.put(targetUserUID, true);
                            myRef.child(newChatID).child("users").updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(view.getContext(), ChatActivity.class);
                                        Bundle bundle = new Bundle();
                                        bundle.putString("chatID", newChatID);
                                        bundle.putString("chatName", user.getUsername());
                                        bundle.putString("targetUserID", targetUserUID);
                                        intent.putExtras(bundle);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        view.getContext().startActivity(intent);
                                        finish();
                                    }
                                    else {
                                        task.getException().printStackTrace();
                                    }
                                }
                            });
                        }
                        else {
                            Intent intent = new Intent(view.getContext(), ChatActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("chatID", existingChatID);
                            bundle.putString("chatName", user.getUsername());
                            bundle.putString("targetUserID", targetUserUID);
                            intent.putExtras(bundle);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            view.getContext().startActivity(intent);
                            finish();
                        }
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

        nameAndAge1 = username + ", " + age.toString();
        nameAndAge = (TextView) findViewById(R.id.NameAndAgeTextView);
        nameAndAge.setText(nameAndAge1);
        textView8 = (TextView) findViewById(R.id.textView8);
        textView8.setText(aboutMe);
        textView9 = (TextView) findViewById(R.id.textView9);
        textView9.setText(whatImLookingFor);
    }

    // Retrieves profile picture from Firebase Storage and sets it as profile picture
    public void setProfilePicture(UserObject userObject){
        profilePicture = (ImageView) findViewById(R.id.imageView);

        // Extract user child to get profile picture name
        myRef2.child(userObject.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Get profile picture file name
                String profilePictureName = snapshot.child("Profile Picture").getValue().toString();
                StorageReference storageReference = FirebaseStorage.
                        getInstance().
                        getReference("profilePicture/" +
                                userObject.getUid() +
                                "/" +
                                profilePictureName);
                // Set profile picture using Glide
                Glide.with(getApplicationContext())
                        .load(storageReference)
                       .into(profilePicture);
                Log.d("test2", profilePictureName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // If there is an error retrieving profile pics, show toast
                Log.d("testError", "testing");
                Toast.makeText(getApplicationContext(),
                        "Error retrieving profile photo. Please try again later.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}