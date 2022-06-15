package sg.edu.np.ignight;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;

public class ProfileCreationActivity extends AppCompatActivity {

    ArrayList<String> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_creation);

        InitDropdown();

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


    }

    public void InitDropdown(){
        //Gender
        Spinner Genderdropdown = findViewById(R.id.GenderDropdown);
        String[] gender = new String[]{"Male", "Female"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, gender);
        Genderdropdown.setAdapter(genderAdapter);

        //Age
        Spinner Agedropdown = findViewById(R.id.AgeDropdown);
        String[] age = new String[]{"21-30", "31-40", "41-50", "51-60", "61-70", "71-80", "81 & above"};
        ArrayAdapter<String> ageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, age);
        Agedropdown.setAdapter(ageAdapter);

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

                        for (int j = 0; j < checkedList.size(); j++){
                            stringBuilder.append(locations[checkedList.get(j)]);
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
}
