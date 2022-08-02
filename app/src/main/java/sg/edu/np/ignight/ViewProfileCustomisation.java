package sg.edu.np.ignight;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Member;
import java.util.List;

public class ViewProfileCustomisation extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private ImageButton backButton3;
    private Button saveChangesButton;
    private Spinner fontSpinner, accentThemeSpinner ;
    private String uid, existingFontItem, existingAccentThemeItem;

    // Init for firebase
    private FirebaseUser user;
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    // Spinner options for fonts and accent theme
    /*private String[] fontDropdownOptions = {"Select font", "Amaranth", "Cormorant", "Poppins", "Ropa", "Square Peg"};*/
    private String[] accentThemeDropdownOptions = {"Select theme", "Green", "Yellow", "Purple",
            "Blue", "Red", "Teal"};
    private ArrayAdapter<String> fontArrayAdapter,accentThemeArrayAdapter;
    private SpinnerMemberViewProfileCustomisationFont spinnerMember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile_customisation);


        // Firebase logic to store the user's options
        // - Allows customisations to be shown on different devices on the same account
        // - Other users can see the personalisation on the current user's profile
        user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();
        database = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/");
        myRef = database.getReference("user").child(uid);


        // There are three kinds of customisations for a user to personalize their own profile view:
        // - Change font used in profile view
        // - Change the accent theme of their own profile


        /*
        // Init spinner for font change
        // Font spinner drop down list and accent theme
        fontSpinner = (Spinner)findViewById(R.id.FontDropdown);
        fontSpinner.setOnItemSelectedListener(this);
        spinnerMember = new SpinnerMemberViewProfileCustomisationFont();
        fontArrayAdapter = new ArrayAdapter<String>(
                ViewProfileCustomisation.this,
                android.R.layout.simple_spinner_item,
                fontDropdownOptions);
        fontArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fontSpinner.setAdapter(fontArrayAdapter);*/




        // - Change the accent theme of their own profile
        accentThemeSpinner = (Spinner)findViewById(R.id.ChangeAccentThemeDropdown1);
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
                Intent backToMainMenu = new Intent(getApplicationContext(), MainMenuActivity.class)
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
                SaveValue2(existingAccentThemeItem);
            }
        });
    }

    // For spinner dropdown lists
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        String text = adapterView.getItemAtPosition(position).toString();
        // Does not actually set the profile customisations here.
        // Profile customisations are set in viewProfile, when the viewing user
        // sees the view profile. The viewProfile activity then retrieves the user details
        // and their selected customisations

        /*
        // Retrieves font and accent theme items and sets them
        switch (adapterView.getId()){
            case R.id.FontDropdown:
                existingFontItem = fontSpinner.getSelectedItem().toString();
                Log.d("existingFontItem", existingFontItem);
                break;
            case R.id.ChangeAccentThemeDropdown1:
                existingAccentThemeItem = accentThemeSpinner.getSelectedItem().toString();
                Log.d("existingAccentThemeItem", existingAccentThemeItem);
                break;
        }*/
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // Nothing to be done here
    }

    public void SaveValue(String item) {
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(item.equals("Select font")){
                    Toast.makeText(ViewProfileCustomisation.this, "Select a font", Toast.LENGTH_SHORT).show();
                }
                else{
                    spinnerMember.setFonts(item);
                    myRef.child("FontOption").setValue(spinnerMember);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void SaveValue2(String item) {
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(item.equals("Select theme")){
                    Toast.makeText(ViewProfileCustomisation.this, "Select a theme", Toast.LENGTH_SHORT).show();
                }
                else{
                    spinnerMember.setFonts(item);
                    myRef.child("AccentThemeOption").setValue(spinnerMember);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}