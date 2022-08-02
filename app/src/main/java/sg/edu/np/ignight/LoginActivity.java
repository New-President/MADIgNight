package sg.edu.np.ignight;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

// To authenticate user (phone number authentication with Firebase) and continue to app
public class LoginActivity extends AppCompatActivity {

    private EditText phoneNumberInput, codeInput;
    private Button loginButton, sendOTPButton, resetLoginFieldsButton;
    private TextView errorMessage, phonePrefix;
    private ImageView loginSuccessImage;
    private ProgressBar loginProgressBar;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private PhoneAuthProvider.ForceResendingToken resendingToken;
    private boolean initialVerificationSent;

    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /*FirebaseAuth.getInstance().signOut();*/
        FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/").goOnline();

        // to update db values
        // (to add username for chat users)
//        FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot chatSnapshot : snapshot.child("chat").getChildren()) {
//                    String chatid = chatSnapshot.getKey();
//
//                    for (DataSnapshot userSnapshot : chatSnapshot.child("users").getChildren()) {
//                        String uid = userSnapshot.getKey();
//                        String username = snapshot.child("user").child(uid).child("username").getValue().toString();
//                        userSnapshot.getRef().setValue(username);
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e(TAG, "onCancelled: " + error.getMessage());
//            }
//        });
//
        // (to set profileUrl for users)
//        FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("user").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
//                    String uid = userSnapshot.getKey();
//
//                    if (!Boolean.parseBoolean(userSnapshot.child("profileCreated").getValue().toString())) {
//                        continue;
//                    }
//                    if (userSnapshot.child("profileUrl").exists()) {
//                        continue;
//                    }
//
//                    String imageKey = userSnapshot.child("Profile Picture").getValue().toString();
//                    FirebaseStorage.getInstance("gs://madignight.appspot.com/").getReference().child("profilePicture").child(uid).child(imageKey).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                        @Override
//                        public void onSuccess(Uri uri) {
//                            FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("user").child(uid).child("profileUrl").setValue(uri.toString());
//                        }
//                    });
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e(TAG, "onCancelled: " + error.getMessage());
//            }
//        });

        // to add onCall for chats
//        FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("chat").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
//                    FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("chat").child(chatSnapshot.getKey()).child("onCall").setValue(false);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//
//        // remove nodes with deleted user uids
//        DatabaseReference rootDB = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
//        ArrayList<String> uidList = new ArrayList<>();
//        rootDB.child("user").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
//                    if (childSnapshot.child("phone").exists()  && childSnapshot.child("profileCreated").exists()){
//                        uidList.add(childSnapshot.getKey());
//                    }
//                }
//
//                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
//                    if (childSnapshot.child("chats").exists()) {
//                        for (DataSnapshot chatSnapshot : childSnapshot.child("chats").getChildren()) {
//                            if (!uidList.contains(chatSnapshot.getValue().toString())) {
//                                chatSnapshot.getRef().setValue(null);
//                            }
//                        }
//                    }
//                    if (childSnapshot.child("chatRequests").exists()) {
//                        if (childSnapshot.child("chatRequests").child("received").exists()) {
//                            for (DataSnapshot receivedSnapshot : childSnapshot.child("chatRequests").child("received").getChildren()) {
//                                if (!uidList.contains(receivedSnapshot.getValue().toString())) {
//                                    receivedSnapshot.getRef().setValue(null);
//                                }
//                            }
//                        }
//                        if (childSnapshot.child("chatRequests").child("sent").exists()) {
//                            for (DataSnapshot sentSnapshot : childSnapshot.child("chatRequests").child("received").getChildren()) {
//                                if (!uidList.contains(sentSnapshot.getValue().toString())) {
//                                    sentSnapshot.getRef().setValue(null);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });

        getPermission();

        // initialize Firebase
        FirebaseApp.initializeApp(this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(SafetyNetAppCheckProviderFactory.getInstance());

        // initialize Fresco (library to view images full screen)
        Fresco.initialize(this);

        // initialize various fields
        phoneNumberInput = findViewById(R.id.phoneNumberInput);  // EditText field for phone number
        codeInput = findViewById(R.id.OTPInput);  // EditText field for OTP
        sendOTPButton = findViewById(R.id.sendOTPButton);  // Button for sending OTP
        loginButton = findViewById(R.id.loginButton);  // Button for verification and login
        resetLoginFieldsButton = findViewById(R.id.resetLoginFields);  // Button to reset fields to default
        errorMessage = findViewById(R.id.loginErrorMessage);  // TextView to show error in logging in
        phonePrefix = findViewById(R.id.phonePrefix);  // TextView with phone number prefix (set to +65)
        loginSuccessImage = findViewById(R.id.loginSuccessImage);  // ImageView to show login success
        loginProgressBar = findViewById(R.id.loginProgressBar);  // ProgressBar to show login loading

        // set default fields first
        setDefaultFields(false);

        // check if user is already logged in
        userIsLoggedIn(true);

        // callbacks to be used in startPhoneNumberVerification()
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            // verification complete -> sign in with credentials
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            // OTP sent -> store verification id and resending token, start timer for OTP resend
            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                verificationId = s;
                initialVerificationSent = true;
                resendingToken = forceResendingToken;
                allowResendOTP();
            }

