package sg.edu.np.ignight;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import sg.edu.np.ignight.Models.UserAccount;

public class RegistrationActivity extends AppCompatActivity{

    private EditText numberInput, usernameInput, passwordInput, confirmPasswordInput;
    private Button createButton;
    private TextView textView2;
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Instantiate database
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference myRef = database.getReference("user");

        // Initialize some fields
        numberInput = (EditText) findViewById(R.id.editTextPhone);
        usernameInput = (EditText) findViewById(R.id.newUsernameInput);
        passwordInput= (EditText) findViewById(R.id.editTextTextPassword);
        confirmPasswordInput = (EditText) findViewById(R.id.editTextTextPassword2);
        createButton = (Button) findViewById(R.id.createButton);
        textView2 = (TextView) findViewById(R.id.textView2);

        // Set inputs to string
        String newNumber = numberInput.getText().toString().trim();
        String newUsername = usernameInput.getText().toString().trim();
        String newPassword = passwordInput.getText().toString().trim();
        String newConfirmPassword = confirmPasswordInput.getText().toString().trim();

        // Reading the user account data inside the database

        // Use cases:
        // 1. Check if the account has been created yet (just check if number has been used before)
        // 2. Check if the username created is unique
        // 3. Check if the password and password confirmation match
        // 4. Register new account if account does not exist in database and cases 2. and 3. are not caught
        // 5. Check if any of the fields are empty. If they are, present an error.
        // 6. Validate the input format for the inputs. If they are invalid, present an error.
        // Please add in/edit these use cases if you have any ideas

        // If there are any errors, the font colour of textView2 turns red and shows the error message

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        while (true){
                             boolean inputCheck = inputValidationChecks();
                             if (inputCheck){ // Breaks from inputVCheck once user enters "correct" input
                                 break;
                             }
                        }

                        if(!checkForUnusedNumber(newNumber, snapshot)){
                            numberInput.setError("An existing account already uses this number.");
                            numberInput.requestFocus();
                            return;
                        }

                        if(!checkForUnusedUsername(newUsername, snapshot)){
                            usernameInput.setError("An existing account already uses this username.");
                            usernameInput.requestFocus();
                            return;
                        }

                        if(checkForUnusedNumber(newNumber, snapshot) &&
                        checkForUnusedUsername(newUsername, snapshot)){
                            // if number and username are new and unused
                            registerUser(); // uploads user's info to database to register them

                            // directs users to the login menu
                            Intent toLoginScreen = new Intent(RegistrationActivity.this, LoginActivity.class);
                            startActivity(toLoginScreen);
                            return;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(RegistrationActivity.this,
                                "An error occurred. Please try again.",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

    }


    private boolean checkForUnusedNumber(String number, DataSnapshot dataSnapshot){
        database = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app");
        myRef = database.getReference("user");

        UserAccount userAccount = new UserAccount();

        // Loop through data snapshot and see if the username exists
        for (DataSnapshot ds: dataSnapshot.getChildren()){
            if(userAccount.getPhone().equals(number)){
                return true; // returns true if the phone number exists in the database
            }
        }

        return false; // return false if the phone number does not exist in the database
    }

    private boolean checkForUnusedUsername(String username, DataSnapshot dataSnapshot){
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference myRef = database.getReference("user");

        UserAccount userAccount = new UserAccount();

        for (DataSnapshot ds: dataSnapshot.getChildren()){
            if(expandUsername(userAccount.getUsername()).equals(username)){

            }
        }

        return false;
    }

    private boolean inputValidationChecks(){ // Returns true if all validation checks are okay, & vice versa
        String newNumber = numberInput.getText().toString().trim();
        String newUsername = usernameInput.getText().toString().trim();
        String newPassword = passwordInput.getText().toString().trim();
        String newConfirmPassword = confirmPasswordInput.getText().toString().trim();

        // Following control structures for use case 5.
        if(newNumber.isEmpty()){
            numberInput.setError("No empty fields are allowed!");
            numberInput.requestFocus();
            return false;
        }

        if(newUsername.isEmpty()){
            usernameInput.setError("No empty fields are allowed!");
            usernameInput.requestFocus();
            return false;
        }

        if(newPassword.isEmpty()){
            passwordInput.setError("No empty fields are allowed!");
            passwordInput.requestFocus();
            return false;
        }

        if(newConfirmPassword.isEmpty()){
            confirmPasswordInput.setError("No empty fields are allowed!");
            confirmPasswordInput.requestFocus();
            return false;
        }

        // Following control structures for use case 6.
        if(!Patterns.PHONE.matcher(newNumber).matches()){
            numberInput.setError("Provide a valid phone number!");
            numberInput.requestFocus();
            return false;
        }

        if(newPassword.length() < 5){
            passwordInput.setError("Password should be 5 characters or longer!");
            passwordInput.requestFocus();
            return false;
        }

        // Control structure for use case 3.
        if(!newPassword.equals(newConfirmPassword)){
            confirmPasswordInput.setError("Confirmation password does not match!");
            confirmPasswordInput.requestFocus();
            return false;
        }

        return true;
    }

    private void registerUser() {

    }

    // Utilities for string manipulation. Feel free to use these for other things
    public static String expandUsername(String username){
        return username.replace(".", " ");
    }

    public static String condenseUsername(String username){
        return username.replace(" ", ",");
    }
}