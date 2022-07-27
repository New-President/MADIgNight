package sg.edu.np.ignight.ChatNotifications;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import sg.edu.np.ignight.R;

public class ChatNotificationSender {

    private String fcmToken;
    private String senderID;
    private String chatID;
    private String messageID;
    private Context context;

    private RequestQueue requestQueue;
    private final String postUrl = "https://fcm.googleapis.com/fcm/send";
    private String fcmServerKey;

    public ChatNotificationSender(String fcmToken, String senderID, String chatID, String messageID, Context context) {
        this.fcmToken = fcmToken;
        this.senderID = senderID;
        this.chatID = chatID;
        this.messageID = messageID;
        this.context = context;
    }

    // send notification using Volley
    public void sendNotification() {
        fcmServerKey = context.getResources().getString(R.string.fcm_server_key);

        requestQueue = Volley.newRequestQueue(context);
        JSONObject jsonObject = new JSONObject();

        try {
            // create request
            jsonObject.put("to", fcmToken);

            JSONObject data = new JSONObject();  // add custom data
            data.put("purpose", "message");
            data.put("senderID", senderID);
            data.put("chatID", chatID);
            data.put("messageID", messageID);

            jsonObject.put("data", data);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, postUrl, jsonObject, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.v("sendRequest", "got response");
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            }) {
                @Override
                public Map<String, String> getHeaders() {  // set headers
                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", "application/json");
                    header.put("authorization", "key=" + fcmServerKey);
                    return header;
                }
            };

            requestQueue.add(request);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
