package sg.edu.np.ignight.Chat;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import sg.edu.np.ignight.R;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> {

    private Context context;
    private ArrayList<String> mediaUriList;

    public MediaAdapter(Context context, ArrayList<String> mediaUriList) {
        this.context = context;
        this.mediaUriList = mediaUriList;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_media, null, false);

        return new MediaViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        Glide.with(context)
                .load(Uri.parse(mediaUriList.get(position)))
                .placeholder(R.drawable.ic_baseline_image_24)
                .into(holder.media);
    }

    @Override
    public int getItemCount() {
        return mediaUriList.size();
    }

    public class MediaViewHolder extends RecyclerView.ViewHolder {

        ImageView media;

        public MediaViewHolder(View itemView) {
            super(itemView);

            media = itemView.findViewById(R.id.media);
        }
    }
}
