package sg.edu.np.ignight;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProfileViewActivity extends AppCompatActivity {
    private String username, aboutMe, whatImLookingFor, uid;
    private Integer age;
    private ArrayList interests;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private User userData;
    private Button friendButton, ignightButton;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

        // insert profile interest data here
        database = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app");
        myRef = database.getReference("user");

        // User profile data from the database will be extracted for display
        // Interests are stored as children
        // UID here will be extracted from the profile as a string
        userData = new User();
        interests = new ArrayList();

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) { // extracts profile info
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    // extracts profile info
                    username = (String) dataSnapshot.child("username").getValue();
                    age = (Integer) dataSnapshot.child("Age").getValue();
                    aboutMe = (String) dataSnapshot.child("About Me").getValue();
                    whatImLookingFor = (String) dataSnapshot.child("Relationship Preference").getValue();

                    // sets profile info to a user object, so that the info can be passed around the app
                    userData.Username = username;
                    userData.Age = age;
                    userData.Aboutme = aboutMe;
                    userData.Relationship_pref = whatImLookingFor;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // if there is any issue, this toast error will popup
                Log.d("failed", "failed");
                Toast.makeText(getApplicationContext(),
                        "Profile retrival failed. Please try again later.",
                        Toast.LENGTH_LONG)
                        .show();
            }
        });


        RecyclerView rv = findViewById(R.id.recyclerView);
        ProfileViewInterestsAdapter adapter = new ProfileViewInterestsAdapter(interests);
        LinearLayoutManager layout = new LinearLayoutManager(this);
        layout.setOrientation(LinearLayoutManager.HORIZONTAL);

        rv.setAdapter(adapter);
        rv.setLayoutManager(layout);

        // add intents for bottom buttons here
        backButton = findViewById(R.id.BackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent backToMainMenu = new Intent(getApplicationContext(), MainMenuActivity.this);

            }
        });

        friendButton = findViewById(R.id.button);
        friendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

        ignightButton = findViewById(R.id.createButton);
        ignightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
    }
}