package sg.edu.np.ignight;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ProfileViewActivity extends AppCompatActivity {
    private String username;
    private String aboutMe;
    private String whatImLookingFor;
    private String nameAndAgeJoined;
    private String age;
    private String stringInterests;
    private ArrayList interests;
    private HashMap userInfo;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private TextView nameAndAge, textView8, textView9;
    private Button ignightButton;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

        // gets the user UID here to identify which user it is
        // current UID is for testing only
        String uid = "SqDiaNh7KGhYd09lWeVpVrRTSKc2";

        // insert profile interest data here
        database = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app");
        myRef = database.getReference("user").child(uid);

        // User profile data from the database will be extracted for display
        // Interests are stored as children
        // UID here will be extracted from the profile as a string
        interests = new ArrayList();
        ArrayList<String> interestsDisplay = new ArrayList<>();

        // profile information will be collected in a loop in onDataChange
        userInfo = new HashMap();

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) { // extracts profile info
                if(snapshot.exists()){
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        String data = dataSnapshot.getKey().toString();
                        String data1 = dataSnapshot.getValue().toString();
                        userInfo.put(data, data1);
                        Log.d("test", data1);
                    }

                    // userInfo contains all of the relevant data
                    aboutMe = (String) userInfo.get("About Me");
                    username = (String) userInfo.get("username");
                    whatImLookingFor = (String) userInfo.get("Relationship Preference");
                    age = (String) userInfo.get("Age");
                    stringInterests = (String) userInfo.get("Interest");

                    // converts stringInterests into array after converting string
                    stringInterests = stringInterests.substring(1, stringInterests.length() - 1);
                    interests = new ArrayList<String>(Arrays.asList(stringInterests.split(", ")));
                    for (int i = 0; i < interests.size(); i++){
                        String b = (String) interests.get(i);
                        String a = b.substring(0, b.length() - 1);
                        interestsDisplay.add(a);
                        Log.d("test2", a);
                    }

                    // set text
                    nameAndAge = (TextView) findViewById(R.id.NameAndAgeTextView);
                    nameAndAgeJoined = username + ", " + age;
                    nameAndAge.setText(nameAndAgeJoined);
                    textView8 = (TextView) findViewById(R.id.textView8);
                    textView8.setText(aboutMe);
                    textView9 = (TextView) findViewById(R.id.textView9);
                    textView9.setText(whatImLookingFor);
                }
                else{
                    // if there is any issue, this toast error will popup
                    Log.d("failed", "failed");
                    Toast.makeText(getApplicationContext(),
                            "Profile retrieval failed. Please try again later.",
                            Toast.LENGTH_LONG)
                            .show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // if there is any issue, this toast error will popup
                Log.d("failed", "failed");
                Toast.makeText(getApplicationContext(),
                        "Profile retrieval failed. Please try again later.",
                        Toast.LENGTH_LONG)
                        .show();
            }
        });


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
                Intent startChat = new Intent(getApplicationContext(), ChatActivity.class);
                startActivity(startChat);
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
}