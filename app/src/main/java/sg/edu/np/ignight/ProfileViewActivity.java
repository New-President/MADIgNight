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

        // Get userObject from main menu
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


        db = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/");
        myRef2 = db.getReference("user");
        myRef = db.getReference().child("chat");

        currentUserUID = FirebaseAuth.getInstance().getUid();
        targetUserUID = user.getUid();
        interestsDisplay = new ArrayList<>();
        interestsDisplay = user.getInterestList();
        ShowInformation(user);
        setProfilePicture(user);

        // add intents for bottom buttons here
        backButton = findViewById(R.id.profileViewBackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent backToMainMenu = new Intent(getApplicationContext(), MainMenuActivity.class);
                startActivity(backToMainMenu);
            }
        });

        viewBlogsButton = findViewById(R.id.ViewBlogsBtn);
        viewBlogsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent blogPage = new Intent(ProfileViewActivity.this, BlogActivity.class);
                blogPage.putExtra("canEdit", false);
                blogPage.putExtra("user", user);
                startActivity(blogPage);
            }
        });


        ignightButton = findViewById(R.id.button4);
        ignightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean chatExists = false;
                        String existingChatID = "";

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
                                        view.getContext().startActivity(intent);
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
        RecyclerView rv = findViewById(R.id.InterestsRecyclerView);
        ProfileViewInterestsAdapter adapter = new ProfileViewInterestsAdapter(ProfileViewActivity.this, interestsDisplay);
        LinearLayoutManager layout = new LinearLayoutManager(this);
        layout.setOrientation(LinearLayoutManager.HORIZONTAL);
        Log.d("test3", "rvtest");

        rv.setAdapter(adapter);
        rv.setLayoutManager(layout);
    }

    public void ShowInformation(UserObject userObject){
        // inits and sets profileView details to be displayed
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

    public void setProfilePicture(UserObject userObject){
        profilePicture = (ImageView) findViewById(R.id.imageView);

        // Gets the image url for the profile picture file
        //profilePictureUrl = userObject.getProfilePicUrl(); to be implemented in the future

        // code for retrieving profile picture url when getProfileUrl is not being used
        // Reference to an image file in cloud storage

        // extract user child to get profile picture name
        myRef2.child(userObject.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // get profile picture file name
                String profilePictureName = snapshot.child("Profile Picture").getValue().toString();
                StorageReference storageReference = FirebaseStorage.
                        getInstance().
                        getReference("profilePicture/" +
                                userObject.getUid() +
                                "/" +
                                profilePictureName);
                Glide.with(getApplicationContext())
                        .load(storageReference)
                       .into(profilePicture);
                Log.d("test2", profilePictureName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // if there is an error retrieving profile pics, show toast
                Log.d("testError", "testing");
                Toast.makeText(getApplicationContext(),
                        "Error retrieving profile photo. Please try again later.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}