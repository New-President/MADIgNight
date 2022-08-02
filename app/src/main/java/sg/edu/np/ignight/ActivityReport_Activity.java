package sg.edu.np.ignight;

import static android.app.AppOpsManager.MODE_ALLOWED;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.github.mikephil.charting.formatter.ValueFormatter;
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

    private PieChart pieChart;
    private BarChart barChart;

    private ImageButton backButton2;
    private Button setGoalButton;
    private TextView timeUsageTestTextView;

    private String uid, timeSpentToday, packageName;

    private long foregroundTime, time;

    private int hours1, hours2, minutes1, setTimeLimit;

    private EditText inputText;

    private FirebaseUser user;
    private FirebaseDatabase database;
    private DatabaseReference myRef, myRef2;

    BarDataSet barDataset1, barDataset2;
    BarData barData;

    @SuppressLint("LogNotTimber")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activityreport_activity);

        // Firebase will retrieve chat information.
        // Firebase logic
        // Retrieves current user

        user = FirebaseAuth.getInstance().getCurrentUser();
        //get the current user's UID
        uid = user.getUid();
        database = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/");
        // getting the reference
        myRef = database.getReference("user");
        //myRef2 = database.getReference("chat");

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
        pieChartData = new ArrayList<>();

        // After calling the generateIgNightChatData function below, the following pieces of data are retrieved:
        // barChartData.add(new BarEntry(1, foregroundTime)); <- added already in function
        // chatIDtargetUserIDkeypair,
        // targetUserIDtotalNumberOfTextsKeypair;
        // targetUserIDusernames;
        HashMap<String, String> chatIDtargetUserIDkeypair = new HashMap<String, String>();
        HashMap<String, String>targetUserIDusernamesTargetUserIDkeypair = new HashMap<String, String>();
        HashMap<String, Integer> targetUserIDtotalNumberOfTextsKeypair = new HashMap<String, Integer>();
        generateIgNightChatData(chatIDtargetUserIDkeypair,
                targetUserIDusernamesTargetUserIDkeypair,
                targetUserIDtotalNumberOfTextsKeypair);
        getTimeSpentToday();

        setGoalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // If user chooses to edit their usage time goal
                try{
                    setTimeLimit = Integer.parseInt(String.valueOf(inputText.getText()));
                    Log.d("newUsage", String.valueOf(setTimeLimit));
                    myRef.child(uid).child("usageTimeLimit").setValue(setTimeLimit);
                    Toast.makeText(ActivityReport_Activity.this,
                            "Exit and return to see new bar chart value.",
                            Toast.LENGTH_LONG)
                            .show();
                }
                catch (Exception e){
                    Toast.makeText(ActivityReport_Activity.this,
                            "Enter a number.",
                            Toast.LENGTH_LONG)
                            .show();
                }
            }
        });

    }

    private ArrayList<BarEntry> barChartDataValues(int i){
        ArrayList<BarEntry> dataValues = new ArrayList<BarEntry>();
        dataValues.add(new BarEntry(0, i));
        return dataValues;
    }
    private ArrayList<BarEntry> barChartDataValues2(int i){
        ArrayList<BarEntry> dataValues = new ArrayList<BarEntry>();
        dataValues.add(new BarEntry(1, i));
        return dataValues;
    }
    // Obtain time spent on the IgNight app and displays it
    // Also adds data to the barChartData for graph display
    private void getTimeSpentToday(){
        // Instantiate some fields
        SharedPreferences sharedPreferences = getSharedPreferences("dataForDay", Context.MODE_PRIVATE);
        SharedPreferences.Editor sPdayEdit = sharedPreferences.edit();
        UsageStatsManager mUsageStatsManager = (UsageStatsManager)getSystemService(USAGE_STATS_SERVICE);
        time = System.currentTimeMillis();
        hours2 = 0;
        timeUsageTestTextView = (TextView) findViewById(R.id.timeUsageTestTextView);
        barData = new BarData();

        List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time-(1000), time);
        System.out.println(Arrays.toString(stats.toArray()));
        if(stats != null) {
            for (UsageStats usageStats : stats) {
                // Shows the time spent on the IgNight app itself
                packageName = usageStats.getPackageName();

                // The system can only retrieve every single timing for each package in the phone.
                // This picks out the timing for IgNight
                if(packageName.equals("sg.edu.np.ignight")){
                    // Sets the usage time
                    foregroundTime = usageStats.getTotalTimeInForeground();

                    // Time unit conversion logic, time is retrieved in ms by the system
                    hours1 = (int) ((foregroundTime / (1000 * 60 * 60)) % 24);
                    minutes1 = (int) ((foregroundTime / (1000 * 60)) % 60);

                    // Show time spent in textView below bar chart
                    timeSpentToday = "You spent " + hours1 + "h," + minutes1 + "min" + " on IgNight today.";
                    timeUsageTestTextView.setText(timeSpentToday);

                    // Adds data to barChart return list, so that the barChart can show the data
                    barDataset1 = new BarDataSet(barChartDataValues((int) (foregroundTime/3600000)), "Time you spent today");
                    // The time the user spent will be entered as another value on the chart
                    // for comparative purposes to the user's "goal" time

                    // Accesses and stores limit in the database so that limit setting will be "synced"
                    // across different devices using the same account
                    inputText = (EditText)findViewById(R.id.editTextNumber);
                    setGoalButton = (Button) findViewById(R.id.setGoalButton);
                    myRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d("onDataChange", "onDataChangeSuccess");
                            // Checks for presence of existing data in the database

                            for(DataSnapshot ds : snapshot.getChildren()) {
                                if(ds.getKey().equals("usageTimeLimit")){
                                    Log.d("hasChild", "hasChild");
                                    // If the usage time limit child exists, set data and bar chart
                                    setTimeLimit = Integer.parseInt(ds.getValue().toString());
                                    Log.d("has setTimeLimit2", String.valueOf(setTimeLimit));
                                    barDataset2 = new BarDataSet(barChartDataValues2(setTimeLimit), "Your usage goal");
                                    barDataset2.setColor(Color.RED);
                                    barData.addDataSet(barDataset2);
                                }
                            }
                            barDataset1.setColor(Color.rgb(252,194,103)); // Colour here is #FCC267 IgNight yellow
                            barData.addDataSet(barDataset1);


                            // Remove description label and set barChart to display loaded data
                            barChart.getDescription().setEnabled(false);
                            // Nice animation
                            barChart.animateY(1100);
                            // Loads data and chart itself
                            barChart.setData(barData);
                            barChart.invalidate();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.d("onCancelled", "hasChild");
                            Toast.makeText(ActivityReport_Activity.this,
                                    "Error retrieving usage time information",
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
                    // Add time to sharedPreference to save it
                    sPdayEdit.putInt(packageName, hours1 * 60 + minutes1);
                    sPdayEdit.apply();
                }
            }
        }
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

    // Disable timber alert
    @SuppressLint("LogNotTimber")

    // Generates IgNight chat data before displaying it on a pieChart
    private void generateIgNightChatData(HashMap<String, String> chatIDtargetUserIDkeypair,
                                         HashMap<String, String> targetUserIDusernamesTargetUserIDkeypair,
                                         HashMap<String, Integer> targetUserIDtotalNumberOfTextsKeypair) {
        // Retrieves the number of chat messages sent to each IgNighted user,
        // and stores it in a key:pair dictionary
        // It does it everytime the user opens the activity report so that it refreshes correctly everytime
        DatabaseReference altRef = database.getReference("");
        altRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Retrieve the chat IDs of all chats that the user is in
                // Puts all of the chat IDs and associated target user IDs into a hashMap
                // targetUserId refers to the ID of the other user that the current user chats with\
                for (DataSnapshot dataSnapshot : snapshot
                        .child("user")
                        .child(uid)
                        .child("chats")
                        .getChildren()){

                    String chatId = dataSnapshot.getKey();
                    String targetUserId = (String) dataSnapshot.getValue();
                    chatIDtargetUserIDkeypair.put(chatId, targetUserId);

                    // Log testing
                    //Log.d("success1", chatId + " " + targetUserId);
                }

                for(Map.Entry<String,String> entry: chatIDtargetUserIDkeypair.entrySet()){
                    Integer totalNumberOfTextsSent = (int) (long) snapshot
                            .child("chat")
                            .child(entry.getKey())
                            .child("messages")
                            .getChildrenCount();
                    Log.d("testOndataChange", entry.getValue() + String.valueOf(totalNumberOfTextsSent));
                    targetUserIDtotalNumberOfTextsKeypair.put(entry.getValue(), totalNumberOfTextsSent);
                }

                // Alternate way of doing targetUserIDtotalNumberOfTextsKeypair if the current way fails
                /*
                for(Map.Entry<String,String> entry: chatIDtargetUserIDkeypair.entrySet()){
                    myRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Integer totalNumberOfTextsSent = (int) (long) snapshot
                                    .child(entry.getKey())
                                    .child("messages")
                                    .getChildrenCount();
                            Log.d("testOndataChange3", String.valueOf(totalNumberOfTextsSent) + entry.getKey());
                            targetUserIDtotalNumberOfTextsKeypair.put(entry.getValue(), totalNumberOfTextsSent);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(ActivityReport_Activity.this,
                                    "Error retrieving chat information",
                                    Toast.LENGTH_LONG)
                                    .show();
                        }

                    });
                    Log.d("async done", "success");

                }*/

                // Log testing
                //Log.d("async done2", "success");

                for(Map.Entry<String, Integer> entry: targetUserIDtotalNumberOfTextsKeypair.entrySet()){

                    // Log testing
                    //Log.d("success2", "success2");
                    String targetUsernameRetrieved = "";
                    try {
                        targetUsernameRetrieved = snapshot
                                .child("user")
                                .child(entry.getKey())
                                .child("username")
                                .getValue().toString();
                    }catch(Exception e){
                        Toast.makeText(ActivityReport_Activity.this,
                                "Error retrieving chat information",
                                Toast.LENGTH_LONG)
                                .show();
                    }


                    // Log testing
                    //Log.d("test3", entry.getKey());

                    targetUserIDusernamesTargetUserIDkeypair.put(targetUsernameRetrieved, entry.getKey());

                    // Log testing
                    //Log.d("targetUserIDusernamesTargetUserIDkeypair success", targetUsernameRetrieved);
                }


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

                            // Log testing
                            //Log.d("targetUsernameFound", targetUsername);
                        }
                    }

                    // We now have the targetUsername and the respective totalNumberOfTexts between
                    // the currentUser and the targetUser
                    // Adds data here to the pieChart ArrayList
                    pieChartData.add(new PieEntry(totalNumberOfTexts, targetUsername));

                    // Log testing
                    //Log.d("pieChartData", totalNumberOfTexts + " " + targetUsername);
                }

                // Checks if there is any data generated. If so, display the top IgNight
                if(targetUserIDtotalNumberOfTextsKeypair2.size()!=0){
                    // Finds the highest text count and the associated target user with it
                    Map.Entry<String, Integer> highestTextCount = null;
                    for(Map.Entry<String, Integer> entry3: targetUserIDtotalNumberOfTextsKeypair.entrySet())
                    {
                        if (highestTextCount == null || entry3.getValue().compareTo(highestTextCount.getValue()) > 0)
                        {
                            highestTextCount = entry3;
                        }
                    }
                    String topIgNightTargetUserID = highestTextCount.getKey();
                    String topIgNightTargetUsername = "";
                    for(Map.Entry<String, String> entry4: targetUserIDusernamesTargetUserIDkeypair.entrySet()) {
                        // If the right username is found,
                        if(entry4.getValue().equals(topIgNightTargetUserID)){
                            // Set username value
                            topIgNightTargetUsername = entry4.getKey().toString();
                        }
                    }

                    // Sets top IgNight by chat display
                    TextView topIgNightDisplayText = (TextView) findViewById(R.id.topIgNightDisplay);

                    // Log testing
                    //Log.d("Your top IgNight is", String.valueOf(topIgNightDisplayText));

                    topIgNightDisplayText.setText("Your top IgNight is " + topIgNightTargetUsername + ".");
                }

                // init pie data set
                PieDataSet pieDataSet = new PieDataSet(pieChartData, "");
                Log.d("pieDataSet", "success");
                // set colors and hide draw value
                pieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
                // input bar data
                pieChart.setData(new PieData(pieDataSet));
                // set animation
                pieChart.animateXY(1100,1100);
                // remove description label
                pieChart.getDescription().setEnabled(false);
                // adjust font & formatting
                pieChart.getLegend().setEnabled(false);
                pieDataSet.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return String.valueOf((int) Math.floor(value)) + " texts";
                    }
                });
                pieChart.setEntryLabelTextSize(23f);
                pieChart.setEntryLabelColor(Color.rgb(0,0,0));
                pieDataSet.setValueTextSize(23);
                pieDataSet.setValueTextColor(Color.rgb(0,0,0));


                // sets dating suggestions based on some set statistics
                TextView suggestionsText = findViewById(R.id.activityReportTitle);
                if(chatIDtargetUserIDkeypair.size() < 3){
                    suggestionsText.setText("Try Harder!");
                }
                if(chatIDtargetUserIDkeypair.size() >= 3){
                    suggestionsText.setText("Keep it up!");
                }
                if(hours2 > 5){
                    suggestionsText.setText("You're Addicted!");
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
    }

    // Calls usage permissions
    private boolean usageStatsPermissionsRequest(Context context){
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.getPackageName());
        return mode == MODE_ALLOWED;
    }
}

