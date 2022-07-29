package sg.edu.np.ignight.Chatbot;

import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse;

public interface ChatbotReply {
    // interface to handle chatbot response
    void getResponse(DetectIntentResponse response);
}
