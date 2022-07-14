package sg.edu.np.ignight;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import sg.edu.np.ignight.Blog.CommentsAdapter;
import sg.edu.np.ignight.Objects.Comment;

public class CommentSectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        // Retrieves uid who posted the blogs & id of blog
        String uid = intent.getStringExtra("uid");
        String blogID = intent.getStringExtra("blogID");

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("user").child(uid)
                .child(blogID).child("comments");
        
        setContentView(R.layout.activity_comment_section);
        ArrayList<Comment> commentsList = new ArrayList<>();





        CommentsAdapter commentsAdapter = new CommentsAdapter(this, commentsList);
        RecyclerView rv = findViewById(R.id.commentsRV);
        rv.setAdapter(commentsAdapter);









    }
}