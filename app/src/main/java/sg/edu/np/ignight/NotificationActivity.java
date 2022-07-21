package sg.edu.np.ignight;

import static android.content.ContentValues.TAG;
import static sg.edu.np.ignight.Notification.CHANNEL_1_ID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.hardware.ConsumerIrManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

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
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        database = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/");
        databaseBlogReference = database.getReference("user").child(Unique).child("blog");
        databaseUserReference = database.getReference("user");

        data = new ArrayList<>();
        likedUsers = new ArrayList<>();
        userList = new ArrayList<>();

        getBlogList();
        getUserList();

        initRecyclerView();

        /*editTextTitle = findViewById(R.id.edit_text_title);
        editTextMessage = findViewById(R.id.edit_text_message);*/
    }

    public void initRecyclerView(){
        RecyclerView rv = findViewById(R.id.notificationRecyclerView);
        NotificationAdapter adapter = new NotificationAdapter(NotificationActivity.this, likedUsers, userList, data);
        LinearLayoutManager layout = new LinearLayoutManager(this);

        rv.setAdapter(adapter);
        rv.setLayoutManager(layout);
    }

    private void getBlogList() {

        blogIDList = new ArrayList<>();
        databaseBlogReference.addChildEventListener(new ChildEventListener() {

            // Updates changes to the activity when a change is made in the database
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                if (snapshot.exists()) {
                    if (!blogIDList.contains(snapshot.getKey())) {
                        Log.d(TAG, "Hello: " + snapshot.getKey());
                        blogIDList.add(snapshot.getKey());
                        String blogID = snapshot.child("blogID").getValue().toString();
                        String description = snapshot.child("description").getValue().toString();
                        String imgID = snapshot.child("imgID").getValue().toString();
                        String location = snapshot.child("location").getValue().toString();
                        int likes = Integer.parseInt(snapshot.child("likes").getValue().toString());
                        int comments = Integer.parseInt(snapshot.child("comments").getValue().toString());

                        ArrayList<String> commentsList = new ArrayList<>();


                        // Stores uid of user who liked the blog

                        if (snapshot.child("likedUsersList").hasChildren()) {
                            for (DataSnapshot likedUsersSnapshot : snapshot.child("likedUsersList").getChildren()) {
                                if (likedUsersSnapshot.getValue().toString().equals("true") && !likedUsers.contains(likedUsersSnapshot.getKey())) {
                                    likedUsers.add(likedUsersSnapshot.getKey());
                                }
                            }
                        }

                        BlogObject blogObject = new BlogObject(description, location, imgID, blogID, likes, comments, likedUsers);
                        data.add(blogObject);

                        /*NotificationAdapter.notifyDataSetChanged();*/
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                for (BlogObject existingBlog : data) {
                    if (existingBlog.blogID.equals(snapshot.getKey())) {

                        String description = snapshot.child("description").getValue().toString();
                        String imgID = snapshot.child("imgID").getValue().toString();
                        String location = snapshot.child("location").getValue().toString();
                        int likes = Integer.parseInt(snapshot.child("likes").getValue().toString());
                        int comments = Integer.parseInt(snapshot.child("comments").getValue().toString());

                        likedUsers = new ArrayList<>();

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
                        existingBlog.setLikedUsers(likedUsers);

                        /*blogAdapter.notifyDataSetChanged();*/
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                int index = blogIDList.indexOf(snapshot.getKey());
                data.remove(index);
                blogIDList.remove(index);
                /*blogAdapter.notifyDataSetChanged();*/
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error.getMessage());
            }


        });
    }

    public void getUserList() {
        ValueEventListener getUserListListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {

                        boolean exists = false;

                        if (!childSnapshot.getKey().equals(FirebaseAuth.getInstance().getUid())) {
                            for (UserObject existingUser : userList) {
                                if (childSnapshot.getKey().equals(existingUser.getUid())) {
                                    exists = true;
                                    break;
                                }
                            }

                            if (exists || childSnapshot.child("profileCreated").getValue().toString().equals("false")) {
                                continue;
                            }

                            String uid = childSnapshot.getKey();
                            ArrayList<String> dateLocList = new ArrayList<>();
                            ArrayList<String> interestList = new ArrayList<>();

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

                            userList.add(user);


/*
                            userList.add(user);
                            userListAdapter.notifyDataSetChanged();
*/

                        }
                    }
                }

                Log.e(TAG, "Hello1: " + userList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error.getMessage());
            }
        };


        databaseUserReference.addValueEventListener(getUserListListener);
    }

    /*public void sendOnChannel1(View v){
        String title = editTextTitle.getText().toString();
        String message =editTextMessage.getText().toString();

        android.app.Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_android_blacknoti)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();

        notificationManager.notify(1,notification);

    }*/
}