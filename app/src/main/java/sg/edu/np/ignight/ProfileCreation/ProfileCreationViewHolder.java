package sg.edu.np.ignight.ProfileCreation;

import android.view.View;
import android.widget.Button;

import androidx.recyclerview.widget.RecyclerView;

import sg.edu.np.ignight.R;

public class ProfileCreationViewHolder extends RecyclerView.ViewHolder {
    Button interest;

    public ProfileCreationViewHolder(View item){
        super(item);

        interest = item.findViewById(R.id.InterestButton);
    }
}
