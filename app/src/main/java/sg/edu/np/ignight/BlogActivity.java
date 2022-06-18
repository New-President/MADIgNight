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
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class BlogActivity extends AppCompatActivity {
    private ArrayList<Blog> blogsList;
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog);
        context = this;
        blogsList = new ArrayList<>();
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
                    Blog blog = keyNode.getValue(Blog.class);
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

            }
        });



        ImageButton backBtn = findViewById(R.id.backButton);
        FloatingActionButton createBlogBtn = findViewById(R.id.createBlogBtn);



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