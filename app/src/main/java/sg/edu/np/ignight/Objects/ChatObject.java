package sg.edu.np.ignight.Objects;

// ChatObject to use for chat function
public class ChatObject {
    private String chatId;
    private String chatName;
    private String targetUserId;
    private int unreadMsgCount;

    // takes in chat id, name of chat, user id of the other user for the chat
    public ChatObject(String chatId, String chatName, String targetUserId, int unreadMsgCount) {
        this.chatId = chatId;
        this.chatName = chatName;
        this.targetUserId = targetUserId;
        this.unreadMsgCount = unreadMsgCount;
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

    public void setUnreadMsgCount(int unreadMsgCount) {
        this.unreadMsgCount = unreadMsgCount;
    }
    public void setChatName(String chatName) {
        this.chatName = chatName;
    }
}
