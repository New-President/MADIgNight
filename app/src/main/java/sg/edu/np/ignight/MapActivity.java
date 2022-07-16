package sg.edu.np.ignight;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.material.tabs.TabLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

import sg.edu.np.ignight.Map.LocationObject;
import sg.edu.np.ignight.Map.MapViewPagerAdapter;

public class MapActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        //Button checkPerms = findViewById(R.id.checkPerms);
        TabLayout mapTabLayout = findViewById(R.id.tabLayout);
        ViewPager2 mapVP = findViewById(R.id.mapViewPager);
        ImageButton backBtn = findViewById(R.id.MapBackButton);
        // tabs (passes in csv data to viewpageradapter for each tab
        MapViewPagerAdapter vpAdapter = new MapViewPagerAdapter(this);
        // recycler view
        mapVP.setAdapter(vpAdapter);
        mapTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mapVP.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
        // Updates the tabLayout when the users scrolls horizontally to navigate to different tabs
        // instead of clicking the tabs
        mapVP.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                mapTabLayout.getTabAt(position).select();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

//    private ArrayList<LocationObject> readLocationsData(){
//
//        ArrayList<LocationObject> locationsList = new ArrayList<>();
//        InputStream is = getResources().openRawResource(R.raw.locations);
//        BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
//
//        String line;
//        while (true) {
//            try {
//                // skip over the header
//                reader.readLine();
//                if (((line = reader.readLine())!= null)) {
//                    Log.d("lineread", line);
//                    String[] tokens = line.split(",");
//                    Log.d("lineread", tokens[0]);
//                    Log.d("lineread", tokens[1]);
//                    Log.d("lineread", tokens[2]);
//                    Log.d("lineread", tokens[3]);
//                    Log.d("lineread", tokens[4]);
//                    LocationObject locationObject = new LocationObject(tokens[0], tokens[1], tokens[2], tokens[3], tokens[4]);
//
//                    locationsList.add(locationObject);
//                }
//                else {
//                    break;
//                }
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        return locationsList;
//    }

}