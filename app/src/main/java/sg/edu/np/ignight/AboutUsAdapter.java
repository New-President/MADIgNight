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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_about_us, parent, false);
        return new AboutUsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AboutUsViewHolder holder, int position) {
        AboutUs content = data.get(position);
        holder.name.setText(content.Name);
        holder.description.setText(content.Description);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
