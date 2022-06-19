package sg.edu.np.ignight;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class ProfileCreationViewHolder extends RecyclerView.ViewHolder {
    Button interest;

    public ProfileCreationViewHolder(View item){
        super(item);

        interest = item.findViewById(R.id.Interest);
    }
}