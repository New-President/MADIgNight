package sg.edu.np.ignight.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Map;

import sg.edu.np.ignight.R;

public class MapAdapter extends RecyclerView.Adapter<MapAdapter.MapViewHolder>{

    private ArrayList<LocationObject> locationsList;
    private Context c;

    public MapAdapter(ArrayList<LocationObject> locationsList, Context c){
        this.locationsList = locationsList;
        this.c = c;
    }

    @NonNull
    @Override
    public MapViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.map_layout, parent, false);
        return new MapViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull MapViewHolder holder, int position) {
        LocationObject location = locationsList.get(position);

    }

    @Override
    public int getItemCount() {
        return locationsList.size();
    }

    public class MapViewHolder extends RecyclerView.ViewHolder {
        TextView desc, location;
        View viewItem;
        TextView likes, comments;
        ImageView commentButton, likesButton, blogImg, editBlogButton;

        public MapViewHolder(View item) {
            super(item);
            desc = item.findViewById(R.id.blogDesc);
            location = item.findViewById(R.id.location);
            blogImg = item.findViewById(R.id.blogImg);
            likes = item.findViewById(R.id.likesCount);
            comments = item.findViewById(R.id.commentsCount);
            commentButton = item.findViewById(R.id.commentButton);
            likesButton = item.findViewById(R.id.likeButton);
            editBlogButton = item.findViewById(R.id.editBlogButton);
            viewItem = item;
        }
    }
}

