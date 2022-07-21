package sg.edu.np.ignight.ChatRequest;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.facebook.drawee.drawable.ProgressBarDrawable;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import sg.edu.np.ignight.Objects.UserObject;
import sg.edu.np.ignight.ProfileViewActivity;
import sg.edu.np.ignight.R;

public class ChatRequestReceivedAdapter extends RecyclerView.Adapter<ChatRequestReceivedAdapter.ChatRequestReceivedViewHolder> {

    private Context context;
    private ArrayList<ChatRequestObject> chatRequestList;

    public ChatRequestReceivedAdapter(Context context, ArrayList<ChatRequestObject> chatRequestList) {
        this.context = context;
        this.chatRequestList = chatRequestList;
    }

    @NonNull
    @Override
    public ChatRequestReceivedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_request_received, parent, false);

        return new ChatRequestReceivedAdapter.ChatRequestReceivedViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRequestReceivedViewHolder holder, int position) {
        ChatRequestObject chatRequest = chatRequestList.get(holder.getAdapterPosition());

        // set profile picture + full screen on click
        Glide.with(context).load(chatRequest.getCreatorProfile()).placeholder(R.drawable.ic_baseline_image_24).into(holder.profilePicture);
        holder.profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GenericDraweeHierarchyBuilder hierarchyBuilder = GenericDraweeHierarchyBuilder.newInstance(context.getResources())
                        .setFailureImage(R.drawable.ic_baseline_error_outline_24)
                        .setProgressBarImage(new ProgressBarDrawable())
                        .setPlaceholderImage(R.drawable.ic_baseline_image_24);

                new ImageViewer.Builder(view.getContext(), Collections.singletonList(chatRequest.getCreatorProfile()))
                        .setStartPosition(0)
                        .hideStatusBar(false)
                        .allowZooming(true)
                        .allowSwipeToDismiss(true)
                        .setCustomDraweeHierarchyBuilder(hierarchyBuilder)
                        .show();
            }
        });

        // set username and time when request was sent
        holder.username.setText(chatRequest.getCreatorName());
        holder.requestTimestamp.setText(chatRequest.getCreateTimestamp().getDateTime());

        // show accept/reject buttons and hide accepted/rejected textviews by default
        showButtons(holder, true);
        showAccepted(holder, false);
        showRejected(holder, false);

        holder.acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startChat(holder, chatRequest, true);
            }
        });

        holder.rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startChat(holder, chatRequest, false);
            }
        });

        // view profile of user (sender) when the username is clicked
        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ProfileViewActivity.class);
                DatabaseReference targetUserDB = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("user").child(chatRequest.getCreatorID());
                targetUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String uid = chatRequest.getCreatorID();
                        ArrayList<String> dateLocList = new ArrayList<>();
                        ArrayList<String> interestList = new ArrayList<>();

                        String phone = snapshot.child("phone").getValue().toString();
                        String aboutMe = snapshot.child("About Me").getValue().toString();
                        String gender = snapshot.child("Gender").getValue().toString();
                        String genderPref = snapshot.child("Gender Preference").getValue().toString();
                        String profilePicUrl = snapshot.child("profileUrl").getValue().toString();
                        String relationshipPref = snapshot.child("Relationship Preference").getValue().toString();
                        String username = snapshot.child("username").getValue().toString();
                        String profileCreated = snapshot.child("profileCreated").getValue().toString();
                        int age = Integer.parseInt(snapshot.child("Age").getValue().toString());

                        for (DataSnapshot dateLocSnapshot : snapshot.child("Date Location").getChildren()) {
                            dateLocList.add(dateLocSnapshot.getValue().toString());
                        }
                        for (DataSnapshot interestSnapshot : snapshot.child("Interest").getChildren()) {
                            interestList.add(interestSnapshot.getValue().toString());
                        }

                        UserObject user = new UserObject(uid, aboutMe, age, dateLocList, gender, genderPref, interestList, profilePicUrl, relationshipPref, phone, profileCreated, username);

                        intent.putExtra("user", user);
                        view.getContext().startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "onCancelled: " + error.getMessage());
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatRequestList.size();
    }

    // if accepted - update relevant request data and create new chat
    // otherwise update relevant request data
    private void startChat(@NonNull ChatRequestReceivedViewHolder holder, ChatRequestObject request, boolean accepted) {
        DatabaseReference rootDB = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
        DatabaseReference chatRequestDB = rootDB.child("chatRequest");
        DatabaseReference userDB = rootDB.child("user");

        String requestID = request.getRequestID();
        String currentTimestamp = new Date().toString();
        String currentUserUID = FirebaseAuth.getInstance().getUid();

        Map updateRequestMap = new HashMap<>();
        updateRequestMap.put("responseTimestamp", currentTimestamp);
        updateRequestMap.put("pending", false);
        updateRequestMap.put("accepted", accepted);

        Map updateUserMap = new HashMap<>();
        updateUserMap.put(currentUserUID + "/chatRequests/received/" + requestID, null);

        if (accepted) {
            DatabaseReference chatDB = rootDB.child("chat");
            String targetUserUID = request.getCreatorID();

            // start chat and update existing request
            String newChatID = chatDB.push().getKey();

            updateUserMap.put(currentUserUID + "/chats/" + newChatID, targetUserUID);
            updateUserMap.put(targetUserUID + "/chats/" + newChatID, currentUserUID);

            Map newChatMap = new HashMap<>();
            newChatMap.put("users/" + currentUserUID, request.getReceiverName());
            newChatMap.put("users/" + targetUserUID, request.getCreatorName());
            newChatMap.put("newChat/" + currentUserUID, true);
            newChatMap.put("newChat/" + targetUserUID, true);
            newChatMap.put("lastUsed", currentTimestamp);
            newChatMap.put("onCall", false);

            chatRequestDB.child(requestID).updateChildren(updateRequestMap);

            chatDB.child(newChatID).updateChildren(newChatMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        userDB.updateChildren(updateUserMap);

                        deleteItem(holder, request, true);
                    }
                    else {
                        task.getException().printStackTrace();
                    }
                }
            });
        }
        else {
            userDB.updateChildren(updateUserMap);
            chatRequestDB.child(requestID).updateChildren(updateRequestMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        deleteItem(holder, request, false);
                    }
                }
            });
        }
    }

    // add animation for the deletion of item
    private void deleteItem(@NonNull ChatRequestReceivedViewHolder holder, ChatRequestObject request, boolean accepted) {
        View view = holder.itemView;
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_out_right);
        animation.setDuration(300);

        if (accepted) {
            showButtons(holder, false);
            showRejected(holder, false);
            showAccepted(holder, true);
        }
        else {
            showButtons(holder, false);
            showAccepted(holder, false);
            showRejected(holder, true);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                view.startAnimation(animation);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (chatRequestList.contains(request)) {
                            chatRequestList.remove(request);
                            notifyDataSetChanged();
                        }
                    }
                }, animation.getDuration());
            }
        }, 1000);
    }

    // show/hide accept/reject buttons
    private void showButtons(@NonNull ChatRequestReceivedViewHolder holder, boolean show) {
        if (show) {
            holder.buttonContainer.setVisibility(View.VISIBLE);
        }
        else {
            holder.buttonContainer.setVisibility(View.GONE);
        }
    }

    // show/hide accepted indicator
    private void showAccepted(@NonNull ChatRequestReceivedViewHolder holder, boolean show) {
        if (show) {
            holder.requestAccepted.setVisibility(View.VISIBLE);
        }
        else {
            holder.requestAccepted.setVisibility(View.GONE);
        }
    }

    // show/hide rejected indicator
    private void showRejected(@NonNull ChatRequestReceivedViewHolder holder, boolean show) {
        if (show) {
            holder.requestRejected.setVisibility(View.VISIBLE);
        }
        else {
            holder.requestRejected.setVisibility(View.GONE);
        }
    }

    class ChatRequestReceivedViewHolder extends RecyclerView.ViewHolder {

        private ImageView profilePicture;
        private Button acceptButton, rejectButton;
        private TextView username, requestTimestamp, requestAccepted, requestRejected;
        private RelativeLayout buttonContainer;

        public ChatRequestReceivedViewHolder(@NonNull View itemView) {
            super(itemView);

            profilePicture = itemView.findViewById(R.id.chatRequestReceivedProfilePicture);
            acceptButton = itemView.findViewById(R.id.chatRequestReceivedAccept);
            rejectButton = itemView.findViewById(R.id.chatRequestReceivedReject);
            username = itemView.findViewById(R.id.chatRequestReceivedUserName);
            requestTimestamp = itemView.findViewById(R.id.chatRequestReceivedTimestamp);
            requestAccepted = itemView.findViewById(R.id.chatRequestReceivedAccepted);
            requestRejected = itemView.findViewById(R.id.chatRequestReceivedRejected);
            buttonContainer = itemView.findViewById(R.id.chatRequestReceivedButtonContainer);
        }
    }
}
