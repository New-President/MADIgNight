package sg.edu.np.ignight;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SideMenu extends Activity {

    private FirebaseStorage storage;
    private StorageReference storageReference;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private PhoneAuthProvider.ForceResendingToken resendingToken;
    private boolean initialVerificationSent;

    private String verificationId;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String Uid = user.getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_side_menu);

        // Side menu layout
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*.7),height);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.START;
        params.x = 0;
        params.y = 0;
        getWindow().setAttributes(params);

        // Toast Message
        LayoutInflater inflater = getLayoutInflater();
        View customtoast = inflater.inflate(R.layout.toast_message_bg, findViewById(R.id.toast_message));
        TextView txtMessage = customtoast.findViewById(R.id.toast_message);
        txtMessage.setText("Thanks for smashing this button! unfortunately we do not have this feature yet, we will try to get it out by Stage 2!!!");
        txtMessage.setTextColor(Color.RED);
        Toast mToast = new Toast(getApplicationContext());
        mToast.setDuration(Toast.LENGTH_LONG);
        mToast.setView(customtoast);

        TextView editprofile = findViewById(R.id.editprofile_sidemenu);
        editprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editProfile = new Intent(SideMenu.this, ProfileCreationActivity.class);
                editProfile.putExtra("ProfileCreated", true);
                startActivity(editProfile);
            }
        });

        TextView aboutus = findViewById(R.id.aboutus_sidemenu);
        aboutus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mToast.show();
            }
        });

        TextView premium = findViewById(R.id.premium_sidemenu);
        premium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mToast.show();
            }
        });

        TextView map = findViewById(R.id.map_sidemenu);

        // Asks for location permission
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mapPage = new Intent(SideMenu.this, MapActivity.class);
                startActivity(mapPage);

            }
        });

        TextView TandC = findViewById(R.id.TandC_sidemenu);
        TandC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent main_to_tnc = new Intent(SideMenu.this, TNC.class);
                startActivity(main_to_tnc);
            }
        });

        TextView logout = findViewById(R.id.logout_sidemenu);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/").goOffline();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        // side menu button for activity report activity. pls edit if its wrong
        TextView activityReport = findViewById(R.id.activityReport_sidemenu);
        activityReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ActivityReportActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        TextView createBlogBtn = findViewById(R.id.menuCreateBlogBtn);
        createBlogBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                Intent createBlog = new Intent(SideMenu.this, BlogActivity.class);
                createBlog.putExtra("canEdit", true);
                startActivity(createBlog);
            }
        });

        Button delete_acc = findViewById(R.id.delete_acc_btn);
        delete_acc.setOnClickListener(new View.OnClickListener() { // onclick on delete account
            @Override
            public void onClick(View view1) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SideMenu.this, R.style.AlertDialogTheme);
                View view = LayoutInflater.from(SideMenu.this).inflate(R.layout.activity_delete_alert_dialog, (ConstraintLayout)findViewById(R.id.layoutDialogContainer));
                builder.setView(view);

                final AlertDialog alertDialog = builder.create(); //Display alert dialog

                EditText delete_word = view.findViewById(R.id.delete_input);
                delete_word.addTextChangedListener(new TextWatcher() { //check the DELETE word to confirm delete
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { // check everytime the input changes
                        if (delete_word.getText().toString().equals("DELETE")) { // Matched the DELETE word
                            view.findViewById(R.id.delete_send_otp).setEnabled(true);
                            view.findViewById(R.id.delete_send_otp).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    view.findViewById(R.id.delete_send_otp).setEnabled(false);
                                    EditText get_delete_otp = view.findViewById(R.id.delete_otp);
                                    get_delete_otp.setEnabled(true);
                                    get_delete_otp.addTextChangedListener(new TextWatcher() {
                                        @Override
                                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                                        @Override
                                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                            Log.d("uid",Uid);
                                            if (get_delete_otp.length() == 6) {
                                                view.findViewById(R.id.button_yes_delete).setEnabled(true);
                                                view.findViewById(R.id.button_yes_delete).setBackgroundResource(R.drawable.btn_delete_delete);
                                                view.findViewById(R.id.button_yes_delete).setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        user.delete().addOnCompleteListener(new OnCompleteListener<Void>() { // Delete the user
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.d("Delete Status", "User account deleted.");
                                                                }
                                                            }
                                                        });
                                                        alertDialog.dismiss();
                                                        FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/").goOffline();
                                                        FirebaseAuth.getInstance().signOut();
                                                        Intent main_to_start = new Intent(getApplicationContext(), LoginActivity.class);
                                                        main_to_start.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        startActivity(main_to_start); // Go back to login
                                                        finish();
                                                    }
                                                });
                                            }
                                            else { // If does not match length, set to disable the buttons to prevent miss click
                                                Log.d("hello",get_delete_otp.getText().toString());
                                                view.findViewById(R.id.button_yes_delete).setEnabled(false);
                                                view.findViewById(R.id.button_yes_delete).setBackgroundResource(R.drawable.btn_delete_delete_locked);
                                            }
                                        }

                                        @Override
                                        public void afterTextChanged(Editable editable) {}
                                    });
                                }
                            });
                        }
                        else { // If does not match delete, set to disable the buttons to prevent miss click
                            Log.d("hello",delete_word.getText().toString());
                            view.findViewById(R.id.button_yes_delete).setEnabled(false);
                            view.findViewById(R.id.delete_otp).setEnabled(false);
                            view.findViewById(R.id.delete_send_otp).setEnabled(false);
                            view.findViewById(R.id.button_yes_delete).setBackgroundResource(R.drawable.btn_delete_delete_locked);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {}
                });

                view.findViewById(R.id.button_cancel_delete).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });

                if (alertDialog.getWindow() != null){
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                }
                alertDialog.show();
            }
        });

        ImageView profilePicSideMenu = findViewById(R.id.profilepic_sidemenu);

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference myRef = database.getReference("user");
        storage = FirebaseStorage.getInstance("gs://madignight.appspot.com");

        myRef.child(Uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String existProfilePic = snapshot.child("Profile Picture").getValue(String.class);
                Log.d("Hello",existProfilePic);
                storageReference = storage.getReference().child("profilePicture/" + Uid + "/" + existProfilePic);

                try {
                    final File localFile = File.createTempFile(existProfilePic, existProfilePic);
                    storageReference.getFile(localFile)
                            .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    //Toast.makeText(SideMenu.this, "Picture Retrieved", Toast.LENGTH_SHORT).show();
                                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                    profilePicSideMenu.setImageBitmap(bitmap);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(SideMenu.this, "Error loading profile picture", Toast.LENGTH_SHORT).show();
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

    private CountDownTimer countDownTimer;

    // use countdown timer to change sendOTPButton text and enable the button to allow resending the OTP after one minute
    private void allowResendOTP(View view) {
        countDownTimer = new CountDownTimer(60000, 1000) {
            Button send_otp = view.findViewById(R.id.delete_send_otp);
            @Override
            public void onTick(long l) {
                send_otp.setText(Long.toString(Math.round(l / 1000.0)) + "(s)");
            }

            @Override
            public void onFinish() {
                send_otp.setText("send OTP");
                send_otp.setEnabled(true);
            }
        };
        countDownTimer.start();
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
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }
}