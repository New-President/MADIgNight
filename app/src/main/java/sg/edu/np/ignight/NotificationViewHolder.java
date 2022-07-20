package sg.edu.np.ignight;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class NotificationViewHolder extends RecyclerView.ViewHolder {
    ImageView profile;
    TextView description;

    public NotificationViewHolder(View item){
        super(item);

        profile = item.findViewById(R.id.notificationImageView);
        description = item.findViewById(R.id.notificationComment);

    }
}
