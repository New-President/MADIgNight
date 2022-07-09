package sg.edu.np.ignight;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;

import sg.edu.np.ignight.Map.mapViewPagerAdapter;

public class MapActivity extends AppCompatActivity {

    boolean gps_enabled = false;
    boolean network_enabled = false;
    private LocationManager locationManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        //Button checkPerms = findViewById(R.id.checkPerms);
        TabLayout mapTabLayout = findViewById(R.id.tabLayout);
        ViewPager2 mapVP = findViewById(R.id.mapViewPager);
        mapViewPagerAdapter vpAdapter = new mapViewPagerAdapter(this);
        mapVP.setAdapter(vpAdapter);
//        checkPerms.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (!isGPSEnabled()) {
//                    new AlertDialog.Builder(MapActivity.this)
//                            .setTitle("Enable Location")
//                            .setMessage("Location is needed to access this feature")
//                            .setPositiveButton("Settings", new
//                                    DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
//                                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
//
//                                        }
//                                    })
//                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//                                    finish();
//                                }
//                            })
//                            .show();
//                }
//            }
//        });
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
    }

    public boolean isGPSEnabled(){
        Boolean isEnabled = false;
        locationManager = (LocationManager)this.getSystemService(LOCATION_SERVICE);
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if (gps_enabled && network_enabled){
            return true;
        }
        return isEnabled;
    }


}