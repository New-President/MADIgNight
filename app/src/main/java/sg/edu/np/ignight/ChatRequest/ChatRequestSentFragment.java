package sg.edu.np.ignight.ChatRequest;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.util.ArrayList;

import sg.edu.np.ignight.R;

public class ChatRequestSentFragment extends Fragment {

    private RecyclerView chatRequestRV;
    private ChatRequestSentAdapter adapter;
    private LinearLayoutManager layoutManager;

    private ArrayList<ChatRequestObject> chatRequestList;
    private TextView showRVEmpty;

    public ChatRequestSentFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chat_request_sent, container, false);

        chatRequestList = new ArrayList<>();

        getRequests();
        initRecyclerView(view);
        showEmpty();

        return view;
    }

    // get requests and add to chatRequestList
    private void getRequests() {
        DatabaseReference rootDB = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
        DatabaseReference chatRequestDB = rootDB.child("chatRequest");
        DatabaseReference userDB = rootDB.child("user");

        String currentUserID = FirebaseAuth.getInstance().getUid();
        userDB.child(currentUserID).child("chatRequests").child("sent").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ArrayList<String> sentRequestList = new ArrayList<>();

                    // iterate through sent requests
                    for (DataSnapshot requestSnapshot : snapshot.getChildren()) {
                        String requestID = requestSnapshot.getKey();
                        String receiverID = requestSnapshot.getValue().toString();

                        if (sentRequestList.contains(requestID)) {
                            continue;
                        }

                        // following code is only reached when the request is first added to the list

                        // initialize requests and update values when changed
                        chatRequestDB.child(requestID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                boolean requestExists = false;
                                int index = 0;

                                for (int i = 0; i < chatRequestList.size(); i++) {
                                    ChatRequestObject requestIterator = chatRequestList.get(i);
                                    if (requestIterator.getRequestID().equals(requestID)) {
                                        requestExists = true;
                                        index = i;
                                        break;
                                    }
                                }

                                boolean pending = snapshot.child("pending").getValue().toString().equals("true");
                                String receiverName = snapshot.child("receiverName").getValue().toString();
                                String receiverProfile = snapshot.child("receiverProfile").getValue().toString();

                                if (requestExists) {
                                    ChatRequestObject existingRequest = chatRequestList.get(index);

                                    existingRequest.setReceiverName(receiverName);
                                    existingRequest.setReceiverProfile(receiverProfile);
                                    existingRequest.setPendingRequest(pending);

                                    if (!pending) {
                                        try {
                                            boolean accepted = snapshot.child("accepted").getValue().toString().equals("true");
                                            String responseTimestamp = snapshot.child("responseTimestamp").getValue().toString();

                                            existingRequest.setRequestAccepted(accepted);
                                            existingRequest.setResponseTimestamp(responseTimestamp);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                else {
                                    String createTimestamp = snapshot.child("createTimestamp").getValue().toString();
                                    try {
                                        ChatRequestObject request = new ChatRequestObject(requestID, currentUserID, receiverID, createTimestamp, pending);

                                        request.setReceiverName(receiverName);
                                        request.setReceiverProfile(receiverProfile);

                                        if (!pending) {
                                            boolean accepted = snapshot.child("accepted").getValue().toString().equals("true");
                                            String responseTimestamp = snapshot.child("responseTimestamp").getValue().toString();

                                            request.setRequestAccepted(accepted);
                                            request.setResponseTimestamp(responseTimestamp);
                                        }

                                        chatRequestList.add(0, request);

                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }

                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "onCancelled: " + error.getMessage());
                            }
                        });

                        // update chatRequestDB when receiver's username or profileUrl changes
                        userDB.child(receiverID).child("username").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                chatRequestDB.child(requestID).child("receiverName").setValue(snapshot.getValue().toString());
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "onCancelled: " + error.getMessage());
                            }
                        });

                        userDB.child(receiverID).child("profileUrl").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                chatRequestDB.child(requestID).child("receiverProfile").setValue(snapshot.getValue().toString());
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "onCancelled: " + error.getMessage());
                            }
                        });

                        sentRequestList.add(requestID);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error.getMessage());
            }
        });
    }

    // initialize recyclerView
    private void initRecyclerView(View view) {
        chatRequestRV = view.findViewById(R.id.chatRequestSentRecyclerView);
        showRVEmpty = view.findViewById(R.id.chatRequestSentEmpty);
        chatRequestRV.setNestedScrollingEnabled(false);
        chatRequestRV.setHasFixedSize(false);
        layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        chatRequestRV.setLayoutManager(layoutManager);
        adapter = new ChatRequestSentAdapter(getActivity().getApplicationContext(), chatRequestList);
        chatRequestRV.setAdapter(adapter);
        chatRequestRV.setItemAnimator(new DefaultItemAnimator());
    }

    // show/hide recyclerview empty message automatically
    private void showEmpty() {
        // initialize
        if (adapter.getItemCount() == 0) {
            showRVEmpty.setVisibility(View.VISIBLE);
        }
        else {
            showRVEmpty.setVisibility(View.GONE);
        }

        // to track changes in the number of items in the recyclerview
        RecyclerView.AdapterDataObserver observer = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();

                int itemCount = adapter.getItemCount();
                if (itemCount == 0) {  // display message when recyclerview is empty
                    showRVEmpty.setVisibility(View.VISIBLE);
                }
                else if (itemCount > 0) {  // otherwise hide message
                    showRVEmpty.setVisibility(View.GONE);
                }
            }
        };

        adapter.registerAdapterDataObserver(observer);
    }
}