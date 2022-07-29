package sg.edu.np.ignight.BlogNotification;

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

public class SendCommentNotification {

    private String fcmToken;
    private String message;
    private String blogID;
    private String senderID;
    private Context context;

    private RequestQueue requestQueue1;
    private final String postUrl = "https://fcm.googleapis.com/fcm/send";
    private String fcmServerKey;

    public SendCommentNotification(String fcmToken, String senderID, String message, String blogID, Context context) {
        this.fcmToken = fcmToken;
        this.message = message;
        this.senderID = senderID;
        this.blogID = blogID;
        this.context = context;
    }

    // send notification using Volley
    public void sendNotification() {
        fcmServerKey = context.getResources().getString(R.string.fcm_server_key);

        requestQueue1 = Volley.newRequestQueue(context);
        JSONObject jsonObject = new JSONObject();

        try {
            // create request
            jsonObject.put("to", fcmToken);

            JSONObject data = new JSONObject();  // add custom data
            data.put("purpose", "comment");
            data.put("message", message);
            data.put("senderID", senderID);
            data.put("blogID", blogID);

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
            requestQueue1.add(request);

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
