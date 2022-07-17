package sg.edu.np.ignight;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class AboutUsViewHolder extends RecyclerView.ViewHolder{
    ImageView pic;
    TextView nme;
    TextView desc;
    public AboutUsViewHolder(View item) {
        super(item);
        pic = item.findViewById(R.id.dev_pic);
        nme = item.findViewById(R.id.name_about_us);
        desc = item.findViewById(R.id.description_about_us);
    }
}
