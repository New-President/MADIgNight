package sg.edu.np.ignight;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import sg.edu.np.ignight.Blog.LoadingBlogDialog;
import sg.edu.np.ignight.Objects.BlogObject;
import sg.edu.np.ignight.Blog.BlogAdapter;
import sg.edu.np.ignight.Objects.UserObject;

@RequiresApi(api = Build.VERSION_CODES.N)
public class BlogActivity extends AppCompatActivity {
    private ArrayList<BlogObject> blogsList;
    private ArrayList<String> blogIDList;
    private Context context;
    private DatabaseReference databaseReference;
    private FirebaseDatabase database;
    private Boolean canEdit;
    private RecyclerView blogRV;
    private BlogAdapter blogAdapter;
    private LinearLayoutManager blogLayoutManager;
    private UserObject userObject;
    private LoadingBlogDialog loadingBlogDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog);
        context = this;

        loadingScreen();
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

        // Only show the create blogs button when viewing own profile
        FloatingActionButton createBlogBtn = findViewById(R.id.createBlogBtn);
        TextView header = findViewById(R.id.blogActivityHeader);
        canEdit = getIntent().getBooleanExtra("canEdit", false);
        if(!canEdit){
            createBlogBtn.setVisibility(View.GONE);
            header.setText("Blog Posts");

        }
        else{
            createBlogBtn.setVisibility(View.VISIBLE);
            header.setText("My Blog Posts");
        }

        initRecyclerView();
        getBlogList();

        // Goes back to previous activity
        ImageButton backBtn = findViewById(R.id.BlogBackButton);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userObject != null){
                    Intent intent = new Intent(getApplicationContext(), ProfileViewActivity.class);
                    intent.putExtra("user", userObject);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                else{
                    finish();
                }
            }
        });

        // If the create blog button is present, goes to CreateBlogActivity
        createBlogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createBlogPage = new Intent(BlogActivity.this, CreateBlogActivity.class);
                createBlogPage.putExtra("deleteBlog", false);
                startActivity(createBlogPage);
            }
        });

    }


    private void initRecyclerView() {
        blogsList = new ArrayList<>();
        blogRV = findViewById(R.id.blogRecycler);
        blogRV.setNestedScrollingEnabled(false);
        blogAdapter = new BlogAdapter(BlogActivity.this, blogsList, userObject, canEdit);
        blogRV.setAdapter(blogAdapter);
        blogLayoutManager = new LinearLayoutManager(context);
        blogLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        blogRV.setLayoutManager(blogLayoutManager);
    }

    private void getBlogList() {

        blogIDList = new ArrayList<>();
        databaseReference.addChildEventListener(new ChildEventListener() {

            // Updates changes to the activity when a change is made in the database
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                if (snapshot.exists()) {
                    if (!blogIDList.contains(snapshot.getKey())) {

                        blogIDList.add(snapshot.getKey());

                        String blogID = snapshot.child("blogID").getValue().toString();
                        String description = snapshot.child("description").getValue().toString();
                        String imgID = snapshot.child("imgID").getValue().toString();
                        String location = snapshot.child("location").getValue().toString();
                        int likes = Integer.parseInt(snapshot.child("likes").getValue().toString());
                        int comments = Integer.parseInt(snapshot.child("comments").getValue().toString());

                        ArrayList<String> commentsList = new ArrayList<>();

                        // Stores comments into commentlist to be retrieved
//                        if (snapshot.child("commentsList").hasChildren()) {
//                            for (DataSnapshot commentSnapshot : snapshot.child("commentsList").getChildren()) {
//                                commentsList.add(commentSnapshot.getValue().toString());
//                            }
//                        }
                        ArrayList<String> likedUsers = new ArrayList<>();

                        // Stores uid of user who liked the blog
                        if (snapshot.child("likedUsersList").hasChildren()) {
                            for (DataSnapshot likedUsersSnapshot : snapshot.child("likedUsersList").getChildren()) {
                                if (likedUsersSnapshot.getValue().toString().equals("true") && !likedUsers.contains(likedUsersSnapshot.getKey())) {
                                    likedUsers.add(likedUsersSnapshot.getKey());
                                }
                            }
                        }

                        BlogObject blogObject = new BlogObject(description, location, imgID, blogID, likes, comments, likedUsers);
                        blogsList.add(blogObject);

                        TextView noBlogMsg = findViewById(R.id.noBlogMsg);

                        noBlogMsg.setVisibility(View.GONE);

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

//                        if (snapshot.child("commentsList").hasChildren()) {
//                            for (DataSnapshot commentSnapshot : snapshot.child("commentsList").getChildren()) {
//                                commentsList.add(commentSnapshot.getValue().toString());
//                            }
//                        }

                        ArrayList<String> likedUsers = new ArrayList<>();

                        if (snapshot.child("likedUsersList").hasChildren()) {
                            for (DataSnapshot likedUsersSnapshot : snapshot.child("likedUsersList").getChildren()) {
                                if (likedUsersSnapshot.getValue().toString().equals("true") && !likedUsers.contains(likedUsersSnapshot.getKey())) {
                                    likedUsers.add(likedUsersSnapshot.getKey());
                                }
                            }
                        }

                        existingBlog.setDescription(description);
                        existingBlog.setImgID(imgID);
                        existingBlog.setLocation(location);
                        existingBlog.setLikes(likes);
                        existingBlog.setComments(comments);
                        //existingBlog.setCommentsList(commentsList);
                        existingBlog.setLikedUsers(likedUsers);

                        blogAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                int index = blogIDList.indexOf(snapshot.getKey());
                blogsList.remove(index);
                blogIDList.remove(index);
                blogAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error.getMessage());
            }
        });

    }

    public void loadingScreen(){
        loadingBlogDialog = new LoadingBlogDialog(BlogActivity.this);
        loadingBlogDialog.startLoadingDialog("Fetching blogs..");
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingBlogDialog.dismissDialog();
            }
        }, 1500);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainMenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

}