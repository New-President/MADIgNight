package sg.edu.np.ignight;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainMenuAdapter extends RecyclerView.Adapter<MainMenuViewHolder>{
    public static ArrayList<User> data;
    Context c;

    public MainMenuAdapter(Context c, ArrayList<User> data){
        this.c =c;
        this.data = data;
    }

    @NonNull
    @Override
    public MainMenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_menu_layout, parent, false );
        return new MainMenuViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull MainMenuViewHolder holder, int position) {
        User user = data.get(position);
        holder.Name.setText(user.Username);
        Button next = holder.Reject;
        Button ignight = holder.Accept;
        ImageView profile = holder.ProfilePic_menu;
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        ignight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
