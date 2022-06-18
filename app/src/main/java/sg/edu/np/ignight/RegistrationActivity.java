package sg.edu.np.ignight;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.translation.ViewTranslationCallback;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class RegistrationActivity extends AppCompatActivity{

    private EditText numberInput, usernameInput, passwordInput, confirmPasswordInput, emailInput;
    private ProgressBar progressBar2;
    private TextView textView2;
    private FirebaseDatabase database;
    private Task<Void> myRef;
    private FirebaseAuth mAuth;
    private Button createButton;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Init everything
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference myRef = database.getReference("user");
        mAuth = FirebaseAuth.getInstance();

        numberInput = findViewById(R.id.editTextPhone);
        usernameInput = findViewById(R.id.newUsernameInput);
        passwordInput = findViewById(R.id.editTextTextPassword);
        confirmPasswordInput = findViewById(R.id.editTextTextPassword2);
        textView2 = findViewById(R.id.textView2);
        emailInput = findViewById(R.id.editEmailAddress);
        progressBar2 = findViewById(R.id.progressBar2);

        createButton = findViewById(R.id.createButton);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

        backButton = findViewById(R.id.BackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toLogin = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(toLogin);
            }
        });
    }

    // PERFORM SEPARATION

    // -- NOT WORKING YET DUE TO DATABASE RULES --

    @SuppressLint("SetTextI18n")
    private void registerUser() {
        // Conversion to string and trimming of input to get "correct" ones
        String newEmail = emailInput.getText().toString().trim();
        String newNumber = numberInput.getText().toString().trim();
        String newUsername = usernameInput.getText().toString().trim();
        String newPassword = passwordInput.getText().toString().trim();
        String newConfirmPassword = confirmPasswordInput.getText().toString().trim();


        // Control structure for input validation
        if(newNumber.isEmpty()){
            numberInput.setError("No empty fields are allowed!");
            numberInput.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()){
            emailInput.setError("No empty fields are allowed!");
            emailInput.requestFocus();
            return;
        }

        if(newUsername.isEmpty()){
            usernameInput.setError("No empty fields are allowed!");
            usernameInput.requestFocus();
            return;
        }

        if(newPassword.isEmpty()){
            passwordInput.setError("No empty fields are allowed!");
            passwordInput.requestFocus();
            return;
        }

        if(newConfirmPassword.isEmpty()){
            confirmPasswordInput.setError("No empty fields are allowed!");
            confirmPasswordInput.requestFocus();
            return;
        }

        // Following control structures for use case 6.
        if(!Patterns.PHONE.matcher(newNumber).matches()){
            numberInput.setError("Provide a valid phone number!");
            numberInput.requestFocus();
            return;
        }

        if(newPassword.length() < 7){
            passwordInput.setError("Password should be 7 characters or longer!");
            passwordInput.requestFocus();
            return;
        }

        // Control structure for use case 3.
        if(!newPassword.equals(newConfirmPassword)){
            confirmPasswordInput.setError("Passwords do not match!");
            confirmPasswordInput.requestFocus();
            return;
        }

        // ProgressBar settings
        progressBar2.setVisibility(View.VISIBLE);

        // FireBase login handling
        mAuth.createUserWithEmailAndPassword(newEmail, newPassword).addOnCompleteListener(task -> {
            // Check if the registration is complete and successful
            if(task.isSuccessful()){
                UserAccount user = new UserAccount(newEmail, newNumber, newUsername);
                // Sends user info to RealTime database

                database = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app");
                myRef = database
                        .getReference("user")
                        .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                        .setValue(user).addOnCompleteListener(task1 -> {
                            // if registration is successful and data has been written to database
                            if(task1.isSuccessful()){
                                textView2.setText("Be Ready To IgNight!");
                                // moves to profile creation activity
                                Intent toProfileCreation = new Intent(this, ProfileCreationActivity.class);
                                startActivity(toProfileCreation);
                            }
                            else{ // registration unsuccessful
                                Toast.makeText(getApplicationContext(),
                                        "Registration unsuccessful. Please try again later.",
                                        Toast.LENGTH_LONG).show();
                            }
                            progressBar2.setVisibility(View.GONE);
                        });
            }
            else{ // registration unsuccessful
                Toast.makeText(getApplicationContext(),
                        "Registration unsuccessful. Please try again later.",
                        Toast.LENGTH_LONG).show();
                progressBar2.setVisibility(View.GONE);
            }
        });
    }
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

}