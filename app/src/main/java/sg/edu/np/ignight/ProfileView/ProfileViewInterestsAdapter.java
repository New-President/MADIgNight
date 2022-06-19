package sg.edu.np.ignight.ProfileView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import sg.edu.np.ignight.R;

public class ProfileViewInterestsAdapter
        extends RecyclerView.Adapter<ProfileViewInterestsViewHolder> {
    public static ArrayList<String> data;
    Context context;
    public ProfileViewInterestsAdapter(Context context, ArrayList<String> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public ProfileViewInterestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.interest_layout, parent, false);
        return new ProfileViewInterestsViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewInterestsViewHolder holder, int position) {
        String context = data.get(position);
        holder.textView3.setText(context);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
