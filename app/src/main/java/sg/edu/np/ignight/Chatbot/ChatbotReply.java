package sg.edu.np.ignight.Chatbot;

import com.google.cloud.dialogflow.v2.DetectIntentResponse;

public interface ChatbotReply {
    void getResponse(DetectIntentResponse response);
}
