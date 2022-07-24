package sg.edu.np.ignight;

import static android.app.AppOpsManager.MODE_ALLOWED;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;


public class ActivityReport_Activity extends AppCompatActivity {

    // init fields
    private ArrayList<PieEntry> pieChartData;
    private ArrayList<BarEntry> barChartData;

    private PieChart pieChart;
    private BarChart barChart;

    private ImageButton backButton2;
    private TextView timeUsageTestTextView;

    private String uid, timeSpentToday, packageName;

    private long foregroundTime, time;

    private int hours1, hours2, minutes1, minutes2, seconds1, seconds2,
            USAGE_STATS_PERMISSION_CODE, numberOfChats;

    private HashMap<String, String> chatIDtargetUserIDkeypair,targetUserIDusernamesTargetUserIDkeypair;
    private HashMap<String, Integer> targetUserIDtotalNumberOfTextsKeypair;

    @SuppressLint("LogNotTimber")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activityreport_activity);

        // Return back to main menu
        backButton2 = findViewById(R.id.backButton3);
        backButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent backToMainMenu = new Intent(getApplicationContext(), MainMenuActivity.class);
                backToMainMenu.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(backToMainMenu);
                finish();
            }
        });

        // Usage stats permissions check
        if(!usageStatsPermissionsRequest(ActivityReport_Activity.this)){
            Toast.makeText(ActivityReport_Activity.this,
                    "Please grant usage permission for this feature to work.",
                    Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            // Returns back to the main menu if the user still refuses the permission
            // after the prompt. This is so that the user does not see the activity page
            // without any missing values
            if(!usageStatsPermissionsRequest(ActivityReport_Activity.this)){
                Toast.makeText(ActivityReport_Activity.this,
                        "Please grant usage permission for this feature to work.",
                        Toast.LENGTH_LONG).show();
            }
        }

        // Fields for charts
        pieChart = findViewById(R.id.PieChart);
        barChart = findViewById(R.id.BarChart);
        barChartData = new ArrayList<>();
        pieChartData = new ArrayList<>();


        // After calling the following functions below, the following pieces of data are retrieved:
        // barChartData.add(new BarEntry(1, foregroundTime)); <- added already in function
        // chatIDtargetUserIDkeypair,
        // targetUserIDtotalNumberOfTextsKeypair;
        // targetUserIDusernames;
        generateIgNightChatData();
        getTimeSpentToday();
        // call suggestions function here to set text

        // We add the retrieved data to the pieChartData arrayList to display the data
        // However, we only want the top 3 IgNights, so we filter the
        // targetUserIDtotalNumberOfTextsKeypair hashMap for top 3 most texts first
        List<Map.Entry<String, Integer>> targetUserIDtotalNumberOfTextsKeypair2 = topThreeIgNightUserChats(targetUserIDtotalNumberOfTextsKeypair);
        for (Map.Entry<String, Integer> entry : targetUserIDtotalNumberOfTextsKeypair2){
            String targetUserID = entry.getKey();
            Integer totalNumberOfTexts = entry.getValue();

            // Iterates through targetUserIDusernames hashMap to match the username
            // to the correct targetUserID
            String targetUsername = "";
            for(Map.Entry<String, String> entry2: targetUserIDusernamesTargetUserIDkeypair.entrySet()) {
                // If the right username is found,
                if(entry2.getValue().equals(targetUserID)){
                    // Set username value
                    targetUsername = entry2.getKey().toString();
                }
            }

            // We now have the targetUsername and the respective totalNumberOfTexts between
            // the currentUser and the targetUser
            // Adds data here to the pieChart ArrayList
            pieChartData.add(new PieEntry(totalNumberOfTexts, targetUsername));
        }



        // barChart and pieChart display here

        // init bar data set
        BarDataSet barDataSet = new BarDataSet(barChartData, "Test1");
        // set colors and hide draw value
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        barDataSet.setDrawValues(false);
        // input bar data
        barChart.setData(new BarData(barDataSet));
        // set animation
        barChart.animateY(1100);
        // set graph description
        barChart.getDescription().setText("Test2");
        barChart.getDescription().setTextColor(Color.BLUE);

        // init pie data set
        PieDataSet pieDataSet = new PieDataSet(pieChartData, "");
        // set colors and hide draw value
        pieDataSet.setColors(ColorTemplate.PASTEL_COLORS);
        // input bar data
        pieChart.setData(new PieData(pieDataSet));
        // set animation
        pieChart.animateXY(1100,1100);

        giveDatingSuggestions();
    }

    // Obtain time spent on the IgNight app and displays it
    // Also adds data to the barChartData for graph display
    private void getTimeSpentToday(){
        // Instantiate some fields
        SharedPreferences sharedPreferences = getSharedPreferences("dataForDay", Context.MODE_PRIVATE);
        SharedPreferences.Editor sPdayEdit = sharedPreferences.edit();
        UsageStatsManager mUsageStatsManager = (UsageStatsManager)getSystemService(USAGE_STATS_SERVICE);
        time = System.currentTimeMillis();
        hours2 = minutes2 = seconds2 = 0;
        timeUsageTestTextView = (TextView) findViewById(R.id.timeUsageTestTextView);

        List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time-(1000), time);
        System.out.println(Arrays.toString(stats.toArray()));
        if(stats != null) {
            for (UsageStats usageStats : stats) {
                // Shows the time spent on the IgNight app itself
                packageName = usageStats.getPackageName();
                if(packageName.equals("sg.edu.np.ignight")){
                    foregroundTime = usageStats.getTotalTimeInForeground();

                    // Time unit conversion logic
                    hours1 = (int) ((foregroundTime / (1000 * 60 * 60)) % 24);
                    minutes1 = (int) ((foregroundTime / (1000 * 60)) % 60);

                    // Testing to check if the phone detects other packages and thus sg.edu.np.ignight
                    // Log.d line here sees if the permissions are enabled so all stats can be seen
                    Log.d("PackageName", packageName + hours1 + "h," + minutes1 + "min");

                    // Show time spent in textView below bar chart
                    timeSpentToday = "You spent " + hours1 + "h," + minutes1 + "min" + " on IgNight today.";
                    timeUsageTestTextView.setText(timeSpentToday);

                    // Adds data to barChart list, so that the barChart can show the data
                    barChartData.add(new BarEntry(1, foregroundTime));
                    // The time the user spent will be entered as the second value on the chart
                    // for comparative purposes to the user's "goal" time

                    // Add time to sharedPreference to save it
                    sPdayEdit.putInt(packageName, hours1 * 60 + minutes1);
                    sPdayEdit.apply();
                }
            }
        }
    }

    /*

   //Prototype - https://stackoverflow.com/questions/36238481/android-usagestatsmanager-not-returning-correct-daily-results?noredirect=1&lq=1
   //Translation from Kotlin
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getTimeSpentToday2(){
        ZoneId dateTime = ZoneId.of("UTC");
        ZoneId defaultZone = ZoneId.systemDefault();

        dateTime startTime = Date.(defaultZone).wi
    }

     */

    private void generateIgNightChatData(){
        // Firebase will retrieve chat information.
        // Usage time information is generated by the device and will thus stay on the device.
        // Firebase logic
        // Retrieves current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //get the current user's UID
        uid = user.getUid();
        // Saving to Firebase database
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/");
        // getting the child user
        DatabaseReference myRef = database.getReference("user");
        // Retrieves the number of chat messages sent to each IgNighted user,
        // and stores it in a key:pair dictionary
        // It does it everytime the user opens the activity report so that it refreshes correctly everytime
        myRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Retrieve the chat IDs of all chats that the user is in
                // Puts all of the chat IDs and associated target user IDs into a hashMap
                // targetUserId refers to the ID of the other user that the current user chats with
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    String chatId = dataSnapshot.getKey();
                    String targetUserId = (String) dataSnapshot.getValue();
                    chatIDtargetUserIDkeypair.put(chatId, targetUserId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ActivityReport_Activity.this,
                        "Error retrieving chat information",
                        Toast.LENGTH_LONG)
                        .show();
            }
        });

        // Number of chats here determine the number of entries in the pieChart display later on
        numberOfChats = chatIDtargetUserIDkeypair.size();

        // Retrieve each chat via chatID with a loop to find the number of texts in total
        // ChatID are keys in chatIDtargetUserIDkeypair hashmap, so we loop through the keys in the hashMap
        for(String key : chatIDtargetUserIDkeypair.keySet()){
            //
            DatabaseReference myRef2 = database.getReference("chat");
            myRef2.child(key).child("messages").addListenerForSingleValueEvent(new ValueEventListener() {
                @SuppressLint("LogNotTimber")
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Counts the number of texts sent by both users in the chat in total
                    Integer totalNumberOfTextsSent = (int) (long) dataSnapshot.getChildrenCount();
                    targetUserIDtotalNumberOfTextsKeypair.put(key, totalNumberOfTextsSent);
                    Log.d("targetUserIDtotalNumberOfTextsKeypair", key + " " + totalNumberOfTextsSent);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ActivityReport_Activity.this,
                            "Error retrieving chat information",
                            Toast.LENGTH_LONG)
                            .show();
                }
            });
        }

        // The following hashmaps with their own respective data have now been filled:
        // chatIDtargetUserIDkeypair and targetUserIDtotalNumberOfTextsKeypair

        // The relevant data from these hashmaps now have to be inserted into
        // the ArrayList of the pieChart
        for(Map.Entry<String, Integer> entry: targetUserIDtotalNumberOfTextsKeypair.entrySet()) {
            // Finds the associated username with each targetUserID
            // Retrieves the usernames associated with the targetUserIDs and puts
            // them into an arrayList targetUserIDusernames
            // targetUserIDusernames will be used for displaying the username on the pieChart
            myRef.child(entry.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String targetUsernameRetrieved = snapshot.child("username")
                            .getValue().toString();
                    targetUserIDusernamesTargetUserIDkeypair.put(targetUsernameRetrieved, entry.getKey());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ActivityReport_Activity.this,
                            "Error retrieving chat information",
                            Toast.LENGTH_LONG)
                            .show();
                }
            });
        }
    }

    // Do NOT invert method.
    private boolean usageStatsPermissionsRequest(Context context){
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.getPackageName());
        return mode == MODE_ALLOWED;
    }

    // Arranges hashMap targetUserIDtotalNumberOfTextsKeypair by top 3
    // Basically does nothing with hashMaps that have 3 entries and below
    private static <K, V extends Comparable<? super V>> List<Map.Entry<K, V>> topThreeIgNightUserChats(Map<K, V> map)
    {
        Comparator<? super Map.Entry<K, V>> comparator = new Comparator<Map.Entry<K, V>>()
        {
            @Override
            public int compare(Map.Entry<K, V> entry, Map.Entry<K, V> entry2)
            {
                return (entry.getValue()).compareTo(entry2.getValue());
            }
        };
        PriorityQueue<Map.Entry<K, V>> highest = new PriorityQueue<Map.Entry<K,V>>(3, comparator);
        for (Map.Entry<K, V> entry : map.entrySet())
        {
            highest.offer(entry);
            while (highest.size() > 3)
            {
                highest.poll();
            }
        }
        List<Map.Entry<K, V>> topThreeIgNightUserChatsHashMap = new ArrayList<Map.Entry<K,V>>();
        while (highest.size() > 0)
        {
            topThreeIgNightUserChatsHashMap.add(highest.poll());
        }
        return topThreeIgNightUserChatsHashMap;
    }

    // Gives dating suggestions based on the statistics
    private void giveDatingSuggestions(){
        TextView suggestionsText = findViewById(R.id.activityReportTitle);
        if(chatIDtargetUserIDkeypair.size() < 3){
            suggestionsText.setText("Try Harder!");
        }
        else if(chatIDtargetUserIDkeypair.size() > 10){
            suggestionsText.setText("Slow Down!");
        }
        if(hours2 > 5){
            suggestionsText.setText("You're Addicted! Slow Down!");
        }
    }
}

