package sg.edu.np.ignight.Chat;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.facebook.drawee.drawable.ProgressBarDrawable;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import sg.edu.np.ignight.R;
import sg.edu.np.ignight.Objects.TimestampObject;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private Context context;
    private ArrayList<MessageObject> messageList;
    private String date = "";

    public MessageAdapter(Context context, ArrayList<MessageObject> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    // to find out whether the message was sent by the current user or the other party
    @Override
    public int getItemViewType(int position) {
        if (messageList.get(position).getCreatorId().equals(FirebaseAuth.getInstance().getUid())) {
            return 1;
        }
        else {
            return 0;
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate((viewType == 1)?R.layout.item_message_by_you:R.layout.item_message_to_you, parent, false);

        return new MessageViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {

        MessageObject thisMessage = messageList.get(position);

        holder.messageLayout.setClipToOutline(true);

        // set date header for the first messages
        if (thisMessage.isFirstMessage()) {
            String date = thisMessage.getTimestamp().getDate();
            String today = null;

            try {
                today = new TimestampObject(new Date().toString()).getDate();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            holder.messageDate.setText((today.equals(date)?"Today":date));
            holder.messageDate.setClipToOutline(true);
            holder.messageDate.setVisibility(View.VISIBLE);
        }
        else {
            holder.messageDate.setVisibility(View.GONE);
        }

        // set text if there is text sent
        if (!thisMessage.getMessage().equals("")) {
            holder.messageText.setText(thisMessage.getMessage());
            holder.messageText.setVisibility(View.VISIBLE);
        }
        else {
            holder.messageText.setVisibility(View.GONE);
        }

        // set timestamp of message
        holder.messageTime.setText(thisMessage.getTimestamp().getTime());

        ArrayList<String> mediaUrlList = thisMessage.getMediaUrlList();

        // show first picture in mediaUrlList
        if (mediaUrlList.isEmpty()) {
            holder.mediaHolder.setVisibility(View.GONE);
            holder.mediaCount.setVisibility(View.GONE);
        }
        else {
            holder.mediaHolder.setVisibility(View.VISIBLE);
            Glide.with(context).load(mediaUrlList.get(0)).placeholder(R.drawable.ic_baseline_image_24).into(holder.mediaHolder);

            if (mediaUrlList.size() > 1) {
                holder.mediaCount.setVisibility(View.VISIBLE);
                holder.mediaCount.setText("+ " + Integer.toString(mediaUrlList.size() - 1));
            }
            else {
                holder.mediaCount.setVisibility(View.GONE);
            }
        }

        // show pictures in full screen when user clicks on the picture
        holder.mediaHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GenericDraweeHierarchyBuilder hierarchyBuilder = GenericDraweeHierarchyBuilder.newInstance(context.getResources())
                        .setFailureImage(R.drawable.ic_baseline_error_outline_24)
                        .setProgressBarImage(new ProgressBarDrawable())
                        .setPlaceholderImage(R.drawable.ic_baseline_image_24);

                new ImageViewer.Builder(view.getContext(), messageList.get(holder.getAdapterPosition()).getMediaUrlList())
                        .setStartPosition(0)
                        .hideStatusBar(false)
                        .allowZooming(true)
                        .allowSwipeToDismiss(true)
                        .setCustomDraweeHierarchyBuilder(hierarchyBuilder)
                        .show();
            }
        });

        // update icon of seen status of the message (for messages sent by you)
        if (getItemViewType(position) == 1) {
            if (thisMessage.isSent() && !thisMessage.isSeen()) {
                holder.messageStatus.setImageResource(R.drawable.ic_baseline_sent_icon_24);
            }
            else if (thisMessage.isSeen()) {
                holder.messageStatus.setImageResource(R.drawable.ic_baseline_read_icon_24);
            }
        }
        else {
            DatabaseReference chatDB = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("chat").child(thisMessage.getChatId());
            DatabaseReference messageDB = chatDB.child("messages").child(thisMessage.getMessageId());

            // update seen status of messages received
            if (!thisMessage.isSeen()) {
                messageDB.child("isSeen").setValue(true);

                String myUID = FirebaseAuth.getInstance().getUid();
                chatDB.child("unread").child(myUID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int unreadCount = 0;
                        if (snapshot.exists()) {
                            unreadCount = Integer.parseInt(snapshot.getValue().toString());
                        }
                        if (messageList.get(messageList.size() - 1).isSeen()) {  // last message in message list is already seen
                            unreadCount = 0;  // set unread count to 0
                        }

                        chatDB.child("unread").child(myUID).setValue((unreadCount == 0)?unreadCount:(unreadCount - 1));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "onCancelled: " + error.getMessage());
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText, mediaCount, messageTime, messageDate;
        public ImageView mediaHolder, messageStatus;
        public ConstraintLayout messageLayout;
        public ViewStub proposeDateViewStub;

        public MessageViewHolder(View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.messageText);
            mediaHolder = itemView.findViewById(R.id.messageMediaHolder);
            mediaCount = itemView.findViewById(R.id.messageMediaCount);
            messageTime = itemView.findViewById(R.id.messageTime);
            messageDate = itemView.findViewById(R.id.messageDate);
            messageLayout = itemView.findViewById(R.id.messageLayout);
            messageStatus = itemView.findViewById(R.id.messageStatus);
            proposeDateViewStub = itemView.findViewById(R.id.proposeDateViewStub);
        }
    }
}
