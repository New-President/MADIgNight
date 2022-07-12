package sg.edu.np.ignight;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

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

public class ActivityReport_Activity extends AppCompatActivity {
    // init fields
    private ArrayList<PieEntry> pieChartData;
    private ArrayList<BarEntry> barChartData;
    private ArrayList<String> weeklyTimeTrackingData;

    private PieChart pieChart;
    private BarChart barChart;

    private ImageButton backButton2;

    private Boolean isTracking;

    private String uid;

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

        // If user is not currently
        if(isTracking == false){
            new AlertDialog.Builder(getApplicationContext())
                    .setTitle("Start Tracking")
                    .setMessage("Would you like to start tracking?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Creates isTracking child within the user
                            // isTracking will contain user's activity info, so that it can be accessed on the same account in another device


                            // The activity shows your weekly usage in a week. So the WeeklyTimeSpent child will have 7 children for 7 days
                            //for (int i = 0; i < 7; i++){

                            //}
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












        // Testing data input
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


        // Control structure for user to enable tracking


    }

}