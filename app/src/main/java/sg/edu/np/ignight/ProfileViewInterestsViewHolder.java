package sg.edu.np.ignight;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ProfileViewInterestsViewHolder extends RecyclerView.ViewHolder {
    TextView interestButton;
    public ProfileViewInterestsViewHolder(@NonNull View itemView) {
        super(itemView);

        interestButton = itemView.findViewById(R.id.interestButton);
    }
}
