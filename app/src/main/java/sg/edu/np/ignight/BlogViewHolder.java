package sg.edu.np.ignight;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class BlogViewHolder extends RecyclerView.ViewHolder {
    TextView desc;
    TextView device;
    TextView likes;
    TextView location;
    TextView comments;
    ImageView blogImg;
    ImageView commentButton;
    ImageView likesButton;
    View viewItem;

    public BlogViewHolder(View item){
        super(item);
        desc = item.findViewById(R.id.blogDesc);
        device = item.findViewById(R.id.device);
        likes = item.findViewById(R.id.likesCount);
        comments = item.findViewById(R.id.commentsCount);
        location = item.findViewById(R.id.location);

        blogImg = item.findViewById(R.id.blogImg);
        commentButton = item.findViewById(R.id.commentButton);
        likesButton = item.findViewById(R.id.likeButton);
        viewItem = item;
    }

}
