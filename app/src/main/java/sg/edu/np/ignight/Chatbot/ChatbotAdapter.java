package sg.edu.np.ignight.Chatbot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import sg.edu.np.ignight.Objects.ChatbotMessageObject;
import sg.edu.np.ignight.R;

public class ChatbotAdapter extends RecyclerView.Adapter<ChatbotAdapter.ChatbotViewHolder> {

    private ArrayList<ChatbotMessageObject> messageList;

    public ChatbotAdapter(ArrayList<ChatbotMessageObject> messageList) {
        this.messageList = messageList;
    }

    @Override
    public int getItemViewType(int position) {
        // check if the message is sent by user
        return (messageList.get(position).isCreator()?0:1);
    }

    @NonNull
    @Override
    public ChatbotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate((viewType == 0)?R.layout.item_chatbot_message_sent:R.layout.item_chatbot_message_received, parent, false);

        return new ChatbotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatbotViewHolder holder, int position) {
        // set message text
        holder.messageText.setText(messageList.get(position).getMessage());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    class ChatbotViewHolder extends RecyclerView.ViewHolder {

        private TextView messageText;

        public ChatbotViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.chatbotMessageText);
        }
    }
}
