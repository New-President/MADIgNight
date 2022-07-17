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
import java.util.Timer;

import sg.edu.np.ignight.Objects.Comment;
import sg.edu.np.ignight.Objects.TimestampObject;
import sg.edu.np.ignight.Objects.UserObject;
import sg.edu.np.ignight.ProfileViewActivity;
import sg.edu.np.ignight.R;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder> {
    private TextView commentContent, timestamp, commenterUsername;
    private ImageView commenterProfPic;
    private Context c;
    private ArrayList<Comment> commentList;

    public CommentsAdapter(Context c, ArrayList<Comment> commmentList){
        this.c = c;
        this.commentList = commmentList;
    }

    @NonNull
    @Override
    public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_layout, parent, false);
        return new CommentsViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentsViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        String targetUid = comment.getUid();
        commentContent.setText(comment.getContent());
        commenterUsername.setText(comment.getUsername());

        try {
            TimestampObject timestampObject = new TimestampObject(comment.getTimestamp());
            timestamp.setText(timestampObject.getDate());

        } catch (ParseException e) {
            e.printStackTrace();
        }

        Glide.with(c)
                .load(comment.getProfUrl()).placeholder(R.drawable.failed)
                .into(commenterProfPic);

        commenterProfPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewUserProfile(targetUid);
            }
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }


    public class CommentsViewHolder extends RecyclerView.ViewHolder {

        public CommentsViewHolder(@NonNull View itemView) {
            super(itemView);
            commentContent = itemView.findViewById(R.id.commentContent);
            timestamp = itemView.findViewById(R.id.commentTimestamp);
            commenterProfPic = itemView.findViewById(R.id.commenterProfPic);
            commenterUsername = itemView.findViewById(R.id.commentUsername);
        }
    }

    public void viewUserProfile(String targetUid){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("user")
                .child(targetUid);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String ownUID = auth.getUid();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> dateLocList = new ArrayList<>();
                ArrayList<String> interestList = new ArrayList<>();

                String uid = snapshot.getKey();

                if (!uid.equals(ownUID)){
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

                    UserObject user = new UserObject(uid, aboutMe, age, dateLocList, gender, genderPref, interestList, profilePicUrl, relationshipPref, phone, profileCreated, username);

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
