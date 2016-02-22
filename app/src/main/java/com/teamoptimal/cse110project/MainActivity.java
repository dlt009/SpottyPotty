package com.teamoptimal.cse110project;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.AttrRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.teamoptimal.cse110project.data.DrawerItem;
import com.teamoptimal.cse110project.data.Restroom;
import com.teamoptimal.cse110project.data.User;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        LocationListener, OnMapReadyCallback {
    private static String TAG = "MainActivity";
    /* Amazon */
    public static AmazonClientManager clientManager;

    /* User */
    private User user;

    private List<Restroom> restrooms;
    private ArrayList<DrawerItem> items;
    private ListView listView;

    /* Map */
    private GoogleMap map;
    private final double milesToMeters = 1609.34;
    private double mile = 0.25; //can replace with mile from user input
    private double meters = mile * milesToMeters;

    /* Location */
    private LocationManager locationManager;
    /* Vars */
    private boolean initialized = false;

    private static final String PREFERENCES = "AppPrefs";
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    private boolean signedInGoogle;
    private boolean signedInFacebook;
    private boolean signedInTwitter;

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

        /* initialize shared preferences object to get info from other activities */
        sharedPreferences = getSharedPreferences(PREFERENCES, 0);
        editor = sharedPreferences.edit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        /* bathroom list view
        listView = (ListView) findViewById(R.id.restrooms_list);

        ArrayList<DrawerItem> items = new ArrayList<>();

        listView.setAdapter(new MyListAdapter(this, R.layout.mylist_layout, items));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, "Send user to Maps Activity",
                        Toast.LENGTH_SHORT).show();
            }
        });*/

        //initialize nav, nav header, and sign in button
        //navigationView = (NavigationView) findViewById(R.id.nav_view);
        //navigationView.setNavigationItemSelectedListener(this);
        //drawer.setScrimColor(getResources().getColor(android.R.color.transparent));

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



        signedInGoogle=sharedPreferences.getBoolean("goog",false);
        signedInFacebook=sharedPreferences.getBoolean("face",false);
        signedInTwitter=sharedPreferences.getBoolean("twit",false);

               /* bathroom list view */
        listView = (ListView) findViewById(R.id.restrooms_list);

        ArrayList<DrawerItem> items = new ArrayList<>();

        listView.setAdapter(new MyListAdapter(this, R.layout.mylist_layout, items));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, "Send user to Maps Activity",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generateListContent() {
        for (Restroom restroom : restrooms) {
            String title = restroom.getDescription();
            int feet = 0;
            String dist = feet + " ft ";
            double rate = restroom.getRating();
            items.add(new DrawerItem(title, dist, rate));
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        //updates button text to reflect if signing in or out
        toggleNavSignInText();
        String name = sharedPreferences.getString("user_name", "Please login to see profile");
        String email = sharedPreferences.getString("user_email", "");

        TextView name_text = (TextView) findViewById(R.id.header_name);
        TextView email_text = (TextView) findViewById(R.id.header_email);

        //if signed in, show profile info on headder, make create restroom button visible
        if (signedInFacebook || signedInGoogle || signedInTwitter) {
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

        // update login status
        signedInGoogle = sharedPreferences.getBoolean("goog", false);
        signedInFacebook = sharedPreferences.getBoolean("face", false);
        signedInTwitter = sharedPreferences.getBoolean("twit", false);

        // change sign-in button text to reflect if currently signing in or out
        if(signedInTwitter || signedInGoogle || signedInFacebook)
            signInButton.setText("Sign Out");
        else
            signInButton.setText("Sign In");
    }

    private void goToCreateRestroom() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        Intent intent = new Intent(this, CreateRestroomActivity.class);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(bestProvider);
        double[] loc = { location.getLatitude(), location.getLongitude() };
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

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
        map.setOnMyLocationChangeListener(myLocationChangeListener()); //Add marker for cur loc

        /* Start location tracking */
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);

        Location location = locationManager.getLastKnownLocation(bestProvider);
        //Log.d(TAG, location.getLatitude() + ", " + location.getLongitude());
        if (location != null) {
            onLocationChanged(location);
        }
        locationManager.requestLocationUpdates(bestProvider, 20000, 0, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged");
        showNearbyMarkers(location, meters);
    }

    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener() {
        return new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                Log.d(TAG, "OnMyLocationChange");
                LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());

                if(!initialized) {
                    map.moveCamera(CameraUpdateFactory.newLatLng(loc));
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 18));
                    initialized = true;
                }
            }
        };
    }

    private boolean isExecuting = false;
    private void showNearbyMarkers(Location location, double radius) {
        if(!isExecuting) {
            isExecuting = true;
            new GetRestroomsTask(location.getLatitude(), location.getLongitude(), radius).execute();
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
        double radius;

        public GetRestroomsTask(double latitude, double longitude, double radius) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.radius = radius;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d(TAG, "doInBackground");
            restrooms = Restroom.getRestrooms(longitude, latitude, radius);
            return null;
        }

        protected void onPostExecute(Void result) {
            Log.d(TAG, "Found " + restrooms.size() + " restrooms");
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
        }
    }

    private class MyListAdapter extends ArrayAdapter<DrawerItem> {
        private int layout;
        private List<DrawerItem> objects;

        private MyListAdapter(Context context, int resource, List<DrawerItem> objects) {
            super(context, resource, objects);
            this.objects = objects;
            layout = resource;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder mainViewHolder = null;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(layout, parent, false);

                ViewHolder viewHolder = new ViewHolder();
                viewHolder.imageColor = (ImageView)convertView.findViewById(R.id.view_color);
                viewHolder.title = (TextView)convertView.findViewById(R.id.view_title);
                viewHolder.dist = (TextView)convertView.findViewById(R.id.view_dist);
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
            else {
                mainViewHolder = (ViewHolder) convertView.getTag();
                DrawerItem dItem =  this.objects.get(position);


                mainViewHolder.title.setText(dItem.getItemTitle());
                mainViewHolder.dist.setText(dItem.getItemDist());
                mainViewHolder.ratings.setRating((float)dItem.getItemRate());

            }

            return convertView;
        }
    }

    public class ViewHolder {
        ImageView imageColor;
        TextView title;
        TextView dist;
        RatingBar ratings;
        Button details;
    }

}

