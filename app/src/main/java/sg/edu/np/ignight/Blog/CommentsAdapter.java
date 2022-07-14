package sg.edu.np.ignight.Blog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import sg.edu.np.ignight.Objects.Comment;
import sg.edu.np.ignight.R;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder> {
    private TextView commentContent, timestamp;
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
        commentContent.setText(comment.getContent());
        timestamp.setText(comment.getTimestamp().getTime());

        String profUri = comment.getUser().getProfilePicUrl();

        Glide.with(c.getApplicationContext())
                .load(profUri).placeholder(R.drawable.failed)
                .into(commenterProfPic);


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
        }
    }
}
