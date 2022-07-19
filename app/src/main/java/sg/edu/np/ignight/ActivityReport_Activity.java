package sg.edu.np.ignight;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import com.google.firebase.storage.FirebaseStorage;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;


public class ActivityReport_Activity extends AppCompatActivity {
    // init fields
    private ArrayList<PieEntry> pieChartData;
    private ArrayList<BarEntry> barChartData;

    private PieChart pieChart;
    private BarChart barChart;

    private ImageButton backButton2;
    private TextView timeUsageTestTextView;

    private String uid, timeSpentToday;


    // Testing
    TreeMap<Long,String> treeMap = new TreeMap<>();

    @SuppressLint("SetTextI18n")
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
        myRef.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Data entries for charts
        pieChart = findViewById(R.id.PieChart);
        barChart = findViewById(R.id.BarChart);
        barChartData = new ArrayList<>();
        pieChartData = new ArrayList<>();

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













        // Testing new way of collecting data
        SharedPreferences spForDay = getSharedPreferences("dataForDay", Context.MODE_PRIVATE);
        SharedPreferences.Editor spForDayEditor = spForDay.edit();

        UsageStatsManager mUsageStatsManager = (UsageStatsManager)getSystemService(USAGE_STATS_SERVICE);
        //UsageStats usageStats;
        String PackageName = "com.android.chrome" ;
        long TimeInforground;
        int minutes,seconds,hours,h=0,m=0,s=0;
        long time = System.currentTimeMillis();

        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH,1);
        c.add(Calendar.HOUR_OF_DAY,0);
        c.add(Calendar.MINUTE,0);
        c.add(Calendar.SECOND,0);
        c.add(Calendar.MILLISECOND,0);
        long howMany = c.getTimeInMillis();
        Log.i("midnighttime",Long.toString(howMany)+" "+Long.toString(time));

        timeUsageTestTextView = (TextView) findViewById(R.id.timeUsageTestTextView);
        List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time-10*1000, time);
        if(stats != null) {
            SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
            for (UsageStats usageStats : stats) {
                TimeInforground = usageStats.getTotalTimeInForeground();
                PackageName = usageStats.getPackageName();
                Log.i("pkg_name", "PackageName is" + PackageName);

                minutes = (int) ((TimeInforground / (1000 * 60)) % 60);
                seconds = (int) (TimeInforground / 1000) % 60;
                hours = (int) ((TimeInforground / (1000 * 60 * 60)) % 24);

                h = h + hours;
                m = m + minutes;
                if (m >= 60) {
                    h = h + m / 60;
                    m = m % 60;
                }
                s = s + seconds;
                if (s >= 60) {
                    m = m + s / 60;
                    s = s % 60;
                }

                spForDayEditor.putInt(PackageName, hours * 60 + minutes);
                spForDayEditor.apply();

                treeMap.put(TimeInforground, PackageName);

                if(PackageName.equals("sg.edu.np.ignight")){
                    timeSpentToday = "You spent " + hours + "h," + minutes + "min" + " on IgNight today.";
                    timeUsageTestTextView.setText(timeSpentToday);
                }
            }
        }

    }
}