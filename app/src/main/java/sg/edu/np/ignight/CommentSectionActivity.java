package sg.edu.np.ignight;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import sg.edu.np.ignight.Blog.CommentsAdapter;
import sg.edu.np.ignight.BlogNotification.SendCommentNotification;
import sg.edu.np.ignight.Objects.Comment;

public class CommentSectionActivity extends AppCompatActivity {
    private ArrayList<Comment> commentsList;
    private RecyclerView commmentRV;
    private RecyclerView.Adapter commentAdapter;
    private LinearLayoutManager commentLayoutManager;
    private DatabaseReference databaseReference;
    private FirebaseDatabase database;
    private ArrayList<String> commentIDList;

    private String blogOwnerUID, blogID, imgID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_section);

        Intent intent = getIntent();


        if(intent.getStringExtra("uid") != null){
            blogOwnerUID = intent.getStringExtra("uid");
            blogID = intent.getStringExtra("blogID");
            imgID = intent.getStringExtra("imgID");
        }else{
            // Get the data to start the commentSection activity from the notification
            Bundle bundle = getIntent().getExtras();
            blogOwnerUID = bundle.getString("blogOwnerUid");
            blogID = bundle.getString("blogID");
            imgID = bundle.getString("imgID");
        }




        // Retrieves uid who posted the blogs & id of blog
        /*blogOwnerUID = intent.getStringExtra("uid");*/


        int numOfComments = intent.getIntExtra("numOfComments", 0);



        ImageView sendCommentBtn = findViewById(R.id.sendCommentBtn);
        ImageView commentProfilePic = findViewById(R.id.commentProfilePic);
        ImageView blogImg = findViewById(R.id.commentBlogImg);
        ImageView backBtn = findViewById(R.id.commentBackButton);
        EditText commentInputField = findViewById(R.id.commentInputField);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/");
        databaseReference = database.getReference("user").child(blogOwnerUID).child("blog").child(blogID);
        DatabaseReference databaseSelf = database.getReference("user").child(auth.getUid());
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        getCommentsList();
        initRecyclerView(blogID, blogOwnerUID);

        // Display own profile picture beside comment input
        databaseSelf.child("profileUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String profileUrl = snapshot.getValue().toString();

                Glide.with(getApplicationContext())
                        .load(profileUrl).placeholder(R.drawable.failed)
                        .into(commentProfilePic);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("CommentPFPfail", "Failed to load profile picture");
            }
        });

        // Display blog image
        try{
            StorageReference storageReference = firebaseStorage.getReference("blog").child(blogOwnerUID).child(imgID);
            File localfile = File.createTempFile("tempfile", ".png");
            storageReference.getFile(localfile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(localfile.getAbsolutePath());
                    blogImg.setImageBitmap(bitmap);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CommentSectionActivity.this, "Failed to retrieve blogs", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (Exception ex){
            Log.d("Load Image Error", "Failed to load image");
        }

        // Send comment
        sendCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = commentInputField.getText().toString();

                String  timestamp = new Date().toString();
                String commentID = databaseReference.push().getKey();

                databaseReference.child("comments").setValue(numOfComments + 1);

                pushNotification(uid ,blogID, content, blogOwnerUID);

                databaseSelf.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String username = snapshot.child("username").getValue().toString();
                        String profUrl = snapshot.child("profileUrl").getValue().toString();

                        Comment newComment = new Comment(commentID, auth.getUid(), username, profUrl, content, timestamp, new ArrayList<String>(), 0);
                        databaseReference.child("commentList").child(commentID).setValue(newComment);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                commentInputField.setText("");
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    // Initialize recyclerview with commentslist
    private void initRecyclerView(String blogID, String blogOwnerUID) {
        commentsList = new ArrayList<>();
        commmentRV = findViewById(R.id.commentsRV);
        commentAdapter = new CommentsAdapter(this, commentsList, blogID, blogOwnerUID);
        commmentRV.setNestedScrollingEnabled(false);
        commmentRV.setAdapter(commentAdapter);
        commentLayoutManager = new LinearLayoutManager(this);
        commentLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        commmentRV.setLayoutManager(commentLayoutManager);
    }

    // Retreives and creates comment objects stored in commentslist
    private void getCommentsList() {
        commentIDList = new ArrayList<>();

        databaseReference.child("commentList").addChildEventListener(new ChildEventListener() {

            // Updates changes to the activity when a change is made in the database
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                if (snapshot.exists()) {
                    if (!commentIDList.contains(snapshot.getKey())) {
                        commentIDList.add(snapshot.getKey());
                        String commentID = snapshot.child("commentID").getValue().toString();
                        String uid = snapshot.child("uid").getValue().toString();
                        String username = snapshot.child("username").getValue().toString();
                        String profUrl = snapshot.child("profUrl").getValue().toString();
                        String content = snapshot.child("content").getValue().toString();
                        String timestamp = snapshot.child("timestamp").getValue().toString();
                        int likes = Integer.parseInt(snapshot.child("likes").getValue().toString());

                        ArrayList<String> likedUsers = new ArrayList<String>();
                        for (DataSnapshot likedUID: snapshot.child("likedUsersList").getChildren()){
                            if (likedUID.exists() && (boolean) likedUID.getValue()){
                                likedUsers.add(likedUID.getKey());
                            }
                        }

                        Comment commentObj = new Comment(commentID, uid, username, profUrl, content, timestamp, likedUsers, likes);
                        commentsList.add(commentObj);


                        commentAdapter.notifyDataSetChanged();

                    }
                }

                Log.d("commentsSize", String.valueOf(commentsList.size()));
            }


            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                for (Comment existingComment : commentsList) {
                    if (existingComment.getCommentID().equals(snapshot.getKey())) {

                        String content = snapshot.child("content").getValue().toString();
                        String timestamp = snapshot.child("timestamp").getValue().toString();
                        int likes = Integer.parseInt(snapshot.child("likes").getValue().toString());
                        existingComment.setContent(content);
                        existingComment.setTimestamp(timestamp);
                        existingComment.setLikes(likes);
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                int index = commentIDList.indexOf(snapshot.getKey());
                commentIDList.remove(index);
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error.getMessage());
            }


        });
    }

    // send notification when other user comments on your blog
    private void pushNotification(String senderUID, String blogID, String message, String blogOwnerUID) {

        DatabaseReference myRef = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();

        // Get the owner of the blog fcmtoken
        myRef.child("user").child(blogOwnerUID).child("fcmToken").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String fcmToken = snapshot.getValue().toString();
                    SendCommentNotification sender = new SendCommentNotification(fcmToken, senderUID, message, blogID, getApplicationContext());
                    sender.sendNotification();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error.getMessage());
            }
        });
    }
}