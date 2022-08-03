package sg.edu.np.ignight.Blog;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.util.ArrayList;

import sg.edu.np.ignight.Objects.Comment;
import sg.edu.np.ignight.Objects.TimestampObject;
import sg.edu.np.ignight.Objects.UserObject;
import sg.edu.np.ignight.ProfileViewActivity;
import sg.edu.np.ignight.R;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder> {

    private Context c;
    private ArrayList<Comment> commentList;
    private String blogID;
    private String blogOwnerUID;

    public CommentsAdapter(Context c, ArrayList<Comment> commmentList, String blogID, String blogOwnerUID){
        this.c = c;
        this.commentList = commmentList;
        this.blogID = blogID;
        this.blogOwnerUID = blogOwnerUID;
    }

    @NonNull
    @Override
    public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_layout, parent, false);
        return new CommentsViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentsViewHolder holder, int position) {
        TextView commentContent = holder.commentContent;
        TextView username = holder.commenterUsername;
        TextView timestampView = holder.timestamp;
        TextView likesCount = holder.likesCount;
        ImageView commenterProfPic = holder.commenterProfPic;
        ImageView likeCommentBtn = holder.likeCommentBtn;

        String uid = FirebaseAuth.getInstance().getUid();
        Comment comment = commentList.get(position);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("user").child(blogOwnerUID)
                .child("blog").child(blogID).child("commentList").child(comment.getCommentID());

        // Retrieve user details
        String targetUid = comment.getUid();
        DatabaseReference commenterReference = FirebaseDatabase.getInstance().getReference("user").child(targetUid);

        commenterReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("username").getValue().toString();
                username.setText(name);
                String url = snapshot.child("profileUrl").getValue().toString();
                // Set profile picture of commenter
                Glide.with(c)
                        .load(url).placeholder(R.drawable.failed)
                        .into(commenterProfPic);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        commentContent.setText(comment.getContent());
        likesCount.setText(String.valueOf(comment.getLikes()));

        try {
            TimestampObject timestampObject = new TimestampObject(comment.getTimestamp());
            timestampView.setText(timestampObject.getDate());

        } catch (ParseException e) {
            e.printStackTrace();
        }

        commenterProfPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewUserProfile(targetUid);
            }
        });

        if (comment.getLikedUsersList().contains(uid)){
            likeCommentBtn.setBackgroundResource(R.drawable.heart);
        }
        else{
            likeCommentBtn.setBackgroundResource(R.drawable.heartwithhole);
        }

        // Update booleans stored in Firebase that determine the state of liking of the comment
        likeCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int likes = comment.getLikes();
                if (comment.getLikedUsersList().contains(uid)) {
                    likes = ((likes - 1) <= 0)?0:(likes -= 1);
                    databaseReference.child("likedUsersList").child(uid).setValue(false);
                    databaseReference.child("likes").setValue(likes);
                    likesCount.setText(String.valueOf(likes));
                    likeCommentBtn.setBackgroundResource(R.drawable.heartwithhole);
                    comment.getLikedUsersList().remove(uid);
                }
                else {
                    likes += 1;
                    databaseReference.child("likedUsersList").child(uid).setValue(true);
                    databaseReference.child("likes").setValue(likes);
                    likesCount.setText(String.valueOf(likes));
                    likeCommentBtn.setBackgroundResource(R.drawable.heart);
                    comment.getLikedUsersList().add(uid);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }


    public class CommentsViewHolder extends RecyclerView.ViewHolder {
        public TextView commentContent, timestamp, commenterUsername, likesCount;
        public ImageView commenterProfPic, likeCommentBtn;
        public CommentsViewHolder(@NonNull View itemView) {
            super(itemView);
            commentContent = itemView.findViewById(R.id.commentContent);
            timestamp = itemView.findViewById(R.id.commentTimestamp);
            commenterProfPic = itemView.findViewById(R.id.commenterProfPic);
            commenterUsername = itemView.findViewById(R.id.commentUsername);
            likeCommentBtn = itemView.findViewById(R.id.likeCommentBtn);
            likesCount = itemView.findViewById(R.id.commentLikes);
        }
    }

    public void viewUserProfile(String targetUid){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("user")
                .child(targetUid);

        // Creates the user object to view their profile using their UID
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> dateLocList = new ArrayList<>();
                ArrayList<String> interestList = new ArrayList<>();

                String uid = FirebaseAuth.getInstance().getUid();

                if (!uid.equals(targetUid)){
                    String phone = snapshot.child("phone").getValue().toString();
                    String aboutMe = snapshot.child("About Me").getValue().toString();
                    String gender = snapshot.child("Gender").getValue().toString();
                    String genderPref = snapshot.child("Gender Preference").getValue().toString();
                    String profilePicUrl = snapshot.child("profileUrl").getValue().toString();
                    String relationshipPref = snapshot.child("Relationship Preference").getValue().toString();
                    String username = snapshot.child("username").getValue().toString();
                    String profileCreated = snapshot.child("profileCreated").getValue().toString();
                    int age = Integer.parseInt(snapshot.child("Age").getValue().toString());

                    for (DataSnapshot dateLocSnapshot : snapshot.child("Date Location").getChildren()) {
                        dateLocList.add(dateLocSnapshot.getValue().toString());
                    }
                    for (DataSnapshot interestSnapshot : snapshot.child("Interest").getChildren()) {
                        interestList.add(interestSnapshot.getValue().toString());
                    }

                    UserObject user = new UserObject(targetUid, aboutMe, age, dateLocList, gender, genderPref, interestList, profilePicUrl, relationshipPref, phone, profileCreated, username);

                    Intent viewUser = new Intent(c, ProfileViewActivity.class);
                    viewUser.putExtra("user", user);
                    c.startActivity(viewUser);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("CommentUserLookup", "Failed to load user profile");
            }
        });
    }
}
