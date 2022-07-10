package sg.edu.np.ignight.Chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.facebook.drawee.drawable.ProgressBarDrawable;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.ArrayList;
import java.util.Collections;

import sg.edu.np.ignight.ChatActivity;
import sg.edu.np.ignight.Objects.ChatObject;
import sg.edu.np.ignight.R;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder> {

    private Context context;
    private ArrayList<ChatObject> chatList;

    public ChatListAdapter(Context context, ArrayList<ChatObject> chatList) {
        this.context = context;
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);

        return new ChatListViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatListViewHolder holder, int position) {
        ChatObject chat = chatList.get(holder.getAdapterPosition());

        // set chat name of the chat
        holder.chatTitle.setText(chat.getChatName());
        // goes to the chat activity for the selected chat
        holder.chatLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ChatActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("chatID", chat.getChatId());
                bundle.putString("chatName", chat.getChatName());
                bundle.putString("targetUserID", chat.getTargetUserId());
                intent.putExtras(bundle);
                view.getContext().startActivity(intent);

                DatabaseReference chatDB = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("chat").child(chat.getChatId());
                chatDB.child("newChat").child(FirebaseAuth.getInstance().getUid()).setValue(false);
            }
        });

        int unreadMsgCount = chat.getUnreadMsgCount();
        if (unreadMsgCount > 0) {
            holder.chatUnreadMsgCount.setText(unreadMsgCount + " unread");
            holder.chatUnreadMsgCount.setVisibility(View.VISIBLE);
        }
        else {
            holder.chatUnreadMsgCount.setVisibility(View.GONE);
        }

        if (chat.isNewChat()) {
            holder.newChatIndicator.setVisibility(View.VISIBLE);
        }
        else {
            holder.newChatIndicator.setVisibility(View.GONE);
        }

        // show profile picture of user
        Glide.with(context).load(chat.getProfileUrl()).placeholder(R.drawable.ic_baseline_image_24).into(holder.profilePicture);

        // show pictures in full screen when user clicks on the picture
        holder.profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GenericDraweeHierarchyBuilder hierarchyBuilder = GenericDraweeHierarchyBuilder.newInstance(context.getResources())
                        .setFailureImage(R.drawable.ic_baseline_error_outline_24)
                        .setProgressBarImage(new ProgressBarDrawable())
                        .setPlaceholderImage(R.drawable.ic_baseline_image_24);

                new ImageViewer.Builder(view.getContext(), Collections.singletonList(chat.getProfileUrl()))
                        .setStartPosition(0)
                        .hideStatusBar(false)
                        .allowZooming(true)
                        .allowSwipeToDismiss(true)
                        .setCustomDraweeHierarchyBuilder(hierarchyBuilder)
                        .show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public class ChatListViewHolder extends RecyclerView.ViewHolder {
        public TextView chatTitle, chatUnreadMsgCount, newChatIndicator;
        public ConstraintLayout chatLayout;
        private ImageView profilePicture;

        public ChatListViewHolder(@NonNull View itemView) {
            super(itemView);
            chatTitle = itemView.findViewById(R.id.chatTitle);
            chatLayout = itemView.findViewById(R.id.chatLayout);
            chatUnreadMsgCount = itemView.findViewById(R.id.unreadMsgCount);
            profilePicture = itemView.findViewById(R.id.chatProfilePicture);
            newChatIndicator = itemView.findViewById(R.id.newChatIndicator);
        }
    }
}
