package sg.edu.np.ignight;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.material.tabs.TabLayout;

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
        MapViewPagerAdapter vpAdapter = new MapViewPagerAdapter(this);
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



}