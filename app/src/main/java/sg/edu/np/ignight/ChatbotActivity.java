package sg.edu.np.ignight;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse;
import com.google.cloud.dialogflow.v2beta1.QueryInput;
import com.google.cloud.dialogflow.v2beta1.SessionName;
import com.google.cloud.dialogflow.v2beta1.SessionsClient;
import com.google.cloud.dialogflow.v2beta1.SessionsSettings;
import com.google.cloud.dialogflow.v2beta1.TextInput;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.UUID;

import sg.edu.np.ignight.Chatbot.ChatbotAdapter;
import sg.edu.np.ignight.Chatbot.ChatbotReply;
import sg.edu.np.ignight.Chatbot.QueryChatbot;
import sg.edu.np.ignight.Objects.ChatbotMessageObject;

public class ChatbotActivity extends AppCompatActivity implements ChatbotReply {

    private RecyclerView chatbotMessageRV;
    private RecyclerView.Adapter chatbotAdapter;
    private LinearLayoutManager layoutManager;

    private ArrayList<ChatbotMessageObject> messageList;

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

        initRecyclerView();
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
            InputStream stream = this.getResources().openRawResource(R.raw.chatbot_credentials);  // get credentials
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

    // get response and add new message to messageList
    @Override
    public void getResponse(DetectIntentResponse response) {
        if (response != null) {
            String reply = response.getQueryResult().getFulfillmentText();

            if (!reply.isEmpty()) {
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

    // show/hide button to scroll to bottom of chat
    private void enableScrollToChatBottomButton(boolean toEnable) {
        scrollToChatBottomButton.setVisibility((toEnable)?View.VISIBLE: View.GONE);
        scrollToChatBottomButton.setClickable(toEnable);
    }

    // initialize recyclerview
    private void initRecyclerView() {
        messageList = new ArrayList<>();
        chatbotMessageRV = findViewById(R.id.chatbotMessageRV);
        chatbotMessageRV.setNestedScrollingEnabled(false);
        chatbotMessageRV.setHasFixedSize(false);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setStackFromEnd(true);
        chatbotMessageRV.setLayoutManager(layoutManager);
        chatbotAdapter = new ChatbotAdapter(messageList);
        chatbotMessageRV.setAdapter(chatbotAdapter);
        chatbotMessageRV.setItemAnimator(new DefaultItemAnimator());
    }
}