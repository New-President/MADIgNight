package sg.edu.np.ignight.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
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
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.model.DirectionsResult;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import sg.edu.np.ignight.MapActivity;
import sg.edu.np.ignight.R;
import timber.log.Timber;

public class ViewLocation extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {
    private MapView mapView;
    private LocationObject location;
    boolean gps_enabled = false;
    boolean network_enabled = false;
    private LocationManager locationManager;
    private Geocoder geocoder;
    private GeoApiContext geoApiContext = null;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location userLoc;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_location);
        mapView = findViewById(R.id.mapView);
        location = (LocationObject) getIntent().getSerializableExtra("locationObject");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        ImageView viewLocImg = findViewById(R.id.viewLocImg);
        ImageView viewLocBackBtn = findViewById(R.id.viewLocBackButton);
        TextView locNameField = findViewById(R.id.viewLocName);
        TextView locDescField = findViewById(R.id.viewLocDesc);

        locNameField.setText(location.getName());
        locDescField.setText(location.getDesc());
        Glide.with(this)
                .load(location.getImgUri())
                .placeholder(R.drawable.ic_baseline_image_24)
                .into(viewLocImg);
        initGoogleMap(savedInstanceState);

        // Checks if location is enabled
        if (!isGPSEnabled()) {
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

        viewLocBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void initGoogleMap(Bundle savedInstanceState){

        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        geocoder = new Geocoder(this);

        if(geoApiContext == null){
            geoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.google_map_api_key)).build();
        }
        getUserLocation();
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

    @Override
    public void onMapReady(GoogleMap map) {
        String locName = location.getName();
        try {
            List<Address> addressList = geocoder.getFromLocationName(locName, 1);

            if (addressList.size() > 0 ){
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(),address.getLongitude());
                map.addMarker(new MarkerOptions().position(latLng).title(locName).snippet("Determine distance to " + locName + "?"));
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);
        map.setOnInfoWindowClickListener(this);

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

    @Override
    public void onInfoWindowClick(@NonNull Marker marker) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(marker.getSnippet())
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        calculateDirections(marker, userLoc);
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                }).create().show();

    }
    private void calculateDirections(Marker marker, Location userLoc){
        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude,
                marker.getPosition().longitude);
        DirectionsApiRequest directionsApiRequest = new DirectionsApiRequest(geoApiContext);

        directionsApiRequest.alternatives(true);
        directionsApiRequest.origin(
                new com.google.maps.model.LatLng(userLoc.getLatitude(), userLoc.getLongitude())
        );

        directionsApiRequest.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Log.d("DirectionsCal", "Calculate Directions: " + result.routes);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.d("DirectionsFail", "onFailure: Failed to get directions");
            }
        });
    }
    private void getUserLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()){
                    userLoc = task.getResult();
                    GeoPoint geoPoint = new GeoPoint(userLoc.getLatitude(), userLoc.getLongitude());
                    Timber.d("GeoPoint of user location: %s", geoPoint.toString());
                }
            }
        });
    }
}