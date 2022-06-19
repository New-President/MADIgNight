package sg.edu.np.ignight;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ProfileViewInterestsAdapter extends RecyclerView.Adapter<ProfileViewInterestsViewHolder> {
    ArrayList<UserObject> data;
    // takes in stored data. only the interests will be extracted from the class
    public ProfileViewInterestsAdapter(ArrayList<UserObject> data) { this.data = data;}

    @NonNull
    @Override
    public ProfileViewInterestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.interest_layout, null, false);
        return new ProfileViewInterestsViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewInterestsViewHolder holder, int position) {
        UserObject content = data.get(position);
        holder.interestButton.setText(content.Interest);

    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
