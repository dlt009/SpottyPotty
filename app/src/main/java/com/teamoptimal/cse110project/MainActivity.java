package com.teamoptimal.cse110project;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.teamoptimal.cse110project.data.RestroomItem;
import com.teamoptimal.cse110project.data.Restroom;
import com.teamoptimal.cse110project.data.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback {
    private static String TAG = "MainActivity";

    /* Amazon */
    public static AmazonClientManager clientManager;

    /* User */
    public static User user;

    /* Drawer */
    private ArrayList<Restroom> restrooms;
    private ArrayList<RestroomItem> items;
    private ListView listView;
    private MyListAdapter adapter;
    public static String clickedRestroomID;

    /* Map */
    private GoogleMap map;
    private LatLng currentLocation;
    private LatLng lastKnownLocation;

    /* Location */
    private LocationManager locationManager;

    /* Vars */
    private boolean initialized = false;
    private boolean isExecuting = false; // Is executing the getting of restrooms

    private static final String PREFERENCES = "AppPrefs";
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    /* UI Elements */
    private View header;
    private Button signInButton;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Show error if Google Play Services are not available
        if (!isGooglePlayServicesAvailable()) {
            finish();
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToCreateRestroom();
            }
        });

        /* Initialize shared preferences object to get info from other activities */
        sharedPreferences = getSharedPreferences(PREFERENCES, 0);
        editor = sharedPreferences.edit();

        /* Drawer */
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        /* Bathroom list view */
        listView = (ListView) findViewById(R.id.restrooms_list);

        items = new ArrayList<>();
        adapter = new MyListAdapter(this, R.layout.mylist_layout, items);

        /* Initialize signIn button and link to SignInActivity */
        header = findViewById(R.id.header);
        signInButton = (Button) header.findViewById(R.id.nav_sign_in_toggle);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                startActivity(intent);
            }
        });
        toggleNavSignInText();

        /* Initialize Amazon Client Manager */
        clientManager = new AmazonClientManager(this);
        clientManager.validateCredentials();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, "Send user to Maps Activity",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        // Updates button text to reflect if signing in or out
        toggleNavSignInText();
        String name = sharedPreferences.getString("user_name", "Please login to see profile");
        String email = sharedPreferences.getString("user_email", "");

        TextView name_text = (TextView) findViewById(R.id.header_name);
        TextView email_text = (TextView) findViewById(R.id.header_email);

        // If signed in, show profile info on header, make create restroom button visible
        if (signInButton.getVisibility() == View.GONE) {
            name_text.setText(name);
            email_text.setText(email);
            fab.setVisibility(View.VISIBLE);
        }
        else {
            name_text.setText("Please login to see profile");
            email_text.setText("");
            fab.setVisibility(View.GONE);
        }
    }

    private void toggleNavSignInText () {
        // Update login status
        boolean signedInGoogle = sharedPreferences.getBoolean("goog", false);
        boolean signedInFacebook = sharedPreferences.getBoolean("face", false);
        boolean signedInTwitter = sharedPreferences.getBoolean("twit", false);

        // Change sign-in button text to reflect if currently signing in or out
        if(signedInTwitter || signedInGoogle || signedInFacebook)
            signInButton.setVisibility(View.GONE);
        else
            signInButton.setText("Sign In");
    }

    private void goToCreateRestroom() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            // Ask for permission
            return;
        }

        // Show create restroom activity with current location
        Intent intent = new Intent(this, CreateRestroomActivity.class);
        double[] loc = { currentLocation.latitude, currentLocation.longitude };
        intent.putExtra("Location", loc);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        // Check for permission
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

        map = googleMap;
        map.getUiSettings().setZoomGesturesEnabled((true));
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setBuildingsEnabled(true);
        map.setMyLocationEnabled(true);
        map.setOnMyLocationChangeListener(myLocationChangeListener());
    }

    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener() {
        return new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                Log.d(TAG, "OnMyLocationChange");
                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                if(!initialized) {
                    map.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18));
                    lastKnownLocation = currentLocation;
                    Log.d(TAG, location.getLatitude() + ", " + location.getLongitude());
                    showNearbyMarkers(location, 0.00727946446);
                    initialized = true;
                }

                float[] result = new float[2];
                Location.distanceBetween(lastKnownLocation.latitude, lastKnownLocation.longitude,
                        currentLocation.latitude, currentLocation.longitude, result);

                // 0.00727946446 latitude = 0.5 miles
                // 0.25 miles = 402.336 meters
                if(result[0] > 402.336) {
                    map.clear();
                    showNearbyMarkers(location, 0.00727946446);
                    lastKnownLocation = currentLocation;
                } else {
                    if(restrooms != null && !restrooms.isEmpty())
                        generateListContent();
                }
            }
        };
    }

    public void generateListContent() {
        items.clear();
        for (Restroom restroom : restrooms) {
            String title = restroom.getDescription();

            float[] result = new float[2];
            Location.distanceBetween(restroom.getLatitude(), restroom.getLongitude(),
                    currentLocation.latitude, currentLocation.longitude, result);

            double meters = result[0];
            String distance = String.format("%.2f", meters) + " meters ";
            double rating = restroom.getRating();
            items.add(new RestroomItem(title, distance, rating, restroom.getColor()));
        }
        adapter.notifyDataSetChanged();
    }

    private void showNearbyMarkers(Location location, double diameter) {
        if(!isExecuting) {
            isExecuting = true;
            new GetRestroomsTask(location.getLatitude(), location.getLongitude(), diameter).execute();
        }
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

    private class GetRestroomsTask extends AsyncTask<Void, Void, Void> {
        double latitude;
        double longitude;
        double diameter;

        public GetRestroomsTask(double latitude, double longitude, double diameter) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.diameter = diameter;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d(TAG, "doInBackground");
            restrooms = Restroom.getRestrooms(latitude, longitude, diameter);
            return null;
        }

        protected void onPostExecute(Void result) {
            Log.d(TAG, "Found " + restrooms.size() + " restrooms");
            // Sort location
            Collections.sort(restrooms, new RestroomComparator(latitude, longitude));

            // Add markers
            for(Restroom restroom : restrooms) {
                LatLng latLng = new LatLng(restroom.getLatitude(), restroom.getLongitude());
                float markerColor = restroom.getColor();

                map.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(restroom.getDescription())
                                .icon(BitmapDescriptorFactory.defaultMarker(markerColor)
                ));
            }
            isExecuting = false;

            // Generate drawer list of restrooms
            generateListContent();
        }
    }

    private class MyListAdapter extends ArrayAdapter<RestroomItem> {
        private int layout;
        private List<RestroomItem> restrooms;

        public MyListAdapter(Context context, int resource, List<RestroomItem> restrooms) {
            super(context, resource, restrooms);
            this.restrooms = restrooms;
            layout = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder mainViewHolder;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(layout, parent, false);

                ViewHolder viewHolder = new ViewHolder();
                viewHolder.imageColor = (ImageView)convertView.findViewById(R.id.view_color);
                viewHolder.title = (TextView)convertView.findViewById(R.id.view_title);
                viewHolder.distance = (TextView)convertView.findViewById(R.id.view_dist);
                viewHolder.ratings = (RatingBar)convertView.findViewById(R.id.view_rating);
                viewHolder.details = (Button)convertView.findViewById(R.id.details_button);
                viewHolder.details.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getContext(),
                                "Send user to Reviews Activity", Toast.LENGTH_SHORT).show();
                    }
                });

                convertView.setTag(viewHolder);
            }

            mainViewHolder = (ViewHolder) convertView.getTag();

            RestroomItem dItem =  this.restrooms.get(position);

            // Get color
            int color = Color.HSVToColor(new float[] { dItem.getColor(), 1.0f, 1.0f });

            // Set values
            mainViewHolder.title.setText(dItem.getTitle());
            mainViewHolder.distance.setText("" + dItem.getDistance());
            mainViewHolder.ratings.setRating((float)dItem.getRating());
            mainViewHolder.imageColor.setBackgroundColor(color);

            return convertView;
        }
    }

    private class ViewHolder {
        ImageView imageColor;
        TextView title;
        TextView distance;
        RatingBar ratings;
        Button details;
    }

    private class RestroomComparator implements Comparator<Restroom> {
        private double latitude;
        private double longitude;

        public RestroomComparator(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        public int compare(Restroom a, Restroom b) {
            float[] resultA = new float[2];
            float[] resultB = new float[2];
            Location.distanceBetween(a.getLatitude(), a.getLongitude(),
                    latitude, longitude, resultA);
            Location.distanceBetween(b.getLatitude(), b.getLongitude(),
                    latitude, longitude, resultB);

            return resultA[0] < resultB[0] ? -1 : resultA[0] == resultB[0] ? 0 : 1;
        }
    }
}

