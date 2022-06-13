package sg.edu.np.ignight;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

public class ProfileCreation extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_creation);
        InitDropdown();

        String[] locations = {"Restaurant", "Arcade", "Cafe", "Amusement Park", "Shopping", "Mall", "Hotel", "Home"};
        TextView LocationPref = findViewById(R.id.DateLocDropdown);
        boolean[] selectedLocation = new boolean[locations.length];
        ArrayList<Integer> checkedList = new ArrayList<>();

        LocationPref.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileCreation.this);
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
                            LocationPref.setText("");
                        }
                    }
                });
                builder.show();
            }
        });


    }

    public void InitDropdown(){
        Spinner Genderdropdown = findViewById(R.id.GenderDropdown);
        String[] gender = new String[]{"Male", "Female"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, gender);
        Genderdropdown.setAdapter(genderAdapter);

        Spinner Agedropdown = findViewById(R.id.AgeDropdown);
        String[] age = new String[]{"21-30", "31-40", "41-50", "51-60", "61-70", "71-80", "81 & above"};
        ArrayAdapter<String> ageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, age);
        Agedropdown.setAdapter(ageAdapter);

        Spinner Relationsshipdropdown = findViewById(R.id.RelationshipPrefDropdown);
        String[] relationshipPref = new String[]{"Serious", "Casual", "Friends"};
        ArrayAdapter<String> relationshipPrefAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, relationshipPref);
        Relationsshipdropdown.setAdapter(relationshipPrefAdapter);

        Spinner GenderPrefdropdown = findViewById(R.id.GenderPrefDropdown);
        String[] genderPref = new String[]{"Male", "Female", "Others"};
        ArrayAdapter<String> genderPrefAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, genderPref);
        GenderPrefdropdown.setAdapter(genderPrefAdapter);
    }
}