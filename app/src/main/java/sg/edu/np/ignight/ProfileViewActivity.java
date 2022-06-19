package sg.edu.np.ignight;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import sg.edu.np.ignight.ProfileView.ProfileViewInterestsAdapter;

public class ProfileViewActivity extends AppCompatActivity {
    private String username;
    private String aboutMe;
    private String whatImLookingFor;
    private Integer age;
    private String nameAndAge1;
    private TextView nameAndAge, textView8, textView9;
    private Button ignightButton;
    private ImageButton backButton;
    public ArrayList<String> interestsDisplay;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

        /*
        // pass in userObject via putExtra intent
        UserObject userObject = (UserObject) getIntent().getSerializableExtra("key"); */

        UserObject userObject = new UserObject();
        userObject.setUsername("test");
        userObject.setAboutMe("test2");
        userObject.setAge(123);
        userObject.setRelationshipPref("test3");
        ArrayList<String> interestsDisplayTest = new ArrayList<>();
        interestsDisplayTest.add("test1");
        interestsDisplayTest.add("test2");
        userObject.setInterestList(interestsDisplayTest);

        myRef = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("chat");
        String currentUserUID = FirebaseAuth.getInstance().getUid();
        String targetUserUID = userObject.getUid();


        ArrayList<String> interestsDisplay = new ArrayList<>();
        interestsDisplay = userObject.getInterestList();

        ShowInformation(userObject);

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
        myRef = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("chat");
        String currentUserUID = FirebaseAuth.getInstance().getUid();
        String targetUserUID = userObject.getUid();


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
}