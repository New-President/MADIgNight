package sg.edu.np.ignight;

import static android.content.ContentValues.TAG;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceIdReceiver;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;

import sg.edu.np.ignight.Objects.BlogObject;
import sg.edu.np.ignight.Objects.UserObject;

public class NotificationActivity extends AppCompatActivity {
    private NotificationManagerCompat notificationManager;

    private ImageView backButton;
    private DatabaseReference databaseBlogReference;
    private DatabaseReference databaseUserReference;
    private FirebaseDatabase database;

    public ArrayList<BlogObject> data = new ArrayList<>();
    public ArrayList<String> blogIDList = new ArrayList<>();
    public ArrayList<String> likedUsers = new ArrayList<>();
    public ArrayList<UserObject> userList = new ArrayList<>();
    public ArrayList<LikedCommentObject> likedCommentList = new ArrayList<>();

    public String phone, username, gender, aboutMe, relationshipPref, genderPref, profilePicUrl;
    public int age;


    // Get the current user
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    //get the current user's UID
    String Unique = user.getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        notificationManager = NotificationManagerCompat.from(this);

        backButton = findViewById(R.id.notificationBackBtn);
        // Back button to go back to main activity
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        database = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/");
        databaseBlogReference = database.getReference("user").child(Unique).child("blog");
        databaseUserReference = database.getReference("user");

        getBlogList();
        getUserList();

