package sg.edu.np.ignight;

import static android.content.ContentValues.TAG;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import sg.edu.np.ignight.ProfileCreation.ProfileCreationAdapter;

public class ProfileCreationActivity extends AppCompatActivity {

    private ArrayList<String> interestList = new ArrayList<>();
    private ArrayList<String> dateLocList = new ArrayList<>();
    private ArrayList<String> invalidList = new ArrayList<>();

    private ImageButton backButton;
    private Button interestButton, saveChangesButton, uploadButton;
    private TextView aboutMeTextview, locationPref;
    private EditText aboutMeInput, nameInput, ageInput;
    private Spinner genderDropdown, genderPrefDropdown, relationshipPrefDropdown;
    private RecyclerView interestRV;

    private boolean fromLogin;
    private boolean fromMenu;

    private final int Gallery_Request = 1;
    private ImageView imgGallery;
    private Uri imageUri;

    private FirebaseStorage storage;
    private StorageReference storageReference;

    // Get the current user
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    //get the current user's UID
    String Uid = user.getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_creation);

        //Get the buttons, Spinners, TextInput and TextView
        backButton = findViewById(R.id.profileCreationBackButton);
        interestButton = findViewById(R.id.InterestButton);
        saveChangesButton = findViewById(R.id.SaveChanges);
        aboutMeTextview = findViewById(R.id.AboutMeTextView);
        locationPref = findViewById(R.id.DateLocDropdown);
        aboutMeInput = findViewById(R.id.AboutMeInput);
        nameInput = findViewById(R.id.InputName);
        ageInput = findViewById(R.id.AgeInput);
        genderDropdown = findViewById(R.id.GenderDropdown);
        genderPrefDropdown = findViewById(R.id.GenderPrefDropdown);
        relationshipPrefDropdown = findViewById(R.id.RelationshipPrefDropdown);
        imgGallery = findViewById(R.id.ProfileCreationImage);
        uploadButton = findViewById(R.id.ProfilePicUpload);
        interestRV = findViewById(R.id.interestRecyclerView);

        // Initialise the input for users
        InitInputs();


        Intent receiveIntent = getIntent();
        // receive the intent from LoginActivity to see if profile has been created
        fromLogin = receiveIntent.getBooleanExtra("fromLogin", false);
        // receive intent from MainMenuActivity to see if user click to ProfileCreationActivity from the MainMenu
        fromMenu = receiveIntent.getBooleanExtra("ProfileCreated", false);

        // Back button to go back to Login Activity when clicked
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fromLogin) {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    finish();
                }
            }
        });


        // Upload the button from their gallery onto the app
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePicture();
            }
        });

        // Saving to Firebase database
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/");
        // getting the child user
        DatabaseReference myRef = database.getReference("user");
        // Saving to Firebase storage
        storage = FirebaseStorage.getInstance("gs://madignight.appspot.com");

        // if the intent from Main Menu is true, it means that the user clicked on edit profile
        // Load all the data from firebase that were keyed in from profile creation, into the input fields
        if (fromMenu) {
            // getting the child (user's UID)
            myRef.child(Uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Get the value of username
                    String existUsername = snapshot.child("username").getValue(String.class);
                    // Get the value of the user's gender
                    String existGender = snapshot.child("Gender").getValue(String.class);
                    // Get the user's age
                    Integer existAge = snapshot.child("Age").getValue(Integer.class);
                    // Get the user's About Me
                    String existAboutMe = snapshot.child("About Me").getValue(String.class);
                    // Get the user's relationship preference
                    String existRelationshipPref = snapshot.child("Relationship Preference").getValue(String.class);
                    // Get the user's Gender preference
                    String existGenderPref = snapshot.child("Gender Preference").getValue(String.class);
                    // Get the user's profile picture url
                    String existProfilePic = snapshot.child("profileUrl").getValue(String.class);

                    // Get the total amount of interest the user had inputted
                    Integer totalInterest = (int) snapshot.child("Interest").getChildrenCount();
                    // Getting the specific interests
                    for (int i = 1; i <= totalInterest; i++) {
                        String existingInterest = snapshot.child("Interest").child("Interest" + i).getValue(String.class);
                        // Add into interestList
                        interestList.add(existingInterest);
                        // Load recycler view to show it in View Holder
                        InitRecyclerView();
                    }

                    // Get the total amount of preferred dating locations
                    Integer totalDateLoc = (int) snapshot.child("Date Location").getChildrenCount();
                    // Getting the specific dating location
                    for (int i = 1; i <= totalDateLoc; i++) {
                        String existingDateLoc = snapshot.child("Date Location").child("Date Location" + i).getValue(String.class);
                        // Add into dateLocList
                        dateLocList.add(existingDateLoc);
                    }

                    // Set the text of input to the user's username
                    nameInput.setText(existUsername);
                    // Set age input to user's age
                    ageInput.setText(existAge.toString());
                    // Set About Me input to user's About Me
                    aboutMeInput.setText(existAboutMe);
                    // Get the length of the text
                    int length = aboutMeInput.getText().length();
                    // Set the text size according to length of text keyed in before
                    int textSize = 23;
                    if (length >= 160){
                        textSize = 15;
                    }
                    else if(length >= 140){
                        textSize = 16;
                    }
                    else if(length >= 120){
                        textSize = 17;
                    }
                    else if(length >= 110){
                        textSize = 18;
                    }
                    else if(length >= 100){
                        textSize = 19;
                    }
                    aboutMeInput.setTextSize(textSize);


                    //  Set the gender dropdown value to user's selected gender
                    selectSpinnerValue(genderDropdown, existGender);
                    //  Set the relationship preference dropdown value to user's selected relationship preference
                    selectSpinnerValue(relationshipPrefDropdown, existRelationshipPref);
                    //  Set the gender preference dropdown value to user's selected preference
                    selectSpinnerValue(genderPrefDropdown, existGenderPref);


                    StringBuilder stringBuilder = new StringBuilder();
                    // Appending the specific dating locations to a string
                    for (int i = 0; i < totalDateLoc; i++) {
                        stringBuilder.append(dateLocList.get(i));
                        // if i is not equals to the total number of total Date Locations - 1
                        if (i != totalDateLoc - 1) {
                            stringBuilder.append(", ");
                        }
                    }
                    // Set the text onto the Date Location drop down
                    locationPref.setText(stringBuilder.toString());

                    // set profile picture
                    Glide.with(getApplicationContext()).load(existProfilePic).placeholder(R.drawable.ic_baseline_image_24).into(imgGallery);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        // When save changes button is clicked
        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the UID of the user in firebase database
                DatabaseReference nested = myRef.child(FirebaseAuth.getInstance().getUid());

                // Checking for missing inputs
                // If no invalid fields
                if (validFields()) {
                    // Insert into database
                    // Username
                    String username = nameInput.getText().toString();
                    nested.child("username").setValue(username);

                    //Gender
                    String gender = genderDropdown.getSelectedItem().toString();
                    nested.child("Gender").setValue(gender);

                    // Age
                    int age = Integer.parseInt(ageInput.getText().toString());
                    nested.child("Age").setValue(age);

                    // About me
                    String aboutMe = aboutMeInput.getText().toString();
                    nested.child("About Me").setValue(aboutMe);

                    // Interest
                    int interestSize = interestList.size();
                    DatabaseReference nestedInterest = nested.child("Interest");
                    // new Map to store interests and update database
                    Map interestMap = new HashMap<>();
                    for (int i = 0; i < interests.length; i++) {
                        if (i < interestSize) {
                            interestMap.put("Interest" + (i + 1), interestList.get(i));
                        }
                        else {
                            interestMap.put("Interest" + (i + 1), null);
                        }
                    }
                    nestedInterest.updateChildren(interestMap);

                    // Relationship Preference
                    String RelationshipPref = relationshipPrefDropdown.getSelectedItem().toString();
                    nested.child("Relationship Preference").setValue(RelationshipPref);

                    // Gender Preference
                    String GenderPref = genderPrefDropdown.getSelectedItem().toString();
                    nested.child("Gender Preference").setValue(GenderPref);

                    // Date Location
                    int dateLocSize = dateLocList.size();
                    DatabaseReference nestedDateLoc = nested.child("Date Location");
                    // new Map to store interests and update database
                    Map dateLocMap = new HashMap<>();
                    for (int i = 0; i < locations.length; i++) {
                        if (i < dateLocSize) {
                            dateLocMap.put("Date Location" + (i + 1), dateLocList.get(i));
                        }
                        else {
                            dateLocMap.put("Date Location" + (i + 1), null);
                        }
                    }
                    nestedDateLoc.updateChildren(dateLocMap);

                    // Profile Picture
                    if(imageUri != null){
                        uploadPicture(nested);
                    }

                    // set profileCreated to true
                    nested.child("profileCreated").setValue(true);

                    // if user just created a new profile, it will bring user back to Main Menu
                    if (fromLogin) {
                        Intent intent = new Intent(getApplicationContext(), MainMenuActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        finish();
                    }
                    // if there are invalid  fields
                } else {
                    // show an alert dialog
                    AlertDialog.Builder alert = new AlertDialog.Builder(ProfileCreationActivity.this);
                    alert.setTitle("Invalid Inputs");
                    String message = "";
                    // Put the location of the invalid fields
                    for (int i = 0; i < invalidList.size(); i++) {
                        if (i > 0) {
                            message = message + ", " + invalidList.get(i);
                        } else {
                            message = invalidList.get(i);
                        }
                    }
                    alert.setMessage(message);
                    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    alert.show();

                    invalidList.clear();
                }
            }
        });
    }

    //Preferred Locations
    private String[] locations = {"Mall", "Restaurant", "Arcade", "Cafe", "Theme Park", "Park"};
    private String[] interests = {"Running", "Cooking", "Gaming", "Swimming", "Reading", "Shopping", "Others"};
    // method to initialise Inputs
    private void InitInputs() {
        //Gender (Spinner, dropdown)
        String[] gender = new String[]{"Male", "Female"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, gender);
        genderDropdown.setAdapter(genderAdapter);

        //Relationship Preference (Spinner, dropdown)
        String[] relationshipPref = new String[]{"Serious", "Casual", "Friends"};
        ArrayAdapter<String> relationshipPrefAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, relationshipPref);
        relationshipPrefDropdown.setAdapter(relationshipPrefAdapter);

        // Gender Preference (Spinner, dropdown)
        String[] genderPref = new String[]{"Male", "Female"};
        ArrayAdapter<String> genderPrefAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, genderPref);
        genderPrefDropdown.setAdapter(genderPrefAdapter);

        // Interest (drop down button --> display output in a view holder)
        boolean[] selectedInterest = new boolean[interests.length];
        ArrayList<Integer> interestCheckedList = new ArrayList<>();

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
                            interestCheckedList.add(i);
                            Collections.sort(interestCheckedList);
                        }
                        else{
                            //when checkbox is not selected, remove position from list
                            interestCheckedList.remove(Integer.valueOf(i));
                        }
                    }
                });

                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // remove existing interests from interest List
                        interestList.removeAll(interestList);
                        // Add all the interests selected in the check box into interestList
                        for (int j = 0; j < interestCheckedList.size(); j++){
                            interestList.add(interests[interestCheckedList.get(j)]);
                        }
                        // Initialise recycler view to display interests selected in view holder
                        InitRecyclerView();
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                // When clear all is selected
                builder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Remove all interests from the different list
                        for (int j = 0; j < selectedInterest.length; j++){
                            selectedInterest[j] = false;
                            interestCheckedList.clear();
                            interestList.removeAll(interestList);
                        }
                        // Initialise recycler view again to display the changes that user had cleared all interest selected
                        InitRecyclerView();
                    }
                });
                builder.show();
            }
        });

        // About Me
        aboutMeInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            // Change the size of the text when the length of text increases
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                aboutMeTextview.setText(charSequence.toString());
                float size = aboutMeTextview.getTextSize();
                size = size / 3;
                aboutMeInput.setTextSize((int) size);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        boolean[] selectedLocation = new boolean[locations.length];
        ArrayList<Integer> locationCheckedList = new ArrayList<>();

        locationPref.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileCreationActivity.this);
                builder.setTitle("Select Preferred Dating Location");
                builder.setCancelable(false);    //set dialog non cancelable

                builder.setMultiChoiceItems(locations, selectedLocation, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        if (b) {
                            //when checkbox is selected, add position into list
                            locationCheckedList.add(i);
                            Collections.sort(locationCheckedList);
                        } else {
                            //when checkbox is not selected, remove position from list
                            locationCheckedList.remove(Integer.valueOf(i));
                        }
                    }
                });

                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        StringBuilder stringBuilder = new StringBuilder();
                        // Remove existing date Location list
                        dateLocList.removeAll(dateLocList);
                        for (int j = 0; j < locationCheckedList.size(); j++) {
                            // Append location to string builder
                            stringBuilder.append(locations[locationCheckedList.get(j)]);
                            // Add specific date location to dateLocList
                            dateLocList.add(locations[locationCheckedList.get(j)]);
                            if (j != locationCheckedList.size() - 1) {
                                stringBuilder.append(", ");
                            }
                        }
                        // Display the locations selected on the check box on the drop down button
                        locationPref.setText(stringBuilder.toString());
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
                        // Remove everything from lists when clear all is selected
                        for (int j = 0; j < selectedLocation.length; j++) {
                            selectedLocation[j] = false;
                            locationCheckedList.clear();
                            dateLocList.removeAll(dateLocList);
                            locationPref.setText("Date Location");
                        }
                    }
                });
                builder.show();
            }
        });
    }

    //Init recyclerview
    private void InitRecyclerView() {
        ProfileCreationAdapter adapter = new ProfileCreationAdapter(ProfileCreationActivity.this, interestList);
        LinearLayoutManager layout = new LinearLayoutManager(this);
        // change the layout to horizontal
        layout.setOrientation(LinearLayoutManager.HORIZONTAL);

        interestRV.setAdapter(adapter);
        interestRV.setLayoutManager(layout);
    }

    // Check for incomplete fields
    private Boolean validFields() {

        //username
        String username = nameInput.getText().toString();

        //Gender
        String gender = genderDropdown.getSelectedItem().toString();

        //Age
        String age = ageInput.getText().toString();

        //About me
        String aboutMe = aboutMeInput.getText().toString();

        //Interest
        int interestSize = interestList.size();

        //Relationship Preference
        String RelationshipPref = relationshipPrefDropdown.getSelectedItem().toString();

        // Gender Preference
        String GenderPref = genderPrefDropdown.getSelectedItem().toString();

        // Date Location
        int dateLocSize = dateLocList.size();

        // Checking if there are any missing inputs

        int invalidFieldCount = 0;

        if (username.equals("")) {
            invalidList.add("Username");
            invalidFieldCount++;
        }
        if (gender.equals("")) {
            invalidList.add("Gender");
            invalidFieldCount++;
        }
        if (age.equals("")) {
            invalidList.add("Age");
            invalidFieldCount++;
        } else if (Integer.parseInt(age) < 18) {
            invalidList.add("Invalid Age");
            invalidFieldCount++;
        }
        if (aboutMe.equals("")) {
            invalidList.add("About Me");
            invalidFieldCount++;
        }
        if (interestSize == 0) {
            invalidList.add("Interest");
            invalidFieldCount++;
        }
        if (RelationshipPref.equals("")) {
            invalidList.add("Relationship Preference");
            invalidFieldCount++;
        }
        if (GenderPref.equals("")) {
            invalidList.add("Gender Preference");
            invalidFieldCount++;
        }
        if (dateLocSize == 0) {
            invalidList.add("Date Location Preference");
            invalidFieldCount++;
        }
        if (imageUri == null) {
            if (fromLogin){
                invalidList.add("No Profile Picture");
                invalidFieldCount++;
            }
        }

        return (invalidFieldCount == 0);
    }

    // Select the chosen spinner value to display when loading back user's input
    private void selectSpinnerValue(Spinner spinner, String myString) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(myString)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    // allowing user's to choose different pictures from their gallery
    public void choosePicture() {
        Intent intent = new Intent();
        // Upload any images type
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, Gallery_Request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // When result code is ok
        if (resultCode == RESULT_OK) {
            // if everything processed successfully
            if (requestCode == Gallery_Request) {
                // get image Uri
                imageUri = data.getData();

                InputStream inputStream;

                try {
                    inputStream = getContentResolver().openInputStream(imageUri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    //show message to user indicating that the image is unavailable
                    Toast.makeText(this, "Unable to open image", Toast.LENGTH_SHORT).show();
                }

                //for Gallery
                imgGallery.setImageURI(imageUri);
            }
        }
    }

    // upload the new profile picture and removes previous records in the storage
    private void uploadPicture(DatabaseReference userDB) {
        final String randomKey = UUID.randomUUID().toString();
        storage = FirebaseStorage.getInstance("gs://madignight.appspot.com");
        // putting the images to the specific folders, their own UID
        // the images are labelled with a random key
        storageReference = storage.getReference().child("profilePicture/" + Uid);
        storageReference.child(randomKey).putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageReference.child(randomKey).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // save the profile picture url to database
                                userDB.child("profileUrl").setValue(uri.toString());

                                // Display image successfully uploaded with Toast
                                Toast.makeText(getApplicationContext(), "Image uploaded", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Display image failed to upload with Toast
                        Toast.makeText(getApplicationContext(), "Failed to Upload", Toast.LENGTH_SHORT).show();
                    }
                });

        storageReference.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                for (StorageReference childResult : listResult.getItems()) {
                    if (!childResult.getName().equals(randomKey)) {
                        childResult.delete();
                    }
                }
            }
        });
    }
}
