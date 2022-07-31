package sg.edu.np.ignight;

import static android.content.ContentValues.TAG;

import android.content.Intent;
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
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import sg.edu.np.ignight.Chat.ChatListAdapter;
import sg.edu.np.ignight.Objects.ChatObject;

public class ChatListFragment extends Fragment {


    private RecyclerView chatListRV;
    private ChatListAdapter chatListAdapter;
    private LinearLayoutManager chatListLayoutManager;

    private ArrayList<ChatObject> chatList;
    private TextView chatRequestCount;
    DatabaseReference rootDB;

    public ChatListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        rootDB = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
        chatList = new ArrayList<>();  // initialize chatList

        chatRequestCount = view.findViewById(R.id.chatRequestCount);

        Button chatRequestsButton = view.findViewById(R.id.chatRequestsButton);
        chatRequestsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity().getApplicationContext(), ChatRequestActivity.class));
            }
        });

        FloatingActionButton chatbotButton = view.findViewById(R.id.chatbotFAB);
        chatbotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity().getApplicationContext(), ChatbotActivity.class));
            }
        });

        getChatList();
        initRecyclerView(view);
        getRequestCount();

        return view;
    }

    // get chat request count
    private void getRequestCount() {
        DatabaseReference userDB = rootDB.child("user");

        userDB.child(FirebaseAuth.getInstance().getUid()).child("chatRequests").child("received").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long requestCount = snapshot.getChildrenCount();

                if (requestCount == 0) {
                    chatRequestCount.setVisibility(View.GONE);
                }
                else {
                    chatRequestCount.setText(Long.toString(requestCount));
                    chatRequestCount.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error.getMessage());
            }
        });
    }

    // get list of chats of current user
    private void getChatList() {

        ArrayList<String> chatIdList = new ArrayList<>();
        String currentUserUID = FirebaseAuth.getInstance().getUid();

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

                        // initialize the chat object and update it if values change
                        chatDB.child(chatID).addValueEventListener(new ValueEventListener() {
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

                                boolean newChat = false;
                                if (snapshot.child("newChat").child(currentUserUID).exists()) {
                                    newChat = snapshot.child("newChat").child(currentUserUID).getValue().toString().equals("true");
                                }

                                boolean chatExists = false;
                                int index = 0;
                                for (int i = 0; i < chatList.size(); i++) {
                                    ChatObject chatIterator = chatList.get(i);
                                    if (chatIterator.getChatId().equals(chatID)) {
                                        chatExists = true;
                                        index = i;
                                        break;
                                    }
                                }

                                ChatObject chat = new ChatObject();

                                if (chatExists) {
                                    chat = chatList.get(index);
                                    try {
                                        chat.setChatName(chatName);
                                        chat.setNewChat(newChat);
                                        chat.setLastUsedTimestamp(lastUsedTime);
                                        chat.setUnreadMsgCount(unreadMsgCount);

                                        Collections.sort(chatList);

                                        chatListAdapter.notifyDataSetChanged();
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }
                                else {
                                    try {
                                        chat = new ChatObject(chatID, chatName, targetUserUID, unreadMsgCount, lastUsedTime, newChat);

                                        chatList.add(chat);

                                        Collections.sort(chatList);

                                        chatListAdapter.notifyDataSetChanged();
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }

                                ChatObject finalChat = chat;  // to use in inner class
                                userDB.child(targetUserUID).child("profileUrl").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        try{
                                            String profileUrl = snapshot.getValue().toString();
                                            finalChat.setProfileUrl(profileUrl);
                                            chatListAdapter.notifyDataSetChanged();
                                        }
                                        catch(Exception e){
                                            e.printStackTrace();
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.e(TAG, "onCancelled: " + error.getMessage());
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                error.toException().printStackTrace();

                                Log.e(TAG, "onCancelled at chatlistfragment: " + error.getMessage());
                            }
                        });

                        // listen for changes in the other user's username and update the chat db if it changes
                        userDB.child(targetUserUID).child("username").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                try{
                                    String chatName = snapshot.getValue().toString();
                                    chatDB.child(chatID).child("users").child(targetUserUID).setValue(chatName);
                                }
                                catch (Exception e){
                                    e.printStackTrace();
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
        chatListAdapter = new ChatListAdapter(getActivity().getApplicationContext(), chatList);
        chatListRV.setAdapter(chatListAdapter);
        chatListRV.setItemAnimator(new DefaultItemAnimator());
    }
}