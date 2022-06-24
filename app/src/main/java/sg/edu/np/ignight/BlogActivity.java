package sg.edu.np.ignight;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import sg.edu.np.ignight.Objects.BlogObject;
import sg.edu.np.ignight.Blog.BlogAdapter;
import sg.edu.np.ignight.Objects.UserObject;

public class BlogActivity extends AppCompatActivity {
    private ArrayList<BlogObject> blogsList;
    private Context context;
    private DatabaseReference databaseReference;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog);
        context = this;
        blogsList = new ArrayList<>();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();


        // For viewing own profile
        String uid = user.getUid();

        database = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/");
        UserObject userObject = (UserObject) getIntent().getSerializableExtra("user");

        // Takes user's own UID to retrieve their own blogs when 'Create Blogs' is pressed in the Main Menu side menu
        if (userObject == null){
            databaseReference = database.getReference("user").child(uid).child("blog");
        }
        // Retrieves other profiles there is retrieved userObject from ProfileActivity
        else{
            databaseReference = database.getReference("user").child(userObject.getUid()).child("blog");
        }

        RecyclerView rv = findViewById(R.id.blogRecycler);
        // Updates changes to the activity when a change is made in the database
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                blogsList.clear();
                List<String> keys = new ArrayList<>();
                for(DataSnapshot keyNode : snapshot.getChildren()){
                    keys.add(keyNode.getKey());
                    BlogObject blog = keyNode.getValue(BlogObject.class);
                    blogsList.add(blog);
                }

                // Reloads adapter when there is a change in database
                BlogAdapter adapter = new BlogAdapter(BlogActivity.this, blogsList, userObject);
                rv.setAdapter(adapter);
                LinearLayoutManager layout = new LinearLayoutManager(context);
                layout.setOrientation(LinearLayoutManager.VERTICAL);
                rv.setLayoutManager(layout);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Cancelled", error.toString());
            }
        });


        // Only show the create blogs button when viewing own profile
        FloatingActionButton createBlogBtn = findViewById(R.id.createBlogBtn);
        Boolean canEdit = getIntent().getBooleanExtra("canEdit", false);
        if(!canEdit){
            createBlogBtn.setVisibility(View.GONE);
        }
        else{
            createBlogBtn.setVisibility(View.VISIBLE);
        }

        // Goes back to previous activity
        ImageButton backBtn = findViewById(R.id.BlogBackButton);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // If the create blog button is present, goes to CreateBlogActivity
        createBlogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createBlogPage = new Intent(BlogActivity.this, CreateBlogActivity.class);
                startActivity(createBlogPage);
            }
        });

    }

}