package sg.edu.np.ignight.Chat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.util.ArrayList;

import sg.edu.np.ignight.Objects.TimestampObject;

public class MessageObject {
    private String chatId;
    private String messageId;
    private String creatorId;
    private String message;
    private TimestampObject timestamp;
    private ArrayList<String> mediaUrlList;
    private boolean isFirstMessage;
    private boolean isSent;
    private boolean isSeen;
    private DatabaseReference dbRef;
    private ValueEventListener listener;

    public MessageObject(String chatId, String messageId, String creatorId, String message, String timestamp, ArrayList<String> mediaUrlList) throws ParseException {
        this.chatId = chatId;
        this.messageId = messageId;
        this.creatorId = creatorId;
        this.message = message;
        this.timestamp = new TimestampObject(timestamp);
        this.mediaUrlList = mediaUrlList;
    }

    public String getChatId() {
        return chatId;
    }
    public String getMessageId() {
        return messageId;
    }
    public String getCreatorId() {
        return creatorId;
    }
    public String getMessage() {
        return message;
    }
    public TimestampObject getTimestamp() {
        return timestamp;
    }
    public ArrayList<String> getMediaUrlList() {
        return mediaUrlList;
    }
    public boolean isFirstMessage() {
        return isFirstMessage;
    }
    public boolean isSeen() {
        return isSeen;
    }
    public boolean isSent() {
        return isSent;
    }
    public DatabaseReference getDbRef() {
        return dbRef;
    }
    public ValueEventListener getListener() {
        return listener;
    }

    public void setDbRef(DatabaseReference dbRef) {
        this.dbRef = dbRef;
    }
    public void setListener(ValueEventListener listener) {
        this.listener = listener;
    }
    public void setSent(boolean sent) {
        isSent = sent;
    }
    public void setSeen(boolean seen) {
        isSeen = seen;
    }
    public void setFirstMessage(ArrayList<MessageObject> messageList) {
        String messageDate = this.getTimestamp().getDate();

        if (!messageList.isEmpty()) {
            for (MessageObject messageIterator : messageList) {
                if (messageIterator.getTimestamp().getDate().equals(messageDate)) {
                    this.isFirstMessage = false;
                    return;
                }
            }
        }

        this.isFirstMessage = true;
    }
}
