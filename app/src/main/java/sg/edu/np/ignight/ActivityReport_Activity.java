package sg.edu.np.ignight;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageButton;
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
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import sg.edu.np.ignight.Objects.UserObject;

public class ActivityReport_Activity extends AppCompatActivity {
    // init fields
    private ArrayList<PieEntry> pieChartData;
    private ArrayList<BarEntry> barChartData;
    private List<UsageStats> dailyTimeTrackingData;

    private PieChart pieChart;
    private BarChart barChart;

    private ImageButton backButton2;

    private Boolean isTracking;

    private String uid;
    private String IgNightCounter = "IgNight Counter";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sPedit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activityreport_activity);

        pieChart = findViewById(R.id.PieChart);
        barChart = findViewById(R.id.BarChart);

        // Data entries for charts
        barChartData = new ArrayList<>();
        pieChartData = new ArrayList<>();

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





        // Firebase logic
        // Retrieves current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //get the current user's UID
        uid = user.getUid();
        // Saving to Firebase database
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/");
        // getting the child user
        DatabaseReference myRef = database.getReference("user");
        // Check if the current user has an isTracking child
        myRef.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // DO NOT SIMPLIFY
                if(snapshot.hasChild("isTracking")){
                    isTracking = true;
                }else{
                    isTracking = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });







        // Data tracking logic
        // Retrieves isTracking child from database to check if the user wants to be
        // tracked.

        // If user is not currently tracking
        if(!isTracking){
            new AlertDialog.Builder(getApplicationContext())
                    .setTitle("Start Tracking")
                    .setMessage("Would you like to start tracking?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Creates isTracking child within the user
                            // isTracking will contain user's activity info, so that it can be accessed on the same account in another device
                            myRef.child(uid).child("isTracking");

                            // The activity shows your weekly usage in a week. So the WeeklyTimeSpent child will have 7 children for 7 days
                            // Days of the week will be represented by numbers from 1 to 7, but the actual graph will display the actual days
                            for (int i1 = 1; i1 < 8; i1++){
                                myRef.child(uid).child("isTracking").child(String.valueOf(i1));
                            }
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // If user does not want to do activity tracking, they get back to the home menu
                            Intent backToMainMenu = new Intent(getApplicationContext(), MainMenuActivity.class);
                            backToMainMenu.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(backToMainMenu);
                            finish();
                        }
                    });
        }


        /*
        // Obtain time spent in the app
        // Setting can only be accessed by IgNight
        sharedPreferences = getSharedPreferences("IgNight",MODE_PRIVATE);
        if(!checkUsageStatsAllowedOrNot()){
            // Ask user to grant permission for tracking to work
            Intent usageAccessIntent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            usageAccessIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(usageAccessIntent);

            if(checkUsageStatsAllowedOrNot()){
                startService(new Intent(ActivityReport_Activity.this, BackgroundTrackingService.class));
            }
            else{
                Toast.makeText(getApplicationContext(), "Enable permissions for activtiy report tracking service to work.")
                        .setDuration(Toast.LENGTH_LONG)
                        .show();
            }
        }
        else{
            startService(new Intent(ActivityReport_Activity.this, BackgroundTrackingService.class));
        }
        // test_View = findViewById(R.id.testView);

        TimerTask updateView = new TimerTask(){

            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
        };
        */




        // Testing data input for bar chart and pie chart
        for (int i=1; i<10; i++){
            float value = (float) (i*10.0);
            // per bar chart entry
            BarEntry testBarEntry = new BarEntry(i, value);
            // add data into list
            barChartData.add(testBarEntry);

            // per pie chart entry
            PieEntry testPieEntry = new PieEntry(i, value);
            // add data into list
            pieChartData.add(testPieEntry);
        }
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
        PieDataSet pieDataSet = new PieDataSet(pieChartData, "Test3");
        // set colors and hide draw value
        pieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        // input bar data
        pieChart.setData(new PieData(pieDataSet));
        // set animation
        pieChart.animateXY(1100,1100);
        // hide description
        pieChart.getDescription().setEnabled(false);




    }



}