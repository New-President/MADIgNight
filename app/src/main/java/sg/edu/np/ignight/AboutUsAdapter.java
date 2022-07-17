package sg.edu.np.ignight;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AboutUsAdapter extends RecyclerView.Adapter<AboutUsViewHolder>{
    ArrayList<AboutUs> data;
    Context c;
    public AboutUsAdapter(ArrayList<AboutUs> data, Context c) {
        this.data = data;
        this.c = c;
    }

    @NonNull
    @Override
    public AboutUsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item;
        item = LayoutInflater.from(parent.getContext()).inflate(R.layout.about_us_recycler, null, false);
        return new AboutUsViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull AboutUsViewHolder holder, int position) {
        AboutUs content = data.get(position);
        holder.nme.setText(content.Name);
        holder.desc.setText(content.Description);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
