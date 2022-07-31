package sg.edu.np.ignight.Chat;

import java.text.ParseException;
import java.util.ArrayList;

import sg.edu.np.ignight.Objects.TimestampObject;

// object to store information about messages
public class MessageObject {
    private String chatId;
    private String messageId;
    private String creatorId;
    private String message;
    private String dateDescription;
    private String dateLocation;
    private long startDateTime;
    private long endDateTime;
    private Boolean proposeDate;
    private TimestampObject timestamp;
    private ArrayList<String> mediaUrlList;
    private boolean isFirstMessage;
    private boolean isSent;
    private boolean isSeen;

    public MessageObject(String chatId, String messageId, String creatorId, String message, String timestamp, ArrayList<String> mediaUrlList, Boolean proposeDate, String dateDescription, String dateLocation, long startDateTime, long endDateTime) throws ParseException {
        this.chatId = chatId;
        this.messageId = messageId;
        this.creatorId = creatorId;
        this.message = message;
        this.timestamp = new TimestampObject(timestamp);
        this.mediaUrlList = mediaUrlList;
        this.proposeDate = proposeDate;
        this.dateDescription = dateDescription;
        this.dateLocation = dateLocation;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
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
    public String getDateDescription() {
        return dateDescription;
    }
    public String getDateLocation() {
        return dateLocation;
    }
    public Boolean getProposeDate() {
        return proposeDate;
    }
    public long getStartDateTime(){return startDateTime;}
    public long getEndDateTime(){return endDateTime;}
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

    public void setSent(boolean sent) {
        isSent = sent;
    }
    public void setSeen(boolean seen) {
        isSeen = seen;
    }

    // determine if this message is the first message sent on that day
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