            // verification failed -> call setDefaultFields()
            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                e.printStackTrace();
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
                if (phoneNumber.length() == 8) {
                    enableButton(sendOTPButton);
                }
                else {
                    disableButton(sendOTPButton);
                }
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
                if (code.length() == 6) {
                    enableButton(loginButton);
                }
                else {
                    disableButton(loginButton);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        // enable codeInput and disable phoneNumberInput and sendOTPButton
        // hide the error message in case it is visible
        // start verification or resend verification (if initialVerificationSent is true)
        sendOTPButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleEditText(codeInput, true);
                toggleEditText(phoneNumberInput, false);
                disableButton(sendOTPButton);
                errorMessage.setVisibility(View.GONE);

                if (initialVerificationSent) {
                    resendVerificationCode();
                }
                else {
                    startPhoneNumberVerification();
                }
            }
        });

        // verify phone number with verification code, disable codeInput and show progress bar
        // stop countDownTimer if it is active and reset sendOTPButton text
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleEditText(codeInput, false);
                disableButton(loginButton);
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                    sendOTPButton.setText("send OTP");
                }
                showProgressBar();
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

    // enable a button and change its text color to black
    private void enableButton(Button button) {
        button.setEnabled(true);
        button.setTextColor(Color.parseColor("#000000"));
    }

    // disable a button and change its text color to gray
    private void disableButton(Button button) {
        button.setEnabled(false);
        button.setTextColor(Color.parseColor("#666666"));
    }

    // shows the progress bar and hides the login button
    private void showProgressBar() {
        loginButton.setVisibility(View.GONE);
        loginProgressBar.setVisibility(View.VISIBLE);
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

    // resend otp
    private void resendVerificationCode() {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder()
                .setPhoneNumber(phonePrefix.getText().toString() + phoneNumberInput.getText().toString())
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(callbacks)
                .setForceResendingToken(resendingToken)
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
                        DatabaseReference userDB = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("user");

                        userDB.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (!snapshot.child(user.getUid()).exists()) {
                                    Map<String, Object> userMap = new HashMap<>();
                                    userMap.put("phone", user.getPhoneNumber());
                                    userMap.put("profileCreated", false);
                                    userDB.child(user.getUid()).updateChildren(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            userIsLoggedIn(false);
                                        }
                                    });
                                }
                                else {
                                    userIsLoggedIn(false);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "onCancelled: " + error.getMessage());
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

    // check if there is an authenticated user
    // go to MainMenuActivity if user is already authenticated before (initialCall is true)
    // otherwise, go to MainMenuActivity if user already created profile or go to ProfileCreationActivity if user hasn't created profile
    private void userIsLoggedIn(boolean initialCall) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference userDB = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("user").child(user.getUid());

            if (!initialCall) {
                Handler handler = new Handler();
                loginProgressBar.setVisibility(View.GONE);
                loginSuccessImage.setVisibility(View.VISIBLE);

                userDB.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean profileCreated = snapshot.child("profileCreated").getValue().toString().equals("true");

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent;
                                if (profileCreated) {
                                    intent = new Intent(getApplicationContext(), MainMenuActivity.class);
                                }
                                else {
                                    intent = new Intent(getApplicationContext(), ProfileCreationActivity.class);
                                    intent.putExtra("fromLogin", true);
                                }

                                startActivity(intent);
                                finish();
                            }
                        }, 1000);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "onCancelled: " + error.getMessage());
                    }
                });
            }
            else {
                startActivity(new Intent(getApplicationContext(), MainMenuActivity.class));
                finish();
            }
        }
    }

    // reset fields to default
    private void setDefaultFields(boolean showErrorMessage) {
        loginProgressBar.setVisibility(View.GONE);
        errorMessage.setVisibility(showErrorMessage?View.VISIBLE:View.GONE);
        verificationId = null;
        resendingToken = null;
        initialVerificationSent = false;
        phoneNumberInput.setText("");
        toggleEditText(phoneNumberInput, true);
        codeInput.setText("");
        toggleEditText(codeInput, false);
        disableButton(sendOTPButton);
        disableButton(loginButton);
        loginSuccessImage.setVisibility(View.GONE);
        loginButton.setVisibility(View.VISIBLE);
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

    private CountDownTimer countDownTimer;

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
                enableButton(sendOTPButton);
            }
        };
        countDownTimer.start();
    }

    // request permissions
    private void getPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[] {Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }
}