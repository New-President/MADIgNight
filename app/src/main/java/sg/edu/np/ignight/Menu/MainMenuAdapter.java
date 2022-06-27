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

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import sg.edu.np.ignight.ChatActivity;
import sg.edu.np.ignight.MainMenuActivity;
import sg.edu.np.ignight.ProfileViewActivity;
import sg.edu.np.ignight.R;
import sg.edu.np.ignight.Objects.UserObject;

public class MainMenuAdapter extends RecyclerView.Adapter<MainMenuViewHolder>{
    private ArrayList<UserObject> data;
    private Context c;
    private LinearLayoutManager layoutManager;
    private DatabaseReference myRef2;
    private FirebaseDatabase db;

    public MainMenuAdapter(Context c, ArrayList<UserObject> data, LinearLayoutManager layoutManager){
        this.c = c;
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
        db = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/");
        myRef2 = db.getReference("user");
        holder.Name.setText(user.getUsername());
        Button next = holder.Reject;
        Button ignight = holder.Accept;
        ImageView profile = holder.ProfilePic_menu;

        myRef2.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // get profile picture file name
                String profilePictureName = snapshot.child("Profile Picture").getValue().toString();
                StorageReference storageReference = FirebaseStorage.
                        getInstance().
                        getReference("profilePicture/" +
                                user.getUid() +
                                "/" +
                                profilePictureName);
                Glide.with(c.getApplicationContext())
                        .load(storageReference)
                        .into(profile);
                Log.d("test2", profilePictureName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // if there is an error retrieving profile pics, show toast
                Log.d("testError", "testing");
                Toast.makeText(c.getApplicationContext(),
                        "Error retrieving profile photo. Please try again later.",
                        Toast.LENGTH_LONG).show();
            }
        });

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
                DatabaseReference userDB = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("user");

                String currentUserUID = FirebaseAuth.getInstance().getUid();
                String targetUserUID = user.getUid();

                userDB.child(currentUserUID).child("chats").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean chatExists = false;
                        String existingChatID = "";

                        if (snapshot.exists()) {
                            for (DataSnapshot chatIdSnapshot : snapshot.getChildren()) {

                                if (chatIdSnapshot.getValue().toString().equals(targetUserUID)) {
                                    chatExists = true;
                                    existingChatID = chatIdSnapshot.getKey();
                                    break;
                                }
                            }
                        }

                        if (!chatExists) {
                            String newChatID = chatDB.push().getKey();
                            Map userMap = new HashMap<>();
                            userMap.put(currentUserUID, true);
                            userMap.put(targetUserUID, true);

                            userDB.child(currentUserUID).child("chats").child(newChatID).setValue(targetUserUID);
                            userDB.child(targetUserUID).child("chats").child(newChatID).setValue(currentUserUID);

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
                                    else {
                                        task.getException().printStackTrace();
                                    }
                                }
                            });
                        }
                        else {
                            Intent intent = new Intent(view.getContext(), ChatActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("chatID", existingChatID);
                            bundle.putString("chatName", user.getUsername());
                            bundle.putString("targetUserID", targetUserUID);
                            intent.putExtras(bundle);
                            view.getContext().startActivity(intent);
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
                mainmenu_to_profileview.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                c.startActivity(mainmenu_to_profileview);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
