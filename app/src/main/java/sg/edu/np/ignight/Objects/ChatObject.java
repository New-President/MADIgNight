package sg.edu.np.ignight.Objects;

// ChatObject to use for chat function
public class ChatObject {
    private String chatId;
    private String chatName;
    private String targetUserId;

    // takes in chat id, name of chat, user id of the other user for the chat
    public ChatObject(String chatId, String chatName, String targetUserId) {
        this.chatId = chatId;
        this.chatName = chatName;
        this.targetUserId = targetUserId;
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
}
