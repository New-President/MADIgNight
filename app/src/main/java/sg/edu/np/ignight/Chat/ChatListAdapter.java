package sg.edu.np.ignight.Chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import sg.edu.np.ignight.ChatActivity;
import sg.edu.np.ignight.Objects.ChatObject;
import sg.edu.np.ignight.R;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder> {

    private ArrayList<ChatObject> chatList;

    public ChatListAdapter(ArrayList<ChatObject> chatList) {
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
        // set chat name of the chat
        holder.chatTitle.setText(chatList.get(position).getChatName());
        // goes to the chat activity for the selected chat
        holder.chatLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ChatActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("chatID", chatList.get(holder.getAdapterPosition()).getChatId());
                bundle.putString("chatName", chatList.get(holder.getAdapterPosition()).getChatName());
                bundle.putString("targetUserID", chatList.get(holder.getAdapterPosition()).getTargetUserId());
                intent.putExtras(bundle);
                view.getContext().startActivity(intent);
            }
        });

        int unreadMsgCount = chatList.get(holder.getAdapterPosition()).getUnreadMsgCount();
        if (unreadMsgCount > 0) {
            holder.chatUnreadMsgCount.setText(unreadMsgCount + " unread");
            holder.chatUnreadMsgCount.setVisibility(View.VISIBLE);
        }
        else {
            holder.chatUnreadMsgCount.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public class ChatListViewHolder extends RecyclerView.ViewHolder {
        public TextView chatTitle, chatUnreadMsgCount;
        public ConstraintLayout chatLayout;

        public ChatListViewHolder(@NonNull View itemView) {
            super(itemView);
            chatTitle = itemView.findViewById(R.id.chatTitle);
            chatLayout = itemView.findViewById(R.id.chatLayout);
            chatUnreadMsgCount = itemView.findViewById(R.id.unreadMsgCount);
        }
    }
}
