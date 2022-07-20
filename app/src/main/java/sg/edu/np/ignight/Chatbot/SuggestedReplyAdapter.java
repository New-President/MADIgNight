package sg.edu.np.ignight.Chatbot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import sg.edu.np.ignight.R;

public class SuggestedReplyAdapter extends RecyclerView.Adapter<SuggestedReplyAdapter.SuggestedReplyViewHolder> {

    private ArrayList<SuggestedReplyObject> replyList;
    private EditText input;

    public SuggestedReplyAdapter(ArrayList<SuggestedReplyObject> replyList, EditText input) {
        this.replyList = replyList;
        this.input = input;
    }

    @NonNull
    @Override
    public SuggestedReplyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_suggested_reply, parent, false);

        return new SuggestedReplyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestedReplyViewHolder holder, int position) {
        holder.replyText.setText(replyList.get(holder.getAdapterPosition()).getText());
        holder.replyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // use the suggested replies as input for chatbot
                input.setText(replyList.get(holder.getAdapterPosition()).getText());
            }
        });
    }

    @Override
    public int getItemCount() {
        return replyList.size();
    }

    class SuggestedReplyViewHolder extends RecyclerView.ViewHolder {

        private TextView replyText;

        public SuggestedReplyViewHolder(@NonNull View itemView) {
            super(itemView);

            replyText = itemView.findViewById(R.id.suggestedReplyText);
        }
    }
}
