package sg.edu.np.ignight.ProfileView;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import sg.edu.np.ignight.R;

public class ProfileViewInterestsViewHolder extends RecyclerView.ViewHolder {
    TextView textView3;
    public ProfileViewInterestsViewHolder(View item) {
        super(item);

        textView3 = (TextView) item.findViewById(R.id.textView3);
    }
}
