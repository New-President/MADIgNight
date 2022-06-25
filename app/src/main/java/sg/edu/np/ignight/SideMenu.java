package sg.edu.np.ignight;

import androidx.annotation.NonNull;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
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

public class SideMenu extends Activity {

    private FirebaseStorage storage;
    private StorageReference storageReference;

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
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mToast.show();
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

        TextView createBlogBtn = findViewById(R.id.menuCreateBlogBtn);
        createBlogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createBlog = new Intent(SideMenu.this, BlogActivity.class);
                createBlog.putExtra("canEdit", true);
                startActivity(createBlog);
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
                                    Toast.makeText(SideMenu.this, "Picture Retrieved", Toast.LENGTH_SHORT).show();
                                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                    profilePicSideMenu.setImageBitmap(bitmap);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(SideMenu.this, "Error Occurred", Toast.LENGTH_SHORT).show();
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

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }
}