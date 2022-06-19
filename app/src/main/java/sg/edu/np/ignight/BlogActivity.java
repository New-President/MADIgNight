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

import sg.edu.np.ignight.Blog.BlogObject;
import sg.edu.np.ignight.Blog.BlogAdapter;

public class BlogActivity extends AppCompatActivity {
    private ArrayList<BlogObject> blogsList;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog);
        context = this;
        blogsList = new ArrayList<>();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
//        assert user != null;
//        String uid = user.getUid();

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference databaseReference = database.getReference("user").child("SqDiaNh7KGhYd09lWeVpVrRTSKc2").child("blog");


        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                blogsList.clear();
                List<String> keys = new ArrayList<>();
                for(DataSnapshot keyNode : snapshot.getChildren()){
                    keys.add(keyNode.getKey());
                    Log.d("key", keyNode.getKey());
                    BlogObject blog = keyNode.getValue(BlogObject.class);
                    blogsList.add(blog);
                }


                BlogAdapter adapter = new BlogAdapter(BlogActivity.this, blogsList);
                RecyclerView rv = findViewById(R.id.blogRecycler);
                LinearLayoutManager layout = new LinearLayoutManager(context);
                layout.setOrientation(LinearLayoutManager.VERTICAL);
                rv.setAdapter(adapter);
                rv.setLayoutManager(layout);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Cancelled", error.toString());
            }
        });

        ImageButton backBtn = findViewById(R.id.backButton);

        FloatingActionButton createBlogBtn = findViewById(R.id.createBlogBtn);

//        if(!getIntent().getStringExtra("UID").equals("SqDiaNh7KGhYd09lWeVpVrRTSKc2")){
//            createBlogBtn.setVisibility(View.GONE);
//        }

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        createBlogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createBlogPage = new Intent(BlogActivity.this, CreateBlogActivity.class);
                startActivity(createBlogPage);
            }
        });
    }



}