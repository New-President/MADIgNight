package sg.edu.np.ignight;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.ArrayTable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.internal.bind.ArrayTypeAdapter;

import java.util.ArrayList;

import sg.edu.np.ignight.Objects.BlogObject;
import sg.edu.np.ignight.Objects.UserObject;

public class NotificationAdapter
                extends RecyclerView.Adapter<NotificationViewHolder> {

    public static ArrayList<BlogObject> data;
    public static ArrayList<UserObject> userList;
    Context c;

    private FirebaseStorage storage;
    private StorageReference storageReference;

    // Get the current user
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    //get the current user's UID
    String Uid = user.getUid();

    public NotificationAdapter(Context c, ArrayList<BlogObject> data, ArrayList<UserObject> userList){
        this.c = c;
        this.data = data;
        this.userList = userList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = null;
        item = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_layout, null, false);

        return new NotificationViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        BlogObject context = data.get(position);
        String username = "";
        Log.e(TAG, "Bruh: " + data.size());
        for (int i = 0; i<= userList.size(); i++){
            UserObject tempUser = userList.get(i);
            if(tempUser.equals(context.likedUsersList.get(0))){
                username = tempUser.getUsername();
            }
        }
        holder.description.setText(username);





        /*ArrayList<String> userLikedList = context.likedUsersList;
        for (int i = 0; i<=userLikedList.size(); i++){
            String userUid = userLikedList.get(i);
            for (int j = 0; j <= userList.size(); i++){
                UserObject tempUser = userList.get(j);
                if(userUid.equals(tempUser.getUid())){
                    holder.description.setText(tempUser.getUsername());
                }
            }

        }*/

    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
