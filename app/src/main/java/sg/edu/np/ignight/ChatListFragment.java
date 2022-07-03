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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

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

        DatabaseReference rootDB = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
        DatabaseReference userDB = rootDB.child("user");
        DatabaseReference chatDB = rootDB.child("chat");

        userDB.child(currentUserUID).child("chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot chatIDSnapshot : snapshot.getChildren()) {

                        String chatID = chatIDSnapshot.getKey();

                        if (chatIdList.contains(chatID)) {
                            continue;
                        }

                        // following code is only reached when the chat is first being added to chatList
                        String targetUserUID = chatIDSnapshot.getValue().toString();

                        // initialize the chat object
                        chatDB.child(chatID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String chatName = snapshot.child("users").child(targetUserUID).getValue().toString();

                                int unreadMsgCount = 0;
                                if (snapshot.child("unread").child(currentUserUID).exists()) {
                                    unreadMsgCount = Integer.parseInt(snapshot.child("unread").child(currentUserUID).getValue().toString());
                                }

                                // set default last used time to make sure it is sorted to be at the end of the list
                                // this value only remains as the default if the chat does not have a last used time set and there are no messages sent
                                String lastUsedTime = new Date(1).toString();

                                if (snapshot.child("lastUsed").exists()) {
                                    // sets last used time to the value found in the database
                                    lastUsedTime = snapshot.child("lastUsed").getValue().toString();
                                }
                                else if (snapshot.child("messages").exists()) {
                                    // takes the timestamp of the last message sent to be the last used time
                                    long messageCount = snapshot.child("messages").getChildrenCount();
                                    long currentMsg = 1;
                                    for (DataSnapshot messageSnapshot : snapshot.child("messages").getChildren()) {
                                        if (currentMsg == messageCount) {
                                            lastUsedTime = messageSnapshot.child("timestamp").getValue().toString();
                                            break;
                                        }

                                        currentMsg += 1;
                                    }
                                }

                                try {
                                    ChatObject chat = new ChatObject(chatID, chatName, targetUserUID, unreadMsgCount, lastUsedTime);

                                    chatList.add(chat);

                                    Collections.sort(chatList);

                                    chatListAdapter.notifyDataSetChanged();
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "onCancelled: " + error.getMessage());
                            }
                        });

                        // listen for changes in the other user's username and update the chat object if it changes
                        userDB.child(targetUserUID).child("username").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String chatName = snapshot.getValue().toString();
                                final boolean[] complete = {false};

                                for (ChatObject chatIterator : chatList) {
                                    if (complete[0]) {
                                        break;
                                    }
                                    if (chatIterator.getChatId().equals(chatID)) {
                                        if (!chatIterator.getChatName().equals(chatName)) {
                                            chatDB.child(chatID).child("users").child(targetUserUID).setValue(chatName).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    chatIterator.setChatName(chatName);
                                                    chatListAdapter.notifyDataSetChanged();

                                                    complete[0] = true;
                                                }
                                            });
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "onCancelled: " + error.getMessage());
                            }
                        });

                        // listen for changes in the number of unread messages of the chat for the current user and update the chat object if it changes
                        chatDB.child(chatID).child("unread").child(currentUserUID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (ChatObject chatIterator : chatList) {
                                    if (chatIterator.getChatId().equals(chatID)) {
                                        int unreadMsgCount = 0;
                                        if (snapshot.exists()) {
                                            unreadMsgCount = Integer.parseInt(snapshot.getValue().toString());
                                        }

                                        if (chatIterator.getUnreadMsgCount() != unreadMsgCount) {
                                            chatIterator.setUnreadMsgCount(unreadMsgCount);

                                            chatListAdapter.notifyDataSetChanged();
                                        }

                                        break;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "onCancelled: " + error.getMessage());
                            }
                        });

                        // listen for changes in the time when the chat was last used and update the chat object if it changes
                        chatDB.child(chatID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                final String[] lastUsedTime = new String[1];
                                if (snapshot.child("lastUsed").exists()) {
                                    lastUsedTime[0] = snapshot.child("lastUsed").getValue().toString();
                                }
                                else if (snapshot.child("messages").exists()) {
                                    long messageCount = snapshot.child("messages").getChildrenCount();
                                    long currentMsg = 1;
                                    for (DataSnapshot messageSnapshot : snapshot.child("messages").getChildren()) {
                                        if (currentMsg == messageCount) {
                                            lastUsedTime[0] = messageSnapshot.child("timestamp").getValue().toString();
                                            break;
                                        }

                                        currentMsg += 1;
                                    }
                                }
                                else {
                                    lastUsedTime[0] = new Date(1).toString();
                                }

                                for (ChatObject chatIterator : chatList) {
                                    if (chatIterator.getChatId().equals(chatID)) {
                                        try {
                                            chatIterator.setLastUsedTimestamp(lastUsedTime[0]);

                                            Collections.sort(chatList);

                                            chatListAdapter.notifyDataSetChanged();
                                            break;
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "onCancelled: " + error.getMessage());
                            }
                        });

                        chatIdList.add(chatID);
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