        initRecyclerView();

    }


    // initialize the recycler view in the notification activity
    public void initRecyclerView(){
        RecyclerView rv = findViewById(R.id.notificationRecyclerView);
        NotificationAdapter adapter = new NotificationAdapter(NotificationActivity.this, likedCommentList, userList, data);
        LinearLayoutManager layout = new LinearLayoutManager(this);

        rv.setAdapter(adapter);
        rv.setLayoutManager(layout);
    }

    // Get all the existing blog list
    private void getBlogList() {

        blogIDList = new ArrayList<>();
        databaseBlogReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // When there is a blog node in the user database
                if (snapshot.exists()) {
                    // check that we have not added the BlogIDList into the blogIDList
                    if (!blogIDList.contains(snapshot.getKey())) {
                        // add the new blogID into the blogIDList
                        blogIDList.add(snapshot.getKey());
                        // Get the values related to the blog
                        String blogID = snapshot.child("blogID").getValue().toString();
                        String description = snapshot.child("description").getValue().toString();
                        String imgID = snapshot.child("imgID").getValue().toString();
                        String location = snapshot.child("location").getValue().toString();
                        int likes = Integer.parseInt(snapshot.child("likes").getValue().toString());
                        int comments = Integer.parseInt(snapshot.child("comments").getValue().toString());

                        ArrayList<String> commentsList = new ArrayList<>();
                        // Stores comments into commentlist to be retrieved
                        //If there are any comments in the blog
                        if (snapshot.child("commentList").hasChildren()) {
                            // Add each of the comment into the comment List
                            for (DataSnapshot commentSnapshot : snapshot.child("commentList").getChildren()) {
                                commentsList.add(commentSnapshot.getKey());
                            }
                        }

                        for (int i = 0; i < commentsList.size(); i++){
                            String commentKey = commentsList.get(i);
                            // Get the uid of the person who made the comment and the content of the comment
                            String userUID = snapshot.child("commentList").child(commentKey).child("uid").getValue().toString();
                            String content = snapshot.child("commentList").child(commentKey).child("content").getValue().toString();
                            LikedCommentObject tempLCO = new LikedCommentObject(userUID, imgID,false, content);
                            // Store the comment into a likedCommentList where all the comments and all the likes the user received for a certain blog
                            if (!userUID.equals(Unique)){
                                likedCommentList.add(tempLCO);
                            }
                        }

                        // Stores uid of user who liked the blog
                        if (snapshot.child("likedUsersList").hasChildren()) {
                            for (DataSnapshot likedUsersSnapshot : snapshot.child("likedUsersList").getChildren()) {
                                // if the user have any liked blog
                                if (likedUsersSnapshot.getValue().toString().equals("true")) {
                                    // add the person's uid as he liked the photo
                                    likedUsers.add(likedUsersSnapshot.getKey());
                                    LikedCommentObject tempLCO = new LikedCommentObject(likedUsersSnapshot.getKey(), imgID, true, "");
                                    // If the person who liked the blog is not the own user
                                    if (!likedUsersSnapshot.getKey().equals(Unique)){
                                        // Store the comment into a likedCommentList where all the comments and all the likes the user received for a certain blog
                                        likedCommentList.add(tempLCO);
                                    }
                                }
                            }
                        }

                        BlogObject blogObject = new BlogObject(description, location, imgID, blogID, likes, comments, likedUsers);
                        // add all the blog created by the user into a list
                        data.add(blogObject);

                    }
                }
            }

            // if there are changes made to the blog
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                for (BlogObject existingBlog : data) {
                    if (existingBlog.blogID.equals(snapshot.getKey())) {

                        String description = snapshot.child("description").getValue().toString();
                        String imgID = snapshot.child("imgID").getValue().toString();
                        String location = snapshot.child("location").getValue().toString();
                        int likes = Integer.parseInt(snapshot.child("likes").getValue().toString());
                        int comments = Integer.parseInt(snapshot.child("comments").getValue().toString());

                        ArrayList<String> commentsList = new ArrayList<>();
                        // Stores comments into commentlist to be retrieved
                        if (snapshot.child("commentList").hasChildren()) {
                            for (DataSnapshot commentSnapshot : snapshot.child("commentList").getChildren()) {
                                commentsList.add(commentSnapshot.getKey());
                            }
                        }

                        for (int i = 0; i < commentsList.size(); i++){
                            String commentKey = commentsList.get(i);
                            String userUID = snapshot.child("commentList").child(commentKey).child("uid").getValue().toString();
                            String content = snapshot.child("commentList").child(commentKey).child("content").getValue().toString();
                            LikedCommentObject tempLCO = new LikedCommentObject(userUID, imgID,false, content);
                            if (!userUID.equals(Unique)){
                                likedCommentList.add(tempLCO);
                            }
                        }


                        likedUsers = new ArrayList<>();

                        if (snapshot.child("likedUsersList").hasChildren()) {
                            for (DataSnapshot likedUsersSnapshot : snapshot.child("likedUsersList").getChildren()) {
                                if (likedUsersSnapshot.getValue().toString().equals("true")) {
                                    likedUsers.add(likedUsersSnapshot.getKey());
                                    LikedCommentObject tempLCO = new LikedCommentObject(likedUsersSnapshot.getKey(), imgID, true, "");
                                    if (!likedUsersSnapshot.getKey().equals(Unique)){
                                        likedCommentList.add(tempLCO);
                                    }
                                }
                            }
                        }

                        existingBlog.setDescription(description);
                        existingBlog.setImgID(imgID);
                        existingBlog.setLocation(location);
                        existingBlog.setLikes(likes);
                        existingBlog.setComments(comments);
                        existingBlog.setLikedUsers(likedUsers);

                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                int index = blogIDList.indexOf(snapshot.getKey());
                data.remove(index);
                blogIDList.remove(index);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error.getMessage());
            }


        });
    }

    // get all the existing user
    public void getUserList() {
        ValueEventListener getUserListListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        boolean exists = false;
                        // if the uid is not the current user's own uid
                        if (!childSnapshot.getKey().equals(FirebaseAuth.getInstance().getUid())) {
                            // ensure that the uid of the user is different
                            for (UserObject existingUser : userList) {
                                if (childSnapshot.getKey().equals(existingUser.getUid())) {
                                    exists = true;
                                    break;
                                }
                            }

                            // if the user have not created
                            if (exists || childSnapshot.child("profileCreated").getValue().toString().equals("false")) {
                                continue;
                            }

                            String uid = childSnapshot.getKey();
                            ArrayList<String> dateLocList = new ArrayList<>();
                            ArrayList<String> interestList = new ArrayList<>();

                            // Get all the details about the user
                            String phone = childSnapshot.child("phone").getValue().toString();
                            String aboutMe = childSnapshot.child("About Me").getValue().toString();
                            String gender = childSnapshot.child("Gender").getValue().toString();
                            String genderPref = childSnapshot.child("Gender Preference").getValue().toString();
                            String profilePicUrl = childSnapshot.child("profileUrl").getValue().toString();
                            String relationshipPref = childSnapshot.child("Relationship Preference").getValue().toString();
                            String username = childSnapshot.child("username").getValue().toString();
                            String profileCreated = childSnapshot.child("profileCreated").getValue().toString();
                            int age = Integer.parseInt(childSnapshot.child("Age").getValue().toString());

                            for (DataSnapshot dateLocSnapshot : childSnapshot.child("Date Location").getChildren()) {
                                dateLocList.add(dateLocSnapshot.getValue().toString());
                            }
                            for (DataSnapshot interestSnapshot : childSnapshot.child("Interest").getChildren()) {
                                interestList.add(interestSnapshot.getValue().toString());
                            }

                            UserObject user = new UserObject(uid, aboutMe, age, dateLocList, gender, genderPref, interestList, profilePicUrl, relationshipPref, phone, profileCreated, username);

                            // store all the existing users into a list
                            userList.add(user);

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error.getMessage());
            }
        };


        databaseUserReference.addValueEventListener(getUserListListener);
    }
}