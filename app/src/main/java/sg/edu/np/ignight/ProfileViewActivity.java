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

    private Button ignightButton;
    private ImageButton backButton;

    private ImageView profilePicture;

    public ArrayList<String> interestsDisplay;

    private DatabaseReference myRef;
    private FirebaseDatabase db;
    private Map userMap;
    private FirebaseAuth mAuth;

    private Uri imageUri;
    private String myUri = "";
    private StorageReference storageProfilePic;
    private UploadTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

        /*
        // pass in userObject via putExtra intent here
        UserObject userObject = (UserObject) getIntent().getSerializableExtra("key"); */

        // FOR TESTING. not final

        UserObject userObject = new UserObject();
        userObject.setUsername("test");
        userObject.setAboutMe("test2");
        userObject.setAge(123);
        userObject.setRelationshipPref("test3");
        ArrayList<String> interestsDisplayTest = new ArrayList<>();
        interestsDisplayTest.add("test1");
        interestsDisplayTest.add("test2");
        userObject.setInterestList(interestsDisplayTest);
        userObject.setProfilePicUrl("https://m-cdn.phonearena.com/images/review/5269-wide_1200/Google-Pixel-6-review-big-brain-small-price.jpg");

        db = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/");
        myRef = db.getReference().child("chat");

        currentUserUID = FirebaseAuth.getInstance().getUid();
        targetUserUID = userObject.getUid();
        interestsDisplay = new ArrayList<>();
        interestsDisplay = userObject.getInterestList();
        ShowInformation(userObject);
        setProfilePicture(userObject);

        // add intents for bottom buttons here
        backButton = findViewById(R.id.profileViewBackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent backToMainMenu = new Intent(getApplicationContext(), MainMenuActivity.class);
                startActivity(backToMainMenu);
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
                        ArrayList<String> usersInChat = new ArrayList<>();
                        for (DataSnapshot chatIdSnapshot : snapshot.getChildren()) {
                            for (DataSnapshot userIdSnapshot : chatIdSnapshot.child("users").getChildren()) {
                                usersInChat.add(userIdSnapshot.getKey());
                            }
                            if (usersInChat.contains(getApplicationContext()) && usersInChat.contains(targetUserUID)) {
                                chatExists = true;
                                break;
                            }
                        }
                        if (!chatExists) {
                            String newChatID = myRef.push().getKey();
                            userMap = new HashMap<>();
                            userMap.put(currentUserUID, true);
                            userMap.put(targetUserUID, true);

                            myRef.child(newChatID).child("users").updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(view.getContext(), ChatActivity.class);
                                        Bundle bundle = new Bundle();
                                        bundle.putString("chatID", newChatID);
                                        bundle.putString("chatName", userObject.getUsername());
                                        bundle.putString("targetUserID", targetUserUID);
                                        intent.putExtras(bundle);
                                        view.getContext().startActivity(intent);
                                    }
                                }
                            });
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
        profilePictureUrl = userObject.getProfilePicUrl();

        Glide.with(this).load(profilePictureUrl).into(profilePicture);
    }
}