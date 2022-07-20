package sg.edu.np.ignight;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse;
import com.google.cloud.dialogflow.v2beta1.EventInput;
import com.google.cloud.dialogflow.v2beta1.Intent;
import com.google.cloud.dialogflow.v2beta1.QueryInput;
import com.google.cloud.dialogflow.v2beta1.SessionName;
import com.google.cloud.dialogflow.v2beta1.SessionsClient;
import com.google.cloud.dialogflow.v2beta1.SessionsSettings;
import com.google.cloud.dialogflow.v2beta1.TextInput;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import sg.edu.np.ignight.Chatbot.ChatbotAdapter;
import sg.edu.np.ignight.Chatbot.ChatbotReply;
import sg.edu.np.ignight.Chatbot.QueryChatbot;
import sg.edu.np.ignight.Chatbot.SuggestedReplyAdapter;
import sg.edu.np.ignight.Chatbot.SuggestedReplyObject;
import sg.edu.np.ignight.Objects.ChatbotMessageObject;

public class ChatbotActivity extends AppCompatActivity implements ChatbotReply {

    private RecyclerView chatbotMessageRV, suggestedReplyRV;
    private RecyclerView.Adapter chatbotAdapter, suggestedReplyAdapter;
    private LinearLayoutManager chatbotLayoutManager, suggestedReplyLayoutManager;

    private ArrayList<ChatbotMessageObject> messageList;
    private ArrayList<SuggestedReplyObject> replyList;

    private TextInputEditText chatbotInput;
    private ImageButton chatbotSend;
    private ImageView scrollToChatBottomButton;

    // for chatbot
    private SessionsClient sessionsClient;
    private SessionName sessionName;
    private String uuid = UUID.randomUUID().toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        chatbotInput = findViewById(R.id.chatbotInput);
        chatbotSend = findViewById(R.id.chatbotSend);
        scrollToChatBottomButton = findViewById(R.id.chatbotScrollBottom);

        initChatbotRV();
        initSuggestedRepliesRV();
        initBot();

