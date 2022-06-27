package sg.edu.np.ignight;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.firebase.database.ChildEventListener;
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

    private RecyclerView blogRV;
    private BlogAdapter blogAdapter;
    private LinearLayoutManager blogLayoutManager;

    private UserObject userObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog);
        context = this;
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        // When viewing own profile, gets own UID
        String uid = user.getUid();

        database = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/");
        userObject = (UserObject) getIntent().getSerializableExtra("user");

        // Takes user's own UID to retrieve their own blogs when 'Create Blogs' is pressed in the Main Menu side menu
        if (userObject == null){
            databaseReference = database.getReference("user").child(uid).child("blog");
        }
        // Retrieves other profiles there is retrieved userObject from ProfileActivity
        else{
            databaseReference = database.getReference("user").child(userObject.getUid()).child("blog");
        }

        getBlogList();
        initRecyclerView();

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

    private void initRecyclerView() {
        blogsList = new ArrayList<>();
        blogRV = findViewById(R.id.blogRecycler);
        blogRV.setNestedScrollingEnabled(false);
        blogAdapter = new BlogAdapter(BlogActivity.this, blogsList, userObject);
        blogRV.setAdapter(blogAdapter);
        blogLayoutManager = new LinearLayoutManager(context);
        blogLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        blogRV.setLayoutManager(blogLayoutManager);
    }

    private void getBlogList() {
        databaseReference.addChildEventListener(new ChildEventListener() {

            // Updates changes to the activity when a change is made in the database
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ArrayList<String> blogIDList = new ArrayList<>();

                if (snapshot.exists()) {
                    if (!blogIDList.contains(snapshot.getKey())) {

                        blogIDList.add(snapshot.getKey());

                        String blogID = snapshot.child("blogID").getValue().toString();
                        String description = snapshot.child("description").getValue().toString();
                        String imgID = snapshot.child("imgID").getValue().toString();
                        String location = snapshot.child("location").getValue().toString();
                        int likes = Integer.parseInt(snapshot.child("likes").getValue().toString());
                        int comments = Integer.parseInt(snapshot.child("comments").getValue().toString());
                        //boolean liked = Boolean.parseBoolean(snapshot.child("liked").getValue().toString());

                        ArrayList<String> commentsList = new ArrayList<>();

                        if (snapshot.child("commentsList").hasChildren()) {
                            for (DataSnapshot commentSnapshot : snapshot.child("commentsList").getChildren()) {
                                commentsList.add(commentSnapshot.getValue().toString());
                            }
                        }

                        ArrayList<String> likedUsers = new ArrayList<>();

                        if (snapshot.child("likedUsers").hasChildren()) {
                            for (DataSnapshot likedUsersSnapshot : snapshot.child("likedUsers").getChildren()) {
                                likedUsers.add(likedUsersSnapshot.getValue().toString());
                            }
                        }

                        BlogObject blogObject = new BlogObject(description, location, imgID, blogID, likes, comments, commentsList, likedUsers);
                        blogsList.add(blogObject);

                        blogAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                for (BlogObject existingBlog : blogsList) {
                    if (existingBlog.blogID.equals(snapshot.getKey())) {

                        String description = snapshot.child("description").getValue().toString();
                        String imgID = snapshot.child("imgID").getValue().toString();
                        String location = snapshot.child("location").getValue().toString();
                        int likes = Integer.parseInt(snapshot.child("likes").getValue().toString());
                        int comments = Integer.parseInt(snapshot.child("comments").getValue().toString());

                        ArrayList<String> commentsList = new ArrayList<>();

                        if (snapshot.child("commentsList").hasChildren()) {
                            for (DataSnapshot commentSnapshot : snapshot.child("commentsList").getChildren()) {
                                commentsList.add(commentSnapshot.getValue().toString());
                            }
                        }

                        ArrayList<String> likedUsersList = new ArrayList<>();

                        if (snapshot.child("likedUsersList").hasChildren()) {
                            for (DataSnapshot likedUsersSnapshot : snapshot.child("likedUsersList").getChildren()) {
                                likedUsersList.add(likedUsersSnapshot.getValue().toString());
                            }
                        }

                        existingBlog.setDescription(description);
                        existingBlog.setImgID(imgID);
                        existingBlog.setLocation(location);
                        existingBlog.setLikes(likes);
                        existingBlog.setComments(comments);
                        existingBlog.setCommentsList(commentsList);
                        existingBlog.setLikedUsers(likedUsersList);

                        blogAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error.getMessage());
            }
        });
    }

}