package sg.edu.np.ignight;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class AboutUsViewHolder extends RecyclerView.ViewHolder{
    TextView name;
    TextView description;
    public AboutUsViewHolder(View item) {
        super(item);
        name = item.findViewById(R.id.name_about_us);
        description = item.findViewById(R.id.description_about_us);
    }
}
