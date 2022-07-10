package sg.edu.np.ignight.ChatRequest;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.facebook.drawee.drawable.ProgressBarDrawable;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.ArrayList;
import java.util.Collections;

import sg.edu.np.ignight.Objects.UserObject;
import sg.edu.np.ignight.ProfileViewActivity;
import sg.edu.np.ignight.R;

public class ChatRequestSentAdapter extends RecyclerView.Adapter<ChatRequestSentAdapter.ChatRequestSentViewHolder> {

    private ArrayList<ChatRequestObject> chatRequestList;
    private Context context;

    public ChatRequestSentAdapter(Context context, ArrayList<ChatRequestObject> chatRequestList) {
        this.context = context;
        this.chatRequestList = chatRequestList;
    }

    @NonNull
    @Override
    public ChatRequestSentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_request_sent, parent, false);

        return new ChatRequestSentAdapter.ChatRequestSentViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRequestSentViewHolder holder, int position) {
        ChatRequestObject chatRequest = chatRequestList.get(holder.getAdapterPosition());

        // set profile picture + full screen on click
        Glide.with(context).load(chatRequest.getReceiverProfile()).placeholder(R.drawable.ic_baseline_image_24).into(holder.profilePicture);
        holder.profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GenericDraweeHierarchyBuilder hierarchyBuilder = GenericDraweeHierarchyBuilder.newInstance(context.getResources())
                        .setFailureImage(R.drawable.ic_baseline_error_outline_24)
                        .setProgressBarImage(new ProgressBarDrawable())
                        .setPlaceholderImage(R.drawable.ic_baseline_image_24);

                new ImageViewer.Builder(view.getContext(), Collections.singletonList(chatRequest.getReceiverProfile()))
                        .setStartPosition(0)
                        .hideStatusBar(false)
                        .allowZooming(true)
                        .allowSwipeToDismiss(true)
                        .setCustomDraweeHierarchyBuilder(hierarchyBuilder)
                        .show();
            }
        });

        // set username and time when request was sent
        holder.username.setText(chatRequest.getReceiverName());
        holder.requestTimestamp.setText(chatRequest.getCreateTimestamp().getDateTime());

        // set which indicator to show
        if (chatRequest.isPendingRequest()) {  // show default "request sent" indicator if request still pending
            showAccepted(holder, "", false);
            showRejected(holder, "", false);
            showDefaultIndicator(holder, true);
        }
        else {
            // get timestamp when request was accepted/rejected
            String timestamp = chatRequest.getResponseTimestamp().getDateTime();

            if (chatRequest.isRequestAccepted()) {  // show "accepted" indicator with timestamp if request accepted
                showDefaultIndicator(holder, false);
                showRejected(holder, "", false);
                showAccepted(holder, timestamp, true);
            }
            else {  // show "rejected" indicator with timestamp if request rejected
                showDefaultIndicator(holder, false);
                showAccepted(holder, "", false);
                showRejected(holder, timestamp, true);
            }
        }

        // view profile of user (receiver) when the username is clicked
        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ProfileViewActivity.class);
                DatabaseReference targetUserDB = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("user").child(chatRequest.getReceiverID());
                targetUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String uid = chatRequest.getReceiverID();
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

    // show/hide default indicator only
    private void showDefaultIndicator(@NonNull ChatRequestSentViewHolder holder, boolean show) {
        if (show) {
            holder.defaultIndicator.setVisibility(View.VISIBLE);
        }
        else {
            holder.defaultIndicator.setVisibility(View.GONE);
        }
    }

    // show/hide accepted indicator and timestamp
    private void showAccepted(@NonNull ChatRequestSentViewHolder holder, String timestamp, boolean show) {
        if (show) {
            holder.requestAccepted.setVisibility(View.VISIBLE);
            holder.responseTimestamp.setText(timestamp);
            holder.responseTimestamp.setVisibility(View.VISIBLE);
        }
        else {
            holder.requestAccepted.setVisibility(View.GONE);
            holder.responseTimestamp.setVisibility(View.GONE);
        }
    }

    // show/hide rejected indicator and timestamp
    private void showRejected(@NonNull ChatRequestSentViewHolder holder, String timestamp, boolean show) {
        if (show) {
            holder.requestRejected.setVisibility(View.VISIBLE);
            holder.responseTimestamp.setText(timestamp);
            holder.responseTimestamp.setVisibility(View.VISIBLE);
        }
        else {
            holder.requestRejected.setVisibility(View.GONE);
            holder.responseTimestamp.setVisibility(View.GONE);
        }
    }

    class ChatRequestSentViewHolder extends RecyclerView.ViewHolder {

        private ImageView profilePicture;
        private TextView username, requestTimestamp, responseTimestamp, defaultIndicator, requestAccepted, requestRejected;

        public ChatRequestSentViewHolder(@NonNull View itemView) {
            super(itemView);

            profilePicture = itemView.findViewById(R.id.chatRequestSentProfilePicture);
            username = itemView.findViewById(R.id.chatRequestSentUserName);
            requestTimestamp = itemView.findViewById(R.id.chatRequestSentTimestamp);
            responseTimestamp = itemView.findViewById(R.id.chatRequestSentResponseTimestamp);
            defaultIndicator = itemView.findViewById(R.id.chatRequestSentIndicator);
            requestAccepted = itemView.findViewById(R.id.chatRequestSentAccepted);
            requestRejected = itemView.findViewById(R.id.chatRequestSentRejected);
        }
    }

}
