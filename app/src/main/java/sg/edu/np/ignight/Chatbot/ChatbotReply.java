package sg.edu.np.ignight.Chatbot;

import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse;

public interface ChatbotReply {
    void getResponse(DetectIntentResponse response);
}
