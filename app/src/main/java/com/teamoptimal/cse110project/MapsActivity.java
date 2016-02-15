package com.teamoptimal.cse110project;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Circle circle;
    private final double milesToMeters = 1609.34;
    private double mile = 0.25; //can replace with mile from user input
    private double meters = mile*milesToMeters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Show error dialog if GoolglePlayServices not available
        if (!isGooglePlayServicesAvailable()) {
            finish();
        }

        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //replaceMapFrag(); //replace google map fragment
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomGesturesEnabled((true));
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setBuildingsEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mMap.setMyLocationEnabled(true);

        /* will UPDATE location
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(bestProvider);
        if (location != null) {
            onLocationChanged(location);
        }
        locationManager.requestLocationUpdates(bestProvider, 20000, 0, (LocationListener) this);*/


        // **NOTE: This block of code needs to be modified to take in ALL stored markers
        // Still waiting on restroom adding to be completed**

        //int numOfMarkers = 2; //NEED TO BE CHANGED ONCE IMPLEMENTATION OF ADDING RESTROOMS TO DB
                              // IS COMPLETE

        // Add a marker in Ucsd and move the camera
        LatLng ucsd = new LatLng(32.88666, -117.24134);
        LatLng test = new LatLng(32.715738, -117.161084);

        Marker markerCVC = mMap.addMarker(new MarkerOptions().position(ucsd).title("Costa Verde Center"));
        Marker markerUCSD = mMap.addMarker(new MarkerOptions().position(ucsd).title("Test Marker in UCSD"));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(ucsd));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ucsd, 18));


        mMap.setOnMyLocationChangeListener(myLocationChangeListener()); //Add marker for cur loc

        /*circle = mMap.addCircle(new CircleOptions()
                .center(ucsd)
                .radius(meters) //half a mile in meters
                .strokeColor(Color.RED));
        */

        //showNearbyMarker(markerUCSD, circle); //will remove when testing done
        //showNearbyMarker(markerCVC, circle);

        // WHEN IMPLEMENTATION IS COMPLETE WILL FIX
        /*for (int i = 0; i < numOfMarkers; i++) {
            showNearbyMarks(marker, circle);
        }*/
    }

    // If marker is within given radius make visible
    private void showNearbyMarker(Marker marker, Circle circle) {
        float[] distance = new float[2];

        Location.distanceBetween( marker.getPosition().latitude, marker.getPosition().longitude,
                circle.getCenter().latitude, circle.getCenter().longitude, distance);

        if( distance[0] > circle.getRadius()  ){
            marker.setVisible(false);
        } else {
            marker.setVisible(true);
        }
    }
    /* for updating radius
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(latLng));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
    }*/

    private OnMyLocationChangeListener myLocationChangeListener() {
        return new OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());

                Marker mMarker = mMap.addMarker(new MarkerOptions().position(loc).title("Me"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 18));
                //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f));

                circle = mMap.addCircle(new CircleOptions()
                        .center(loc)
                        .radius(meters) //half a mile in meters
                        .strokeColor(Color.RED)
                        .visible(true)); //will change to false later

                //showNearbyMarker(mMarker, circle);
            }
        };
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
