package sg.edu.np.ignight;

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

public class SendBlogNotification {

    private String fcmToken;
    private String title;
    private String body;
    private String senderID;
    private Context context;

    private RequestQueue requestQueue;
    private final String postUrl = "https://fcm.googleapis.com/fcm/send";
    private String fcmServerKey;

    public SendBlogNotification(String fcmToken, String senderID, String title, String body, Context context) {
        this.fcmToken = fcmToken;
        this.title = title;
        this.senderID = senderID;
        this.body = body;
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
            data.put("activity", "Blog");
            data.put("title", title);
            data.put("senderID", senderID);
            data.put("body", body);

            jsonObject.put("data", data);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, postUrl, jsonObject, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.v("sendRequest", "got response" + jsonObject);
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
