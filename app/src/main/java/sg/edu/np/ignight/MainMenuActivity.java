package sg.edu.np.ignight;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import sg.edu.np.ignight.Objects.TimestampObject;

public class MainMenuActivity extends AppCompatActivity {
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private ImageView notificationButton;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String Uid = user.getUid();
    final String[] queryName = {""};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        ImageView ownprofile = findViewById(R.id.ownerprofile_menu);
        ownprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainmenu_to_sidemenu = new Intent(MainMenuActivity.this, SideMenu.class);
                startActivity(mainmenu_to_sidemenu);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            }
        });
        updateConnection();
        getFCMToken();

        Intent intent = getIntent();
        String intentExtra = intent.getStringExtra("showFrag");

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        if (intentExtra != null && intentExtra.equals("chatlist")) {
            ft.replace(R.id.frameLayout_menu, new ChatListFragment());
        }
        else {
            ft.replace(R.id.frameLayout_menu, new Homepage_fragment());
        }
        ft.commit();

        EditText searchUsername = findViewById(R.id.searchUsername);
        searchUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            // set queryName to the text in search box and update Homepagefragment when text changes in the search box
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (searchUsername.getText().toString().length() == 0) {
                    queryName[0] = "";
                }
                else {
                    queryName[0] = searchUsername.getText().toString();
                }

                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.frameLayout_menu, new Homepage_fragment());
                ft.commit();
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        Button home = findViewById(R.id.home_menu);// go back to home menu
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.frameLayout_menu, new Homepage_fragment());
                ft.commit();
            }
        });

        Button chat = findViewById(R.id.chat_menu);// list of chats with other people (Use fragment view)
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.frameLayout_menu, new ChatListFragment());
                ft.commit();
            }
        });
//        ImageView ownerProfilePic = findViewById(R.id.ownerprofile_menu);
//
//        FirebaseDatabase database = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/");
//        DatabaseReference myRef = database.getReference("user");
//        storage = FirebaseStorage.getInstance("gs://madignight.appspot.com");
//        myRef.child(Uid).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                String existProfilePic = snapshot.child("Profile Picture").getValue(String.class);
//                storageReference = storage.getReference().child("profilePicture/" + Uid + "/" + existProfilePic);
//
//                try {
//                    final File localFile = File.createTempFile(existProfilePic, existProfilePic);
//                    storageReference.getFile(localFile)
//                            .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
//                                @Override
//                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
//                                    //Toast.makeText(MainMenuActivity.this, "Picture Retrieved", Toast.LENGTH_SHORT).show();
//                                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
//                                    ownerProfilePic.setImageBitmap(bitmap);
//                                }
//                            })
//                            .addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    Toast.makeText(MainMenuActivity.this, "Error loading profile picture", Toast.LENGTH_SHORT).show();
//                                }
//                            });
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//
    }

    private void getFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                    return;
                }

                // Get new FCM registration token and put in db
                String token = task.getResult();
                DatabaseReference userDB = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("user").child(FirebaseAuth.getInstance().getUid());
                Map tokenMap = new HashMap<>();
                tokenMap.put("fcmToken", token);
                userDB.updateChildren(tokenMap);
            }
        });

    }

    // updates presence system - when user logs on, set connection to true, when user logs off, set connection to null and update last online time
    private void updateConnection() {
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/");

        DatabaseReference userPresenceRef = db.getReference("presence/" + FirebaseAuth.getInstance().getUid());

        DatabaseReference connectionRef = userPresenceRef.child("connection");
        DatabaseReference lastOnlineRef = userPresenceRef.child("lastOnline");

        DatabaseReference connectedRef = db.getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);

                if (connected) {
                    connectionRef.setValue(true);
                    lastOnlineRef.removeValue();

                    connectionRef.onDisconnect().setValue(false);


                    try {
                        lastOnlineRef.onDisconnect().setValue(new TimestampObject(new Date().toString()).toString());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error.getMessage());
            }
        });

        notificationButton = findViewById(R.id.notificationButton);
        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goNotification = new Intent(getApplicationContext(), NotificationActivity.class);
                startActivity(goNotification);
            }
        });

    }

    public String getQueryName() {
        return queryName[0];
    }

    private void Refresh(){
        Intent refresh = new Intent(this, MainMenuActivity.class);
        startActivity(refresh);
    }

}