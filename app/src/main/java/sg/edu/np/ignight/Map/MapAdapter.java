package sg.edu.np.ignight.Map;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
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
        holder.Name.setText(location.getName());
        holder.Category.setText(location.getCategory());

        Glide.with(c)
                .load(location.getImgUri())
                .placeholder(R.drawable.ic_baseline_image_24)
                .into(holder.LocImage);

        holder.ViewLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent viewloc = new Intent(c, ViewLocation.class);
                viewloc.putExtra("locationObject", location);
                c.startActivity(viewloc);
            }
        });
        Log.d("AdapterLocation", "onBindViewHolder: " + location.getName());
    }

    @Override
    public int getItemCount() {
        return locationsList.size();
    }

    public class MapViewHolder extends RecyclerView.ViewHolder {
        public TextView Name, Category;
        public ImageView LocImage, ViewLoc;
        public MapViewHolder(View item) {
            super(item);
            Name = item.findViewById(R.id.locName);
            Category = item.findViewById(R.id.locCat);
            LocImage = item.findViewById(R.id.locImage);
            ViewLoc = item.findViewById(R.id.viewLoc);
        }
    }
}

