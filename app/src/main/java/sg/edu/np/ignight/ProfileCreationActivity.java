package sg.edu.np.ignight;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import sg.edu.np.ignight.ProfileCreation.ProfileCreationAdapter;

public class ProfileCreationActivity extends AppCompatActivity {

    private ArrayList<String> interestList = new ArrayList<>();
    private ArrayList<String> dateLocList = new ArrayList<>();
    private ArrayList<String> invalidList = new ArrayList<>();

    private ImageButton backButton;
    private Button interestButton, saveChangesButton;
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

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String Uid = user.getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_creation);

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
        interestRV = findViewById(R.id.interestRecyclerView);


        InitInputs();


        Intent receiveIntent = getIntent();
        fromLogin = receiveIntent.getBooleanExtra("fromLogin", false);
        fromMenu = receiveIntent.getBooleanExtra("ProfileCreated", false);

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

        imgGallery = findViewById(R.id.ProfileCreationImage);
        Button uploadButton = findViewById(R.id.ProfilePicUpload);

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePicture();
            }
        });

        // Saving to Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference myRef = database.getReference("user");

        storage = FirebaseStorage.getInstance("gs://madignight.appspot.com");


        if (fromMenu) {
            myRef.child(Uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String existUsername = snapshot.child("username").getValue(String.class);
                    String existGender = snapshot.child("Gender").getValue(String.class);
                    Integer existAge = snapshot.child("Age").getValue(Integer.class);
                    String existAboutMe = snapshot.child("About Me").getValue(String.class);
                    String existRelationshipPref = snapshot.child("Relationship Preference").getValue(String.class);
                    String existGenderPref = snapshot.child("Gender Preference").getValue(String.class);
                    String existProfilePic = snapshot.child("Profile Picture").getValue(String.class);

                    Integer totalInterest = (int) snapshot.child("Interest").getChildrenCount();
                    for (int i = 1; i <= totalInterest; i++) {
                        String existingInterest = snapshot.child("Interest").child("Interest" + i).getValue(String.class);
                        interestList.add(existingInterest);
                        InitRecyclerView();
                    }

                    Integer totalDateLoc = (int) snapshot.child("Date Location").getChildrenCount();
                    for (int i = 1; i <= totalDateLoc; i++) {
                        String existingDateLoc = snapshot.child("Date Location").child("Date Location" + i).getValue(String.class);
                        dateLocList.add(existingDateLoc);
                    }

                    nameInput.setText(existUsername);
                    ageInput.setText(existAge.toString());

                    aboutMeInput.setText(existAboutMe);

                    selectSpinnerValue(genderDropdown, existGender);
                    selectSpinnerValue(relationshipPrefDropdown, existRelationshipPref);
                    selectSpinnerValue(genderPrefDropdown, existGenderPref);

                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < totalDateLoc; i++) {
                        stringBuilder.append(dateLocList.get(i));
                        if (totalDateLoc > 1) {
                            stringBuilder.append(", ");
                        }
                    }
                    locationPref.setText(stringBuilder.toString());

                    storageReference = storage.getReference().child("profilePicture/" + Uid + "/" + existProfilePic);

                    try {
                        final File localFile = File.createTempFile(existProfilePic, existProfilePic);
                        storageReference.getFile(localFile)
                                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                        Toast.makeText(ProfileCreationActivity.this, "Picture Retrieved", Toast.LENGTH_SHORT).show();
                                        Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                        imgGallery.setImageBitmap(bitmap);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(ProfileCreationActivity.this, "Error Occurred", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatabaseReference nested = myRef.child(FirebaseAuth.getInstance().getUid());

                // Checking for missing inputs
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
                    for (int i = 0; i < interestSize; i++) {
                        String interest = interestList.get(i);
                        nestedInterest.child("Interest" + (i + 1)).setValue(interest);
                    }

                    // Relationship Preference
                    String RelationshipPref = relationshipPrefDropdown.getSelectedItem().toString();
                    nested.child("Relationship Preference").setValue(RelationshipPref);

                    // Gender Preference
                    String GenderPref = genderPrefDropdown.getSelectedItem().toString();
                    nested.child("Gender Preference").setValue(GenderPref);

                    // Date Location
                    int dateLocSize = dateLocList.size();
                    DatabaseReference nestedDateLoc = nested.child("Date Location");
                    for (int i = 0; i < dateLocSize; i++) {
                        String dateLoc = dateLocList.get(i);
                        nestedDateLoc.child("Date Location" + (i + 1)).setValue(dateLoc);
                    }

                    // Profile Picture
                    String imageKey = uploadPicture();
                    nested.child("Profile Picture").setValue(imageKey);

                    // set profileCreated to true
                    nested.child("profileCreated").setValue(true);

                    if (fromLogin) {
                        Intent intent = new Intent(getApplicationContext(), MainMenuActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        finish();
                    }
                } else {
                    AlertDialog.Builder alert = new AlertDialog.Builder(ProfileCreationActivity.this);
                    alert.setTitle("Invalid Inputs");
                    String message = "";
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

    private void InitInputs() {
        //Gender
        String[] gender = new String[]{"Male", "Female"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, gender);
        genderDropdown.setAdapter(genderAdapter);

        //Relationship Preference
        String[] relationshipPref = new String[]{"Serious", "Casual", "Friends"};
        ArrayAdapter<String> relationshipPrefAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, relationshipPref);
        relationshipPrefDropdown.setAdapter(relationshipPrefAdapter);

        // Gender Preference
        String[] genderPref = new String[]{"Male", "Female", "Others"};
        ArrayAdapter<String> genderPrefAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, genderPref);
        genderPrefDropdown.setAdapter(genderPrefAdapter);

        // Interest
        String[] interests = {"Running", "Cooking", "Gaming", "Swimming", "Reading", "Shopping", "Others"};
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
                        interestList.removeAll(interestList);
                        for (int j = 0; j < interestCheckedList.size(); j++){
                            interestList.add(interests[interestCheckedList.get(j)]);
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
                            interestCheckedList.clear();
                            interestList.removeAll(interestList);
                        }
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


        //Preferred Locations
        String[] locations = {"Restaurant", "Arcade", "Cafe", "Amusement Park", "Shopping", "Mall", "Hotel", "Home"};
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
                        dateLocList.removeAll(dateLocList);
                        for (int j = 0; j < locationCheckedList.size(); j++) {
                            stringBuilder.append(locations[locationCheckedList.get(j)]);
                            dateLocList.add(locations[locationCheckedList.get(j)]);
                            if (j != locationCheckedList.size() - 1) {
                                stringBuilder.append(", ");
                            }
                        }

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
            invalidList.add("No Profile Picture");
        }

        return (invalidFieldCount == 0);
    }

    private void selectSpinnerValue(Spinner spinner, String myString) {
        int index = 0;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(myString)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    public void choosePicture() {
        Intent intent = new Intent();
        intent.setType("image/png");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, Gallery_Request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            // if everything processed successfully
            if (requestCode == Gallery_Request) {
                // get Uri
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

    public String uploadPicture() {

        final String randomKey = UUID.randomUUID().toString();
        storage = FirebaseStorage.getInstance("gs://madignight.appspot.com");
        storageReference = storage.getReference().child("profilePicture/" + Uid).child(randomKey);

        storageReference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        Toast.makeText(getApplicationContext(), "Image uploaded", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed to Upload", Toast.LENGTH_SHORT).show();
                    }
                });
        return randomKey;
    }
}