        ImageButton chatbotBack = findViewById(R.id.chatbotBack);
        chatbotBack.setOnClickListener(new View.OnClickListener() {  // finish activity when back button is clicked
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // show button to scroll to bottom of chat when the recyclerview is scrolled
        chatbotMessageRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int lastChildPosition = layoutManager.findLastCompletelyVisibleItemPosition();
                enableScrollToChatBottomButton(lastChildPosition < layoutManager.getItemCount() - 1);
            }
        });

        // scroll to the bottom of the chat when button is clicked then hide the scroll button
        scrollToChatBottomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chatbotMessageRV.smoothScrollToPosition(messageList.size() - 1);
                enableScrollToChatBottomButton(false);
            }
        });

        // send message/query when send button is clicked
        chatbotSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

    }

    // close the client when the activity is destroyed
    @Override
    protected void onDestroy() {
        super.onDestroy();
        sessionsClient.close();
    }

    // initialize bot connection/session
    private void initBot() {
        try {
            InputStream stream = this.getResources().openRawResource(R.raw.chatbot_credentials);  // get credentials file
            GoogleCredentials credentials = GoogleCredentials.fromStream(stream)
                    .createScoped(new ArrayList<String>() {
                        {
                            add("https://www.googleapis.com/auth/cloud-platform");
                        }
                    });
            String projectId = ((ServiceAccountCredentials) credentials).getProjectId();

            SessionsSettings.Builder settingsBuilder = SessionsSettings.newBuilder();
            SessionsSettings sessionsSettings = settingsBuilder.setCredentialsProvider(
                    FixedCredentialsProvider.create(credentials)).build();
            sessionsClient = SessionsClient.create(sessionsSettings);
            sessionName = SessionName.of(projectId, uuid);

            initConvo();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    // send query to bot with the input text/add new message to messageList and clear EditText
    private void sendMessage() {
        if (chatbotInput.getText() != null) {  // only process message if it is not empty/null
            String text = chatbotInput.getText().toString().trim();
            if (!text.isEmpty()) {
                queryBot(text);
                chatbotInput.setText("");
                showSuggestedReplyRV(false);  // hide suggested replies and clear list also
                ChatbotMessageObject newMessage = new ChatbotMessageObject(text, true);
                messageList.add(newMessage);
                chatbotAdapter.notifyDataSetChanged();
                chatbotMessageRV.smoothScrollToPosition(messageList.size() - 1);
            }
        }
    }

    // create and send query to bot
    private void queryBot(String text) {
        QueryInput input = QueryInput.newBuilder()
                .setText(
                        TextInput.newBuilder().setText(text).setLanguageCode("en-US")
                ).build();
        new QueryChatbot(this, sessionName, sessionsClient, input).execute();
    }

    // send query to bot to make bot initiate conversation
    private void initConvo() {
        QueryInput input = QueryInput.newBuilder()
                .setEvent(
                        EventInput.newBuilder()
                                .setName("Welcome")
                                .setLanguageCode("en-US")
                                .build()
                ).build();
        new QueryChatbot(this, sessionName, sessionsClient, input).execute();
    }

    // get response
    @Override
    public void getResponse(DetectIntentResponse response) {
        if (response != null) {
            String reply = response.getQueryResult().getFulfillmentText();  // get text response (to display as normal chat message)

            List<Intent.Message> fulfillmentMessagesList = response.getQueryResult().getFulfillmentMessagesList();
            if (fulfillmentMessagesList.size() > 1) {  // response contains custom payload (suggested replies)
                Map<String, Value> fieldsMap = fulfillmentMessagesList.get(1).getPayload().getFieldsMap();

                if (fieldsMap.get("options") != null) {
                    JsonArray jsonArray = valueToJsonElement(fieldsMap.get("options")).getAsJsonArray();

                    showSuggestedReplyRV(true);

                    for (JsonElement element : jsonArray) {  // get suggested reply text and add to replyList
                        String suggestedReplyText = element.getAsJsonObject().get("text").toString().replace("\"", "");
                        SuggestedReplyObject suggestedReply = new SuggestedReplyObject(suggestedReplyText);

                        replyList.add(suggestedReply);
                        suggestedReplyAdapter.notifyDataSetChanged();
                    }
                }
            }
            if (!reply.isEmpty()) {  // there is text response
                messageList.add(new ChatbotMessageObject(reply, false));
                chatbotAdapter.notifyDataSetChanged();
                chatbotMessageRV.smoothScrollToPosition(messageList.size() - 1);
            }
            else {
                Toast.makeText(this, "Something went wrong, please try again.", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(this, "Failed to connect, please try again later.", Toast.LENGTH_SHORT).show();
        }
    }

    // methods to parse value from response to valid json format
    // https://www.tabnine.com/web/assistant/code/rs/5c77e296df79be0001da9678#L74
    public JsonObject structToJsonObject(Struct metadata) {
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry<String, Value> entry : metadata.getFieldsMap().entrySet()) {
            jsonObject.add(entry.getKey(), valueToJsonElement(entry.getValue()));
        }
        return jsonObject;
    }

    // methods to parse value from response to valid json format
    // https://www.tabnine.com/web/assistant/code/rs/5c77e296df79be0001da9678#L74
    private JsonElement valueToJsonElement(Value value) {
        if (value.hasStructValue()) {
            return structToJsonObject(value.getStructValue());
        } else if (value.hasListValue()) {
            JsonArray values = new JsonArray();
            for (Value listElement : value.getListValue().getValuesList()) {
                values.add(valueToJsonElement(listElement));
            }
            return values;
        } else if (value.getKindCase().equals(Value.KindCase.NULL_VALUE)) {
            return JsonNull.INSTANCE;
        } else if (value.getKindCase().equals(Value.KindCase.BOOL_VALUE)) {
            return new JsonPrimitive(value.getBoolValue());
        } else if (value.getKindCase().equals(Value.KindCase.STRING_VALUE)) {
            return new JsonPrimitive(value.getStringValue());
        } else if (value.getKindCase().equals(Value.KindCase.NUMBER_VALUE)) {
            return new JsonPrimitive(value.getNumberValue());
        } else {
            Log.d("valueToJsonElement", "Unknown metadata value field type");
            return null;
        }
    }

    // show/hide button to scroll to bottom of chat
    private void enableScrollToChatBottomButton(boolean toEnable) {
        scrollToChatBottomButton.setVisibility((toEnable)?View.VISIBLE: View.GONE);
        scrollToChatBottomButton.setClickable(toEnable);
    }

    // initialize chatbot recyclerview
    private void initChatbotRV() {
        messageList = new ArrayList<>();
        chatbotMessageRV = findViewById(R.id.chatbotMessageRV);
        chatbotMessageRV.setNestedScrollingEnabled(false);
        chatbotMessageRV.setHasFixedSize(false);
        chatbotLayoutManager = new LinearLayoutManager(getApplicationContext());
        chatbotLayoutManager.setStackFromEnd(true);
        chatbotMessageRV.setLayoutManager(chatbotLayoutManager);
        chatbotAdapter = new ChatbotAdapter(messageList);
        chatbotMessageRV.setAdapter(chatbotAdapter);
        chatbotMessageRV.setItemAnimator(new DefaultItemAnimator());
    }

    // initialize suggested replies recyclerview
    private void initSuggestedRepliesRV() {
        replyList = new ArrayList<>();
        suggestedReplyRV = findViewById(R.id.suggestedRepliesRV);
        suggestedReplyRV.setNestedScrollingEnabled(false);
        suggestedReplyRV.setHasFixedSize(false);
        suggestedReplyLayoutManager = new LinearLayoutManager(getApplicationContext());
        suggestedReplyLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        suggestedReplyRV.setLayoutManager(suggestedReplyLayoutManager);
        suggestedReplyAdapter = new SuggestedReplyAdapter(replyList, chatbotInput);
        suggestedReplyRV.setAdapter(suggestedReplyAdapter);
        suggestedReplyRV.setItemAnimator(new DefaultItemAnimator());
    }

    // show/hide suggestedReplyRV
    private void showSuggestedReplyRV(boolean show) {
        suggestedReplyRV.setVisibility(show?View.VISIBLE:View.GONE);
        if (!show) {  // also clear replyList if hide
            replyList.clear();
            suggestedReplyAdapter.notifyDataSetChanged();
        }
    }
}