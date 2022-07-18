package sg.edu.np.ignight.Objects;

public class ChatbotMessageObject {

    private String message;
    private boolean creator;

    public ChatbotMessageObject(String message, boolean creator) {
        this.message = message;
        this.creator = creator;
    }

    public String getMessage() {
        return message;
    }
    public boolean isCreator() {
        return creator;
    }
}
