package sg.edu.np.ignight;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

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

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import sg.edu.np.ignight.Objects.TimestampObject;

public class MainMenuActivity extends AppCompatActivity {
    private FirebaseStorage storage;
    private StorageReference storageReference;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String Uid = user.getUid();

    // Edit profile, Logout, about page, stage 2: map, paywalls, terms & conditions??
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        ImageView ownprofile = findViewById(R.id.ownerprofile_menu);
        ownprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainmenu_to_sidemenu = new Intent(MainMenuActivity.this, side_menu.class);
                startActivity(mainmenu_to_sidemenu);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            }
        });
        updateConnection();

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
        ImageView ownerProfilePic = findViewById(R.id.ownerprofile_menu);

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
                                    Toast.makeText(MainMenuActivity.this, "Picture Retrieved", Toast.LENGTH_SHORT).show();
                                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                    ownerProfilePic.setImageBitmap(bitmap);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainMenuActivity.this, "Error Occurred", Toast.LENGTH_SHORT).show();
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

    private void updateConnection() {
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/");

        db.goOnline();

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
    }
}