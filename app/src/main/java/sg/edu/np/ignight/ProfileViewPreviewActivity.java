package sg.edu.np.ignight;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import sg.edu.np.ignight.ChatNotifications.ChatRequestNotificationSender;
import sg.edu.np.ignight.Objects.UserObject;
import sg.edu.np.ignight.ProfileView.ProfileViewInterestsAdapter;

public class ProfileViewPreviewActivity extends AppCompatActivity {
    private String username;
    private String aboutMe;
    private String whatImLookingFor;
    private Integer age;
    private String nameAndAge1;
    private String currentUserUID, gender;
    private String profilePictureUrl;
    private String preferredGender;
    private ArrayList preferredDateLocation;

    private TextView nameAndAge, textView8, textView9, textView11;

    private Button viewBlogsBtn, interestsBtn;
    private ImageButton backButton;

    private ConstraintLayout headerLayout;

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

    TextView personalInfoText, nameAndAgeText,myInterestsText,aboutMeText,whatImLookingForText,myIdealDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view_preview);

        // Get userObject from main menu to obtain user info
        UserObject user = (UserObject) getIntent().getSerializableExtra("user");

        // Firebase and database init
        db = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/");
        userDB = db.getReference().child("user");
        chatDB = db.getReference().child("chat");


        personalInfoText = (TextView) findViewById(R.id.PersonalInfo2);
        nameAndAgeText = (TextView) findViewById(R.id.NameAndAgeTextView2);;
        myInterestsText = (TextView) findViewById(R.id.textView2);
        aboutMeText = (TextView) findViewById(R.id.textView72);
        whatImLookingForText = (TextView) findViewById(R.id.textView102);
        myIdealDate = (TextView) findViewById(R.id.textView62);
        viewBlogsBtn = findViewById(R.id.ViewBlogsBtn2);
        headerLayout = findViewById(R.id.constraintLayout2);

        // obtain user info and init
        currentUserUID = FirebaseAuth.getInstance().getUid();
        interestsDisplay = new ArrayList<>();
        interestsDisplay = user.getInterestList();

        // User customisation
        profileCustomisation();

        // Show user information
        ShowInformation(user);

        // Intents for buttons
        // Return back to main menu
        backButton = findViewById(R.id.profileViewBackButton2);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent backToMainMenu = new Intent(getApplicationContext(), ViewProfileCustomisation.class);
                backToMainMenu.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(backToMainMenu);
                finish();
            }
        });

        // View the profile's blogs
        viewBlogsBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                // Pass user object to blog for blog to retrieve user info
                Intent blogPage = new Intent(ProfileViewPreviewActivity.this, BlogActivity.class);
                blogPage.putExtra("canEdit", false);
                blogPage.putExtra("user", user);
                startActivity(blogPage);
            }
        });

        // RecyclerView for interests
        RecyclerView rv = findViewById(R.id.InterestsRecyclerView2);
        ProfileViewInterestsAdapter adapter = new ProfileViewInterestsAdapter(ProfileViewPreviewActivity.this, interestsDisplay);
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
        gender = (String) userObject.getGender();
        preferredDateLocation = (ArrayList) userObject.getDateLocList();

        // Changes preferred date locations to a string
        StringBuilder preferredDateLocationDisplay = new StringBuilder();
        for (Object s : preferredDateLocation)
        {
            preferredDateLocationDisplay.append(s);
            preferredDateLocationDisplay.append(", ");
        }
        preferredDateLocationDisplay.append(preferredGender);

        nameAndAge1 = username + ", " + age.toString() + ", " + gender;
        nameAndAge = (TextView) findViewById(R.id.NameAndAgeTextView2);
        nameAndAge.setText(nameAndAge1);
        textView8 = (TextView) findViewById(R.id.textView82);
        textView8.setText(aboutMe);
        textView9 = (TextView) findViewById(R.id.textView92);
        textView9.setText(whatImLookingFor);
        textView11 = (TextView) findViewById(R.id.textView112);
        textView11.setText(preferredDateLocationDisplay);

        // display profile picture
        profilePicture = findViewById(R.id.imageView2);
        Glide.with(getApplicationContext()).load(userObject.getProfilePicUrl()).placeholder(R.drawable.ic_baseline_image_24).into(profilePicture);
    }

    // profile view customisation check & application of themes, if any
    private void profileCustomisation(){
        userDB.child(currentUserUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // If the target user chose to do accent theme customisation
                if(snapshot.child("AccentThemeOption").exists()){
                    // Obtain the user's option
                    String targetUserAccentThemeOption = snapshot.child("AccentThemeOption")
                            .child("themes")
                            .getValue()
                            .toString();
                    // Set the target user's accent theme choice
                    if(targetUserAccentThemeOption.equals("Green")){
                        // Set color to all buttons
                        viewBlogsBtn.setBackgroundResource(R.drawable.green_accent_theme);
                        headerLayout.setBackgroundResource(R.drawable.green_accent_theme);
                    }
                    else if(targetUserAccentThemeOption.equals("Yellow")){
                        // Set color to all buttons
                        viewBlogsBtn.setBackgroundResource(R.drawable.gradient_background);
                        headerLayout.setBackgroundResource(R.drawable.gradient_background);

                    }
                    else if(targetUserAccentThemeOption.equals("Purple")){
                        viewBlogsBtn.setBackgroundResource(R.drawable.purple_accent_theme);
                        headerLayout.setBackgroundResource(R.drawable.purple_accent_theme);
                    }
                    else if(targetUserAccentThemeOption.equals("Blue")){
                        viewBlogsBtn.setBackgroundResource(R.drawable.blue_accent_theme);
                        headerLayout.setBackgroundResource(R.drawable.blue_accent_theme);
                    }
                    else if(targetUserAccentThemeOption.equals("Red")){
                        Log.d("correctTheme", "correctTheme");
                        viewBlogsBtn.setBackgroundResource(R.drawable.red_accent_theme);
                        headerLayout.setBackgroundResource(R.drawable.red_accent_theme);
                    }
                    else if(targetUserAccentThemeOption.equals("Teal")){
                        viewBlogsBtn.setBackgroundResource(R.drawable.teal_accent_theme);
                        headerLayout.setBackgroundResource(R.drawable.teal_accent_theme);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileViewPreviewActivity.this,
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
        viewBlogsBtn.setBackgroundResource(R.drawable.gradient_background);
        headerLayout.setBackgroundResource(R.drawable.gradient_background);
    }
}
