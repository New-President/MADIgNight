package sg.edu.np.ignight;

import androidx.appcompat.app.AppCompatActivity;

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

import java.util.ArrayList;

public class ActivityReport_Activity extends AppCompatActivity {
    // init fields
    private ArrayList<PieEntry> pieChartData;
    private ArrayList<BarEntry> barChartData;

    private PieChart pieChart;
    private BarChart barChart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activityreport_activity);

        pieChart = findViewById(R.id.PieChart);
        barChart = findViewById(R.id.BarChart);

        // Data entries for charts
        barChartData = new ArrayList<>();
        pieChartData = new ArrayList<>();

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
        barChart.animateY(5000);
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
        pieChart.animateXY(5000,5000);
        // hide description
        pieChart.getDescription().setEnabled(false);

        // Back button to go back to main menu
        ImageButton back_btn = findViewById(R.id.profileViewBackButton2);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent activity_report_to_main = new Intent(ActivityReport_Activity.this,MainMenuActivity.class);
                startActivity(activity_report_to_main);
                finish();
            }
        });
    }

}