package sg.edu.np.ignight.Chatbot;

import android.os.AsyncTask;

import com.google.cloud.dialogflow.v2beta1.DetectIntentRequest;
import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse;
import com.google.cloud.dialogflow.v2beta1.QueryInput;
import com.google.cloud.dialogflow.v2beta1.SessionName;
import com.google.cloud.dialogflow.v2beta1.SessionsClient;

public class QueryChatbot extends AsyncTask<Void, Void, DetectIntentResponse> {

    private SessionName sessionName;
    private SessionsClient sessionsClient;
    private QueryInput queryInput;
    private ChatbotReply chatbotReply;

    public QueryChatbot(ChatbotReply chatbotReply, SessionName sessionName, SessionsClient sessionsClient, QueryInput queryInput) {
        this.chatbotReply = chatbotReply;
        this.sessionName = sessionName;
        this.sessionsClient = sessionsClient;
        this.queryInput = queryInput;
    }

    // build request and send with client
    @Override
    protected DetectIntentResponse doInBackground(Void... voids) {
        try {
            DetectIntentRequest request = DetectIntentRequest.newBuilder()
                    .setSession(sessionName.toString())
                    .setQueryInput(queryInput)
                    .build();
            return sessionsClient.detectIntent(request);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // call interface when response is received
    @Override
    protected void onPostExecute(DetectIntentResponse detectIntentResponse) {
        chatbotReply.getResponse(detectIntentResponse);
    }
}
