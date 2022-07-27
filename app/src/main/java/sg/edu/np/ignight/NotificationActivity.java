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

        // NotificationHelper.displayNotification(this, "title", "body");

        TextView textView = findViewById(R.id.textViewToken);
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if(task.isSuccessful()){
                            String token = task.getResult();
                            DatabaseReference nested = databaseUserReference.child(FirebaseAuth.getInstance().getUid());
                            nested.child("fcmToken").setValue(token);
                            Toast.makeText(NotificationActivity.this, "token saved", Toast.LENGTH_SHORT).show();
                            textView.setText("Token: " + token);
                        }
                        else{
                            textView.setText(task.getException().getMessage());
                        }
                    }
                });

    }


    public void initRecyclerView(){
        RecyclerView rv = findViewById(R.id.notificationRecyclerView);
        NotificationAdapter adapter = new NotificationAdapter(NotificationActivity.this, likedCommentList, userList, data);
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
                        Log.d("TAG", "Hello" + snapshot.getKey());
                        String blogID = snapshot.child("blogID").getValue().toString();
                        String description = snapshot.child("description").getValue().toString();
                        String imgID = snapshot.child("imgID").getValue().toString();
                        String location = snapshot.child("location").getValue().toString();
                        int likes = Integer.parseInt(snapshot.child("likes").getValue().toString());
                        int comments = Integer.parseInt(snapshot.child("comments").getValue().toString());

                        /*for (DataSnapshot dateLocSnapshot : childSnapshot.child("Date Location").getChildren()) {
                            dateLocList.add(dateLocSnapshot.getValue().toString());
                        }*/

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

                        /*if (!commentsList.contains(commentSnapshot.getKey())){
                                    String commentKey = commentSnapshot.getKey();
                                    commentsList.add(commentKey);
                                    String userUID = snapshot.child("commentList").child(commentKey).child("uid").getValue().toString();
                                    String content = snapshot.child("commentList").child(commentKey).child("content").getValue().toString();
                                    LikedCommentObject tempLCO = new LikedCommentObject(userUID, false, content);
                                    Log.d("TAG", "Hello: " + userUID);
                                    likedCommentList.add(tempLCO);
                                }*/
                               /*commentsList.add(commentSnapshot.getKey());
                               Log.d("TAG", "Hello: " + commentSnapshot.getKey());*/


                        // Stores uid of user who liked the blog
                        if (snapshot.child("likedUsersList").hasChildren()) {
                            for (DataSnapshot likedUsersSnapshot : snapshot.child("likedUsersList").getChildren()) {
                                Log.d("TAG", "Hello1 " + !likedUsers.contains(likedUsersSnapshot.getKey()));
                                if (likedUsersSnapshot.getValue().toString().equals("true")) {
                                    likedUsers.add(likedUsersSnapshot.getKey());
                                    LikedCommentObject tempLCO = new LikedCommentObject(likedUsersSnapshot.getKey(), imgID, true, "");
                                    if (!likedUsersSnapshot.getKey().equals(Unique)){
                                        likedCommentList.add(tempLCO);
                                    }
                                }
                            }
                        }
                        Log.d("TAG", "Hello" + likedCommentList.size());

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