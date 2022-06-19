package sg.edu.np.ignight.Objects;

public class ChatObject {
    private String chatId;
    private String chatName;
    private String targetUserId;

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
