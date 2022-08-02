package sg.edu.np.ignight;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.giphy.sdk.core.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;

import sg.edu.np.ignight.Objects.UserObject;

public class ViewProfileCustomisation extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private ImageButton backButton3;
    private Button saveChangesButton;
    private Spinner accentThemeSpinner ;
    private String uid, existingAccentThemeItem;
    private UserObject currentUser;

    // Init for firebase
    private FirebaseUser user;
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    // Spinner option for accent theme
    private String[] accentThemeDropdownOptions = {"Select theme", "Green", "Yellow", "Purple",
            "Blue", "Red", "Teal"};
    private ArrayAdapter<String> accentThemeArrayAdapter;
    private SpinnerMemberViewProfileCustomisationTheme spinnerMember;

    private Intent backToMainMenu, profilePreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile_customisation);

        // Firebase logic to store the user's options
        // Other users can see the personalisation on the current user's profile
        user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();
        database = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/");
        myRef = database.getReference("user").child(uid);
        getCurrentUserInfo();
        // - Change the accent theme of their own profile
        accentThemeSpinner = (Spinner)findViewById(R.id.ChangeAccentThemeDropdown1);
        spinnerMember = new SpinnerMemberViewProfileCustomisationTheme();
        accentThemeSpinner.setOnItemSelectedListener(this);
        accentThemeArrayAdapter = new ArrayAdapter<String>(
                ViewProfileCustomisation.this,
                android.R.layout.simple_spinner_item,
                accentThemeDropdownOptions);
        accentThemeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        accentThemeSpinner.setAdapter(accentThemeArrayAdapter);

        // Return back to main menu
        backButton3 = findViewById(R.id.backButton2);
        backButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backToMainMenu = new Intent(getApplicationContext(), MainMenuActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(backToMainMenu);
                finish();
            }
        });

        // Save customisation to current user profile
        saveChangesButton = findViewById(R.id.SaveChangesButton);
        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Obtains the user's spinner selection option and saves them
                //SaveValue(existingFontItem);
                SaveValue(existingAccentThemeItem);
                startActivity(profilePreview);
            }
        });
    }

    // Does not actually set the profile customisations here.
    // Profile customisations are set in viewProfile, when the viewing user
    // sees the view profile. The viewProfile activity then retrieves the user details
    // and their selected customisations

    // For spinner dropdown lists
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {


        // Retrieves accent theme items and produces a toast message
        // existingAccentThemeItem will also have its value stored in the database
        existingAccentThemeItem = accentThemeSpinner.getSelectedItem().toString();
        // Toasts the user's custom selection
        if(existingAccentThemeItem!="Select theme"){
            Toast.makeText(ViewProfileCustomisation.this,
                    existingAccentThemeItem,
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // Nothing to be done here
    }

    public void SaveValue(String item) {
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(item.equals("Select theme")){
                    Toast.makeText(ViewProfileCustomisation.this,
                            "Select a theme",
                            Toast.LENGTH_SHORT).show();
                }
                else{
                    spinnerMember.setThemes(item);
                    myRef.child("AccentThemeOption").setValue(spinnerMember);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewProfileCustomisation.this,
                        "Theme setting error",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // Gets current user info and assigns them to current use object for display
    private void getCurrentUserInfo() {
        // Goes to the current user
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // From homepage_fragment.class activity
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
                    Log.d("test", interestSnapshot.getValue().toString());
                    interestList.add(interestSnapshot.getValue().toString());
                }

                currentUser = new UserObject(uid, aboutMe, age, dateLocList, gender, genderPref, interestList, profilePicUrl, relationshipPref, phone, profileCreated, username);


                // putExtra is here due to async
                profilePreview = new Intent(getApplicationContext(), ProfileViewPreviewActivity.class)
                        .putExtra("user", currentUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewProfileCustomisation.this,
                        "Error retrieving user info",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}