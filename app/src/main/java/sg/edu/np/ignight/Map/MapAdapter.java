package sg.edu.np.ignight.Map;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Map;

import sg.edu.np.ignight.R;

public class MapAdapter extends RecyclerView.Adapter<MapAdapter.MapViewHolder>{
    private TextView Name, Category;
    private ImageView LocImage;

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
        Name.setText(location.getName());
        Category.setText(location.getCategory());

        Glide.with(c)
                .load(location.getImgUri())
                .placeholder(R.drawable.ic_baseline_image_24)
                .into(LocImage);

    }

    @Override
    public int getItemCount() {
        return locationsList.size();
    }

    public class MapViewHolder extends RecyclerView.ViewHolder {

        public MapViewHolder(View item) {
            super(item);
            Name = item.findViewById(R.id.locName);
            Category = item.findViewById(R.id.locCat);
            LocImage = item.findViewById(R.id.locImage);
        }
    }
}

