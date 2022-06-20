package sg.edu.np.ignight.Menu;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import sg.edu.np.ignight.ChatActivity;
import sg.edu.np.ignight.ProfileViewActivity;
import sg.edu.np.ignight.R;
import sg.edu.np.ignight.Objects.UserObject;

public class MainMenuAdapter extends RecyclerView.Adapter<MainMenuViewHolder>{
    private ArrayList<UserObject> data;
    private Context c;
    private LinearLayoutManager layoutManager;

    public MainMenuAdapter(Context c, ArrayList<UserObject> data, LinearLayoutManager layoutManager){
        this.c =c;
        this.data = data;
        this.layoutManager = layoutManager;
    }

    @NonNull
    @Override
    public MainMenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_menu_layout, parent, false );
        return new MainMenuViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull MainMenuViewHolder holder, int position) {
        UserObject user = data.get(position);

        Log.d("menuuusername", user.getUsername());
        Log.d("menuuid", user.toString());
        holder.Name.setText(user.getUsername());
        Button next = holder.Reject;
        Button ignight = holder.Accept;
        ImageView profile = holder.ProfilePic_menu;

        next.setOnClickListener(new View.OnClickListener() {  // goes to the next user in the list or first user (if currently showing user is the last user)
            @Override
            public void onClick(View view) {
                if (holder.getAdapterPosition() != data.size() - 1) {
                    layoutManager.scrollToPosition(holder.getAdapterPosition() + 1);
                }
                else {
                    layoutManager.scrollToPosition(0);
                }

                Toast.makeText(c,"Thank you for your feedback! we will try not to recommend you this type of people next time!",Toast.LENGTH_SHORT ).show();
            }
        });

        ignight.setOnClickListener(new View.OnClickListener() { // start chat with the target user
            @Override
            public void onClick(View view) {

                DatabaseReference chatDB = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("chat");

                String currentUserUID = FirebaseAuth.getInstance().getUid();
                String targetUserUID = user.getUid();

                chatDB.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean chatExists = false;
                        ArrayList<String> usersInChat = new ArrayList<>();

                        for (DataSnapshot chatIdSnapshot : snapshot.getChildren()) {
                            for (DataSnapshot userIdSnapshot : chatIdSnapshot.child("users").getChildren()) {
                                usersInChat.add(userIdSnapshot.getKey());
                            }

                            if (usersInChat.contains(currentUserUID) && usersInChat.contains(targetUserUID)) {
                                chatExists = true;
                                break;
                            }
                        }

                        if (!chatExists) {
                            String newChatID = chatDB.push().getKey();
                            Map userMap = new HashMap<>();
                            userMap.put(currentUserUID, true);
                            userMap.put(targetUserUID, true);

                            chatDB.child(newChatID).child("users").updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(view.getContext(), ChatActivity.class);
                                        Bundle bundle = new Bundle();
                                        bundle.putString("chatID", newChatID);
                                        bundle.putString("chatName", user.getUsername());
                                        bundle.putString("targetUserID", targetUserUID);
                                        intent.putExtras(bundle);
                                        view.getContext().startActivity(intent);
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "onCancelled: " + error.getMessage());
                    }
                });
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {// go to profile view
            @Override
            public void onClick(View view) {
                Intent mainmenu_to_profileview = new Intent(c , ProfileViewActivity.class);
                mainmenu_to_profileview.putExtra("user", user);
                mainmenu_to_profileview.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                c.startActivity(mainmenu_to_profileview);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

}
