package sg.edu.np.ignight.Chat;

import android.content.Context;
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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import sg.edu.np.ignight.R;
import sg.edu.np.ignight.Timestamp.TimestampObject;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private Context context;
    private ArrayList<MessageObject> messageList;
    private String date = "";

    public MessageAdapter(Context context, ArrayList<MessageObject> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

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
    public MessageAdapter.MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate((viewType == 1)?R.layout.item_message_by_you:R.layout.item_message_to_you, parent, false);

        return new MessageViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {

        MessageObject thisMessage = messageList.get(position);

        holder.messageLayout.setClipToOutline(true);

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

        if (!thisMessage.getMessage().equals("")) {
            holder.messageText.setText(thisMessage.getMessage());
            holder.messageText.setVisibility(View.VISIBLE);
        }
        else {
            holder.messageText.setVisibility(View.GONE);
        }

        holder.messageTime.setText(thisMessage.getTimestamp().getTime());

        ArrayList<String> mediaUrlList = thisMessage.getMediaUrlList();

        if (mediaUrlList.isEmpty()) {
            holder.mediaHolder.setVisibility(View.GONE);
            holder.mediaCount.setVisibility(View.GONE);
        }
        else {
            holder.mediaHolder.setVisibility(View.VISIBLE);
            Glide.with(context).load(mediaUrlList.get(0)).into(holder.mediaHolder);

            if (mediaUrlList.size() > 1) {
                holder.mediaCount.setVisibility(View.VISIBLE);
                holder.mediaCount.setText("+ " + Integer.toString(mediaUrlList.size() - 1));
            }
            else {
                holder.mediaCount.setVisibility(View.GONE);
            }
        }

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

        if (getItemViewType(position) == 1) {
            if (thisMessage.isSent() && !thisMessage.isSeen()) {
                holder.messageStatus.setImageResource(R.drawable.ic_baseline_sent_icon_24);
            }
            else if (thisMessage.isSeen()) {
                holder.messageStatus.setImageResource(R.drawable.ic_baseline_read_icon_24);
            }
        }
        else {
            if (!thisMessage.isSeen()) {
                thisMessage.getDbRef().setValue(true);
                thisMessage.getDbRef().removeEventListener(thisMessage.getListener());
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

        public MessageViewHolder(View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.messageText);
            mediaHolder = itemView.findViewById(R.id.messageMediaHolder);
            mediaCount = itemView.findViewById(R.id.messageMediaCount);
            messageTime = itemView.findViewById(R.id.messageTime);
            messageDate = itemView.findViewById(R.id.messageDate);
            messageLayout = itemView.findViewById(R.id.messageLayout);
            messageStatus = itemView.findViewById(R.id.messageStatus);
        }
    }
}
