package sg.edu.np.ignight.Map;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.GeoApiContext;

import java.io.IOException;
import java.util.List;

import sg.edu.np.ignight.R;

public class ViewLocation extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {
    private MapView mapView;
    private LocationObject location;
    boolean gps_enabled = false;
    boolean network_enabled = false;
    private LocationManager locationManager;
    private Geocoder geocoder;
    private GeoApiContext geoApiContext = null;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastUserLoc;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_location);
        mapView = findViewById(R.id.mapView);
        location = (LocationObject) getIntent().getSerializableExtra("locationObject");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = auth.getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("user")
                .child(uid).child("Favourite Locations").child(location.getCategory()).child(location.getName());
        ImageView viewLocImg = findViewById(R.id.viewLocImg);
        ImageView viewLocBackBtn = findViewById(R.id.viewLocBackButton);
        ImageView favouriteBtn = findViewById(R.id.favouriteBtn);
        TextView locNameField = findViewById(R.id.viewLocName);
        TextView locDescField = findViewById(R.id.viewLocDesc);
        TextView addrField = findViewById(R.id.addrText);

        locNameField.setText(location.getName());
        locDescField.setText(location.getDesc());
        addrField.setText(location.getAddress());

        Glide.with(this)
                .load(location.getImgUri())
                .placeholder(R.drawable.ic_baseline_image_24)
                .into(viewLocImg);

        viewLocBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        initGoogleMap(savedInstanceState);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && (boolean) snapshot.getValue()){
                    favouriteBtn.setBackgroundResource(R.drawable.heart);
                }
                else{
                    favouriteBtn.setBackgroundResource(R.drawable.heartwithhole);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Updates child value of location in firebase to true/false when liked/unliked
        favouriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseReference
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && (boolean) snapshot.getValue()){
                            databaseReference.setValue(false);
                            favouriteBtn.setBackgroundResource(R.drawable.heartwithhole);
                        }
                        else{
                            databaseReference.setValue(true);
                            favouriteBtn.setBackgroundResource(R.drawable.heart);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

    }

    // Displays Google Map
    private void initGoogleMap(Bundle savedInstanceState) {
        // Checks if location is enabled
        if (!isGPSEnabled()) {
            turnOnGPS();
        }
        Bundle mapViewBundle = null;
        // Gets Google Map API key (free trial) to initialize
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
        geocoder = new Geocoder(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    // Function to display Google Maps on the Google Map Fragment
    @Override
    public void onMapReady(GoogleMap map) {
        String locName = location.getName();
        try {
            List<Address> addressList = geocoder.getFromLocationName(locName, 1);
            // Displays marker based on user's geolocation
            if (addressList.size() > 0) {
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                map.addMarker(new MarkerOptions().position(latLng).title(locName).snippet("Click here to learn more!"));
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        // Checks that the user's location permission is turned on
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);
        map.setOnInfoWindowClickListener(this);
        map.getUiSettings().setMapToolbarEnabled(false);
        map.getUiSettings().setRotateGesturesEnabled(true);
        map.getUiSettings().setZoomGesturesEnabled(true);
        getCurrentLocation();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    public boolean isGPSEnabled() {
        Boolean isEnabled = false;
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (gps_enabled && network_enabled) {
            return true;
        }
        return isEnabled;
    }

    // When the google map cursor is clicked, it will display an alertdialog
    @Override
    public void onInfoWindowClick(@NonNull Marker marker) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Find paths to this location?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //calculateDirections(marker, userLoc);
                        dialogInterface.dismiss();
                        displayTrack(location.getName());
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                }).create().show();
    }

    // Gets location after checking for location premission access
    private void getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                lastUserLoc = task.getResult();
            }
        });
    }

    // Goes to google maps and displays the available routes from user location to destination
    private void displayTrack(String dest){
        try {
            Uri uri = Uri.parse("https://www.google.co.in/maps/dir/" + lastUserLoc.getLatitude() + "," + lastUserLoc.getLongitude()
                    + "/" + dest);

            // initialize intent (google maps)
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage("com.google.android.apps.maps");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        catch (ActivityNotFoundException e){
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.maps");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage("com.google.android.apps.maps");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    // Requests for location to be turned on if off
    private void turnOnGPS(){
        new AlertDialog.Builder(ViewLocation.this)
                .setTitle("Enable Location")
                .setMessage("Location is needed to access this feature")
                .setPositiveButton("Settings", new
                        DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                finish();
                            }
                        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .show();
    }
}