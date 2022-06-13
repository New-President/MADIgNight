package sg.edu.np.ignight;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

// To authenticate user (phone number authentication with Firebase) and continue to app
public class LoginActivity extends AppCompatActivity {

    private EditText phoneNumberInput, codeInput;
    private Button loginButton, sendOTPButton, resetLoginFieldsButton;
    private TextView errorMessage, phonePrefix;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;

    String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userIsLoggedIn();

        // initialize various fields
        phoneNumberInput = findViewById(R.id.phoneNumberInput);  // EditText field for phone number
        codeInput = findViewById(R.id.OTPInput);  // EditText field for OTP
        sendOTPButton = findViewById(R.id.sendOTPButton);  // Button for sending OTP
        loginButton = findViewById(R.id.loginButton);  // Button for verification and login
        resetLoginFieldsButton = findViewById(R.id.resetLoginFields);  // Button to reset fields to default
        errorMessage = findViewById(R.id.loginErrorMessage);  // TextView to show error in logging in
        phonePrefix = findViewById(R.id.phonePrefix);  // TextView with phone number prefix (set to +65)

        // turn off phone auth app verification
        FirebaseAuth.getInstance().getFirebaseAuthSettings().setAppVerificationDisabledForTesting(true);

        // set default fields first
        setDefaultFields(false);

        // callbacks to be used in startPhoneNumberVerification()
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            // verification complete -> sign in with credentials
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            // OTP sent -> store verification id
            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                verificationId = s;
            }

            // verification failed -> call setDefaultFields()
            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                setDefaultFields(true);
            }
        };

        // enable sendOTPButton if phone number entered is 8 characters
        phoneNumberInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String phoneNumber = phoneNumberInput.getText().toString();
                sendOTPButton.setEnabled(phoneNumber.length() == 8);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        // enable loginButton if OTP entered is 6 characters
        codeInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String code = codeInput.getText().toString();
                loginButton.setEnabled(code.length() == 6);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        // enable codeInput and disable phoneNumberInput and sendOTPButton
        // hide the error message in case it is visible
        // start countDownTimer and send OTP
        sendOTPButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleEditText(codeInput, true);
                toggleEditText(phoneNumberInput, false);
                sendOTPButton.setEnabled(false);
                errorMessage.setVisibility(View.GONE);
                allowResendOTP();
                startPhoneNumberVerification();
            }
        });

        // verify phone number with verification code and disable codeInput and loginButton
        // stop countDownTimer if it is active and reset sendOTPButton text
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleEditText(codeInput, false);
                loginButton.setEnabled(false);
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                    sendOTPButton.setText("send OTP");
                }
                verifyPhoneNumberWithCode();
            }
        });

        // reset login fields to default
        // stop countDownTimer if it is active and reset sendOTPButton text
        resetLoginFieldsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                    sendOTPButton.setText("send OTP");
                }
                setDefaultFields(false);
            }
        });

    }

    // start to verify phone number (send otp to the retrieved phone number)
    private void startPhoneNumberVerification() {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder()
                .setPhoneNumber(phonePrefix.getText().toString() + phoneNumberInput.getText().toString())
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(callbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    // create a PhoneAuthCredential object with the verification id obtained from onCodeSent()
    private void verifyPhoneNumberWithCode() {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, codeInput.getText().toString());
        signInWithPhoneAuthCredential(credential);
    }

    // sign in the user with the credentials
    private void signInWithPhoneAuthCredential(PhoneAuthCredential phoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                // if successfully logged in -> update database and call userIsLoggedIn(), otherwise call setDefaultFields()
                if (task.isSuccessful()) {

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    if (user != null) {
                        final DatabaseReference userDB = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("user").child(user.getUid());

                        userDB.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (!snapshot.exists()) {
                                    Map<String, Object> userMap = new HashMap<>();
                                    userMap.put("phone", user.getPhoneNumber());
                                    userDB.updateChildren(userMap);
                                }

                                userIsLoggedIn();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                setDefaultFields(true);
                            }
                        });
                    }
                }
                else {
                    setDefaultFields(true);
                }
            }
        });
    }

    // check if there is an authenticated user and direct to ChatListActivity
    private void userIsLoggedIn() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            startActivity(new Intent(getApplicationContext(), ChatListActivity.class));
            finish();
        }
    }

    // reset fields to default
    // clear verification id / reset input fields / disable buttons / show error message if showErrorMessage is true
    private void setDefaultFields(boolean showErrorMessage) {
        verificationId = null;
        phoneNumberInput.setText("");
        toggleEditText(phoneNumberInput, true);
        codeInput.setText("");
        toggleEditText(codeInput, false);
        sendOTPButton.setEnabled(false);
        loginButton.setEnabled(false);
        errorMessage.setVisibility((showErrorMessage)?View.VISIBLE:View.GONE);
    }

    // to disable/enable EditText field - set toEnable to false to disable/true to enable
    private void toggleEditText(EditText editText, boolean toEnable) {
        if (toEnable) {
            editText.setFocusableInTouchMode(true);
        }
        else {
            editText.setFocusable(false);
        }
        editText.setEnabled(toEnable);
        editText.setCursorVisible(toEnable);
    }

    CountDownTimer countDownTimer;

    // use countdown timer to change sendOTPButton text and enable the button to allow resending the OTP after one minute
    private void allowResendOTP() {
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long l) {
                sendOTPButton.setText(Long.toString(Math.round(l / 1000.0)) + "(s)");
            }

            @Override
            public void onFinish() {
                sendOTPButton.setText("send OTP");
                sendOTPButton.setEnabled(true);
            }
        };
        countDownTimer.start();
    }
}