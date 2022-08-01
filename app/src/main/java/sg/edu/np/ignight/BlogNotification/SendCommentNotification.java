package sg.edu.np.ignight.BlogNotification;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
        FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Get the fcmServer key that is placed in the database
                fcmServerKey = snapshot.getValue().toString();
                // make a new volley request
                requestQueue1 = Volley.newRequestQueue(context);
                JSONObject jsonObject = new JSONObject();

                try {
                    // create request
                    jsonObject.put("to", fcmToken);
                    // adding the required data into the JSONObject that would be passed into the NotificationService
                    JSONObject data = new JSONObject();  // add custom data
                    data.put("purpose", "comment");
                    data.put("message", message); // content of the message
                    data.put("senderID", senderID); // Sender's UID
                    data.put("blogID", blogID); //BlogID

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
                            header.put("content-type", "application/json");  // Using json as a form of passing the data
                            header.put("authorization", "key=" + fcmServerKey); // add the fcmServerKey
                            return header;
                        }
                    };
                    // Add the JSONObjectrequest into a requestQueue

                    requestQueue1.add(request);

                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error.getMessage());
            }
        });
    }
}
