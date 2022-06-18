package sg.edu.np.ignight;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

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
        next.setOnClickListener(new View.OnClickListener() {// need algo to do
            @Override
            public void onClick(View view) {
                Toast.makeText(c,"Thank you for your feedback! we will try not to recommend you this type of people next time!",Toast.LENGTH_SHORT ).show();
            }
        });
        ignight.setOnClickListener(new View.OnClickListener() { // go to messaging directly
            @Override
            public void onClick(View view) {
                //Intent mainmenu_to_chat = new Intent(c , .class);
                //c.startActivity(mainmenu_to_chat);
            }
        });
        profile.setOnClickListener(new View.OnClickListener() {// go to profile view
            @Override
            public void onClick(View view) {
                Intent mainmenu_to_profileview = new Intent(c , ProfileViewActivity.class);
                c.startActivity(mainmenu_to_profileview);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
