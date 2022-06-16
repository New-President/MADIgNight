package sg.edu.np.ignight;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class MainMenuViewHolder extends RecyclerView.ViewHolder{
    ImageView ProfilePic_menu;
    TextView Name;
    Button Reject;
    Button Accept;
    public MainMenuViewHolder(View item) {
        super(item);
        ProfilePic_menu = item.findViewById(R.id.profilepic_menu);
        Name = item.findViewById(R.id.name_placement);
        Reject = item.findViewById(R.id.Reject_btn);
        Accept = item.findViewById(R.id.IgNight_Btn);
    }
}
