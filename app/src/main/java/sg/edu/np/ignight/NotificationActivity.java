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
        NotificationAdapter adapter = new NotificationAdapter(NotificationActivity.this, data, userList);
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

    public void getUserList(){
        ArrayList<String> interestList = new ArrayList<>();
        ArrayList<String> dateLocList = new ArrayList<>();
        databaseUserReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String uid = snapshot.getKey();
                    Log.e(TAG, "Hello: " + snapshot.child(uid).child("profileCreated").getValue());
                    String profileCreated = snapshot.child(uid).child("phone").getValue().toString();

                    if (profileCreated.equals("true")){
                        String phone = snapshot.child(uid).child("phone").getValue().toString();
                        String username = snapshot.child(uid).child("username").getValue().toString();
                        String gender = snapshot.child(uid).child("Gender").getValue().toString();
                        String aboutMe = snapshot.child(uid).child("About Me").getValue().toString();
                        String relationshipPref = snapshot.child(uid).child("Relationship Preference").getValue().toString();
                        String genderPref = snapshot.child(uid).child("Gender Preference").getValue().toString();
                        String profilePicUrl = snapshot.child(uid).child("profileUrl").getValue().toString();
                        int age = (int) snapshot.child(uid).child("username").getValue();

                        long interestCount = snapshot.child(uid).child("Interest").getChildrenCount();
                        for (int i = 1; i <= interestCount; i++) {
                            String interest = snapshot.child("Interest").child("Interest" + i).getValue(String.class);
                            // Add into interestList
                            interestList.add(interest);
                        }

                        long dateLocCount = snapshot.child(uid).child("Date Location").getChildrenCount();
                        for (int i = 1; i <= dateLocCount; i++) {
                            String dateLoc = snapshot.child("Date Location").child("Date Location" + i).getValue(String.class);
                            // Add into dateLocList
                            dateLocList.add(dateLoc);
                        }
                        UserObject user = new UserObject(uid, aboutMe, age, dateLocList, gender, genderPref, interestList, profilePicUrl, relationshipPref, phone, "True", username );
                        userList.add(user);
                        Log.e(TAG, "Hello: " + userList.size());

                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                int index = userList.indexOf(snapshot.getKey());
                userList.remove(index);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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