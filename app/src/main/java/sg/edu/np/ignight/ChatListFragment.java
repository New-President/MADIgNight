package sg.edu.np.ignight;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import sg.edu.np.ignight.Chat.ChatListAdapter;
import sg.edu.np.ignight.Objects.ChatObject;

public class ChatListFragment extends Fragment {


    private RecyclerView chatListRV;
    private ChatListAdapter chatListAdapter;
    private LinearLayoutManager chatListLayoutManager;

    private ArrayList<ChatObject> chatList;

    public ChatListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        chatList = new ArrayList<>();  // initialize chatList

        getChatList();
        initRecyclerView(view);

        return view;
    }

    // get list of chats of current user
    private void getChatList() {

        ArrayList<String> chatIdList = new ArrayList<>();
        String currentUserUID = FirebaseAuth.getInstance().getUid();

        DatabaseReference userDB = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("user");

        userDB.child(currentUserUID).child("chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot chatIDSnapshot : snapshot.getChildren()) {
                        if (chatIdList.contains(chatIDSnapshot.getKey())) {
                            continue;
                        }

                        String targetUserUID = chatIDSnapshot.getValue().toString();

                        userDB.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String chatName = snapshot.child(targetUserUID).child("username").getValue().toString();
                                ChatObject chat = new ChatObject(chatIDSnapshot.getKey(), chatName, targetUserUID);

                                chatList.add(chat);
                                chatListAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "onCancelled: " + error.getMessage());
                            }
                        });

                        chatIdList.add(chatIDSnapshot.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error.getMessage());
            }
        });
    }

    // initialize recycler view
    private void initRecyclerView(View view) {
        chatListRV = view.findViewById(R.id.chatList);
        chatListRV.setNestedScrollingEnabled(false);
        chatListRV.setHasFixedSize(false);
        chatListLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        chatListRV.setLayoutManager(chatListLayoutManager);
        chatListAdapter = new ChatListAdapter(chatList);
        chatListRV.setAdapter(chatListAdapter);
        chatListRV.setItemAnimator(new DefaultItemAnimator());
    }
}