package sg.edu.np.ignight.Chatbot;

import android.os.AsyncTask;

import com.google.cloud.dialogflow.v2.DetectIntentRequest;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;

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

    // return the response
    @Override
    protected void onPostExecute(DetectIntentResponse detectIntentResponse) {
        chatbotReply.getResponse(detectIntentResponse);
    }
}
