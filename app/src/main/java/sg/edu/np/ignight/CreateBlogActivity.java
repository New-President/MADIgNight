package sg.edu.np.ignight;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateBlogActivity extends AppCompatActivity {

    private static final int GALLERY_REQUEST = 1;
    private ImageView blogImg;
    private Uri imgUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_blog);

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference myRef = database.getReference("user");

        ImageButton backBtn = findViewById(R.id.backButton);

        blogImg = findViewById(R.id.createBlogImg);

        Button uploadBtn = findViewById(R.id.uploadBtn);

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                    startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });

        Button postBtn = findViewById(R.id.postBtn);

        EditText editDesc = findViewById(R.id.editBlogDesc);
        EditText editLocation = findViewById(R.id.editBlogLocation);

        String blogDesc = editDesc.getText().toString().trim();
        String blogLoc = editLocation.getText().toString().trim();

        Blog newBlog = new Blog(blogDesc, blogLoc, imgUri.toString());

        // Store in firebase under Users
        myRef.child("").setValue(newBlog);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){
            imgUri = data.getData();

            blogImg.setImageURI(imgUri);
        }




    }
}