package sg.edu.np.ignight.Objects;

import java.text.ParseException;

// ChatObject to use for chat function
public class ChatObject implements Comparable<ChatObject> {
    private String chatId;
    private String chatName;
    private String targetUserId;
    private int unreadMsgCount;
    private TimestampObject lastUsedTimestamp;
    private boolean newChat;
    private String profileUrl;

    public ChatObject() {}

    // takes in chat id, name of chat, user id of the other user for the chat, unread messages count and last used time as a string
    public ChatObject(String chatId, String chatName, String targetUserId, int unreadMsgCount, String lastUsedTimeString, boolean newChat) throws ParseException {
        this.chatId = chatId;
        this.chatName = chatName;
        this.targetUserId = targetUserId;
        this.unreadMsgCount = unreadMsgCount;
        this.lastUsedTimestamp = new TimestampObject(lastUsedTimeString);
        this.newChat = newChat;
    }

    // compare the lastUsedTimestamp of chats for sorting
    @Override
    public int compareTo(ChatObject chatObject) {
        int result = this.lastUsedTimestamp.getTimestamp().compareTo(chatObject.lastUsedTimestamp.getTimestamp());

        return Integer.compare(0, result);
    }

    public String getChatId() {
        return chatId;
    }
    public String getChatName() {
        return chatName;
    }
    public String getTargetUserId() {
        return targetUserId;
    }
    public int getUnreadMsgCount() {
        return unreadMsgCount;
    }
    public TimestampObject getLastUsedTimestamp() {
        return lastUsedTimestamp;
    }
    public boolean isNewChat() {
        return newChat;
    }
    public String getProfileUrl() {
        return profileUrl;
    }

    public void setUnreadMsgCount(int unreadMsgCount) {
        this.unreadMsgCount = unreadMsgCount;
    }
    public void setChatName(String chatName) {
        this.chatName = chatName;
    }
    public void setNewChat(boolean newChat) {
        this.newChat = newChat;
    }
    public void setLastUsedTimestamp(String lastUsedTimeString) throws ParseException {
        this.lastUsedTimestamp = new TimestampObject(lastUsedTimeString);
    }
    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }
}
