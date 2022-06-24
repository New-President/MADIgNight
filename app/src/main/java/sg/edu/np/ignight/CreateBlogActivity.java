package sg.edu.np.ignight;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import sg.edu.np.ignight.Blog.LoadingBlogDialog;
import sg.edu.np.ignight.Objects.BlogObject;

public class CreateBlogActivity extends AppCompatActivity {

    private static final int GALLERY_REQUEST = 1;
    private ImageView blogImg;
    private Uri imgUri;
    private Button uploadBtn;
    private String uid;
    int counter = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_blog);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        assert user != null;
        uid = user.getUid();
        Context c = this;
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference databaseReference = database.getReference("user").child(uid).child("blog");

        final LoadingBlogDialog loadingBlogDialog = new LoadingBlogDialog(CreateBlogActivity.this);
        ImageButton backBtn = findViewById(R.id.createBlogBackButton);
        blogImg = findViewById(R.id.createBlogImg);
        uploadBtn = findViewById(R.id.uploadBtn);
        Button postBtn = findViewById(R.id.postBtn);
        EditText editDesc = findViewById(R.id.editBlogDesc);
        EditText editLocation = findViewById(R.id.editBlogLocation);
        TextView errorMsg = findViewById(R.id.errorMsg);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Opens photo gallery and takes photo as input only
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });

        postBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                String blogDesc = editDesc.getText().toString().trim();
                String blogLoc = editLocation.getText().toString().trim();

                // Input Validation for missing blog image, description and location
                if (imgUri == null) {
                    errorMsg.setText("Upload an image before posting!");
                }
                if(blogDesc.length() <= 5) {
                    errorMsg.setText("Give your blog more information");
                }
                else if (TextUtils.isEmpty(blogLoc)) {
                    errorMsg.setText("Enter a location");
                }
                else{
                    // Creates a unique ID for the blog post
                    String blogID = randomAlphanumericGenerator();
                    BlogObject newBlog = new BlogObject(blogDesc, blogLoc, uploadImage(c), blogID);
                    // Store in firebase under Users
                    databaseReference.child(blogID).setValue(newBlog);
                    loadingBlogDialog.startLoadingDialog();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadingBlogDialog.dismissDialog();
                            finish();
                        }
                    }, 6000);

                }
            }
        });

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Log.d("TAG", "onDataChange: Added information to database \n " + dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("ValueFail", "Failed to read value.", error.toException());
            }
        });
    }
    // Retrieves input from the gallery intent and sets image in blog
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){
            imgUri = data.getData();
            blogImg.setImageURI(imgUri);
            blogImg.setPadding(0,0,0,0);
            uploadBtn.setText("Change");
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String uploadImage(Context c){
        final String randomKey = UUID.randomUUID().toString();
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://madignight.appspot.com");
        StorageReference storageReference = storage.getReference().child("blog/" + uid).child(randomKey);

        storageReference.putFile(imgUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //Snackbar.make(view, "Image Uploaded", Snackbar.LENGTH_LONG).show();

                        Toast.makeText(c, "Blog uploaded", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(c, "Failed to upload", Toast.LENGTH_LONG).show();
                    }
                });
        return randomKey;
    }
    // Create random unique alphanumeric string
    @RequiresApi(api = Build.VERSION_CODES.N)
    public String randomAlphanumericGenerator() {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 28;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }


}