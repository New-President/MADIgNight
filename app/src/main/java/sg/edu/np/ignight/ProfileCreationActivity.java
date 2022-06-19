package sg.edu.np.ignight;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;

public class ProfileCreationActivity extends AppCompatActivity {

    ArrayList<String> data = new ArrayList<>();
    ArrayList<String> dateLocList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_creation);

        InitDropdown();

        ImageButton backBtn = findViewById(R.id.BackButton);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        String[] interests = {"Running", "Cooking", "Gaming", "Swimming", "Reading", "Shopping", "Others"};
        Button interestButton = findViewById(R.id.InterestButton);
        boolean[] selectedInterest = new boolean[interests.length];
        ArrayList<Integer> checkedList = new ArrayList<>();

        interestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileCreationActivity.this);
                builder.setTitle("Select your Interests");
                builder.setCancelable(false);    //set dialog non cancelable

                builder.setMultiChoiceItems(interests, selectedInterest, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        if (b){
                            //when checkbox is selected, add position into list
                            checkedList.add(i);
                            Collections.sort(checkedList);
                        }
                        else{
                            //when checkbox is not selected, remove position from list
                            checkedList.remove(Integer.valueOf(i));
                        }
                    }
                });

                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        data.removeAll(data);
                        for (int j = 0; j < checkedList.size(); j++){
                            data.add(interests[checkedList.get(j)]);
                        }
                        InitRecyclerView();
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                builder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        for (int j = 0; j < selectedInterest.length; j++){
                            selectedInterest[j] = false;
                            checkedList.clear();
                            data.removeAll(data);
                        }
                        InitRecyclerView();
                    }
                });
                builder.show();
            }
        });

        /*RecyclerView rv = findViewById(R.id.interestRecyclerView);
        ProfileCreationAdapter adapter = new ProfileCreationAdapter(MainActivity.this, data);
        LinearLayoutManager layout = new LinearLayoutManager(this);
        layout.setOrientation(LinearLayoutManager.HORIZONTAL);

        rv.setAdapter(adapter);
        rv.setLayoutManager(layout);*/

        EditText aboutMeInput = findViewById(R.id.AboutMeInput);
        TextView aboutMeView = findViewById(R.id.AboutMeTextView);

        aboutMeInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                aboutMeView.setText(charSequence.toString());
                float size = aboutMeView.getTextSize();
                size = size/3;
                aboutMeInput.setTextSize((int)size);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        // Saving to Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference myRef = database.getReference("user");


        Button saveChanges = findViewById(R.id.SaveChanges);
        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatabaseReference nested = myRef.child("Testing");
                /*DatabaseReference nested = myRef.child(FirebaseAuth.getInstance().getUid());*/

                // Username
                EditText inputName = findViewById(R.id.InputName);
                String username = inputName.getText().toString();

                //Gender
                Spinner GenderDropdown = findViewById(R.id.GenderDropdown);
                String gender = GenderDropdown.getSelectedItem().toString();

                // Age
                EditText inputAge = findViewById(R.id.AgeInput);
                String tempAge = inputAge.getText().toString();

                // About me
                EditText inputAboutMe = findViewById(R.id.AboutMeInput);
                String aboutMe = inputAboutMe.getText().toString();

                // Interest
                int interestSize = data.size();

                // Relationship Preference
                Spinner RelationshipPrefDropdown = findViewById(R.id.RelationshipPrefDropdown);
                String RelationshipPref = RelationshipPrefDropdown.getSelectedItem().toString();

                // Gender Preference
                Spinner GenderPrefdropdown = findViewById(R.id.GenderPrefDropdown);
                String GenderPref = GenderPrefdropdown.getSelectedItem().toString();

                // Date Location
                int dateLocSize = dateLocList.size();

                ArrayList<String> missingList = new ArrayList<>();
                // Checking for missing inputs
                if(IncompleteActions()){
                    // Insert into database
                    // Username
                    nested.child("username").setValue(username);

                    //Gender
                    nested.child("Gender").setValue(gender);

                    // Age
                    int age = Integer.parseInt(tempAge);
                    nested.child("Age").setValue(age);

                    // About me
                    nested.child("About Me").setValue(aboutMe);

                    // Interest
                    DatabaseReference nestedInterest = nested.child("Interest");
                    for(int i = 0; i < interestSize; i++){
                        String interest = data.get(i);
                        nestedInterest.child("Interest" + i+1).setValue(interest);
                    }

                    // Relationship Preference
                    nested.child("Relationship Preference").setValue(RelationshipPref);

                    // Gender Preference
                    nested.child("Gender Preference").setValue(GenderPref);

                    // Date Location
                    DatabaseReference nestedDateLoc = nested.child("Date Location");
                    for(int i = 0; i < dateLocSize; i++){
                        String dateLoc = dateLocList.get(i);
                        nestedDateLoc.child("Date Location" + i+1).setValue(dateLoc);
                    }
                }
                else{
                    if(username.equals("")){
                        missingList.add("Username");
                    }
                    if(tempAge.equals("")){
                        missingList.add("Age");
                    }
                    else if (Integer.parseInt(tempAge) < 18){
                        missingList.add("Invalid Age");
                    }
                    if(aboutMe.equals("")){
                        missingList.add("About Me");
                    }
                    if(interestSize == 0){
                        missingList.add("Interest");
                    }
                    if(dateLocSize == 0){
                        missingList.add("Preferred Date Location");
                    }

                    AlertDialog.Builder alert = new AlertDialog.Builder(ProfileCreationActivity.this);
                    alert.setTitle("Invalid Inputs");
                    String message = "";
                    for (int i = 0; i < missingList.size(); i++){
                        if (i>0){
                            message = message + ", " + missingList.get(i);
                        }
                        else{
                            message = missingList.get(i);
                        }
                    }
                    alert.setMessage(message);
                    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    alert.show();
                }


            }
        });


    }

    public void InitDropdown(){
        //Gender
        Spinner Genderdropdown = findViewById(R.id.GenderDropdown);
        String[] gender = new String[]{"Male", "Female"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, gender);
        Genderdropdown.setAdapter(genderAdapter);

        //Relationship Preference
        Spinner Relationsshipdropdown = findViewById(R.id.RelationshipPrefDropdown);
        String[] relationshipPref = new String[]{"Serious", "Casual", "Friends"};
        ArrayAdapter<String> relationshipPrefAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, relationshipPref);
        Relationsshipdropdown.setAdapter(relationshipPrefAdapter);

        // Gender Preference
        Spinner GenderPrefdropdown = findViewById(R.id.GenderPrefDropdown);
        String[] genderPref = new String[]{"Male", "Female", "Others"};
        ArrayAdapter<String> genderPrefAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, genderPref);
        GenderPrefdropdown.setAdapter(genderPrefAdapter);


        //Preferred Locations
        String[] locations = {"Restaurant", "Arcade", "Cafe", "Amusement Park", "Shopping", "Mall", "Hotel", "Home"};
        TextView LocationPref = findViewById(R.id.DateLocDropdown);
        boolean[] selectedLocation = new boolean[locations.length];
        ArrayList<Integer> checkedList = new ArrayList<>();

        LocationPref.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileCreationActivity.this);
                builder.setTitle("Select Preferred Dating Location");
                builder.setCancelable(false);    //set dialog non cancelable

                builder.setMultiChoiceItems(locations, selectedLocation, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        if (b){
                            //when checkbox is selected, add position into list
                            checkedList.add(i);
                            Collections.sort(checkedList);
                        }
                        else{
                            //when checkbox is not selected, remove position from list
                            checkedList.remove(Integer.valueOf(i));
                        }
                    }
                });

                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        StringBuilder stringBuilder = new StringBuilder();
                        dateLocList.removeAll(dateLocList);
                        for (int j = 0; j < checkedList.size(); j++){
                            stringBuilder.append(locations[checkedList.get(j)]);
                            dateLocList.add(locations[checkedList.get(j)]);
                            if (j != checkedList.size() - 1){
                                stringBuilder.append(", ");
                            }
                        }

                        LocationPref.setText(stringBuilder.toString());
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                builder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        for (int j = 0; j < selectedLocation.length; j++){
                            selectedLocation[j] = false;
                            checkedList.clear();
                            dateLocList.removeAll(dateLocList);
                            LocationPref.setText("Date Location");
                        }
                    }
                });
                builder.show();
            }
        });
    }

    //Init recyclerview
    public void InitRecyclerView(){
        RecyclerView rv = findViewById(R.id.interestRecyclerView);
        ProfileCreationAdapter adapter = new ProfileCreationAdapter(ProfileCreationActivity.this, data);
        LinearLayoutManager layout = new LinearLayoutManager(this);
        layout.setOrientation(LinearLayoutManager.HORIZONTAL);

        rv.setAdapter(adapter);
        rv.setLayoutManager(layout);
    }

    // Check for incomplete fields
    public Boolean IncompleteActions(){

        //username
        EditText inputUsername = findViewById(R.id.InputName);
        String username = inputUsername.getText().toString();

        //Gender
        Spinner GenderDropdown = findViewById(R.id.GenderDropdown);
        String gender = GenderDropdown.getSelectedItem().toString();

        //Age
        EditText inputAge = findViewById(R.id.AgeInput);
        String age = inputAge.getText().toString();

        //About me
        EditText inputAboutMe = findViewById(R.id.AboutMeInput);
        String aboutMe = inputAboutMe.getText().toString();

        //Interest
        int interestSize = data.size();

        //Relationship Preference
        Spinner RelationshipPrefDropdown = findViewById(R.id.RelationshipPrefDropdown);
        String RelationshipPref = RelationshipPrefDropdown.getSelectedItem().toString();

        // Gender Preference
        Spinner GenderPrefdropdown = findViewById(R.id.GenderPrefDropdown);
        String GenderPref = GenderPrefdropdown.getSelectedItem().toString();

        // Date Location
        int dateLocSize = dateLocList.size();

        // Checking if there are any missing inputs
        if (username.equals("") || gender.equals("") || age.equals("") || aboutMe.equals("") || interestSize == 0 || RelationshipPref.equals("") || GenderPref.equals("") || dateLocSize == 0){
            return false;
        }
        else{
            int checkAge = Integer.parseInt(age);
            if(checkAge < 18){
                return false;
            }
        }

        return true;
    }
}
