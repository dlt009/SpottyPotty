package com.teamoptimal.cse110project;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
    public static ArrayList<Restroom> restrooms;
    public static ArrayList<Restroom> originalRestrooms;
    private ArrayList<RestroomItem> items;
    private ListView listView;
    private MyListAdapter adapter;

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
    private Menu optionsMenu;
    private MenuItem signOutOption;

    public static BroadcastReceiver receiver;

    public static String filter = "";
    public static double rated = 0.0;

    boolean signedInGoogle;
    boolean signedInFacebook;
    boolean signedInTwitter;

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

        /* Initialize Amazon Client Manager */
        clientManager = new AmazonClientManager(this);
        clientManager.validateCredentials();

        /* Initialize shared preferences object to get info from other activities */
        sharedPreferences = getSharedPreferences(PREFERENCES, 0);
        editor = sharedPreferences.edit();

        /* Load user */
        String userEmail = sharedPreferences.getString("user_email", "");
        user = new User();

        new GetUserTask(userEmail);

        /* Drawer */
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        /* Bathroom list view */
        listView = (ListView) findViewById(R.id.restrooms_list);

        items = new ArrayList<>();
        adapter = new MyListAdapter(this, R.layout.restroom_item, items);

        /* Initialize signIn button and link to SignInActivity */
        header = findViewById(R.id.header);
        signInButton = (Button) header.findViewById(R.id.nav_sign_in);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                startActivity(intent);
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LatLng latLng = new LatLng(restrooms.get(position).getLatitude(),
                        restrooms.get(position).getLongitude());
                drawer.closeDrawer(GravityCompat.START);
                map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,
                        map.getCameraPosition().zoom));
            }
        });
        receiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent intent){
                String action = intent.getAction();
                if(action.equals("filter_done")){

                    LatLng center = map.getCameraPosition().target;
                    Location centerLoc = new Location(LocationManager.GPS_PROVIDER);
                    centerLoc.setLatitude(center.latitude);
                    centerLoc.setLongitude(center.longitude);
                    //showNearbyMarkers(centerLoc, 0.00727946446, filter, rated);
                    generateListContent();
                }
            }
        };
        registerReceiver(receiver, new IntentFilter("filter_done"));

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "Starting app");

        // Update login status
        signedInGoogle = sharedPreferences.getBoolean("goog", false);
        signedInFacebook = sharedPreferences.getBoolean("face", false);
        signedInTwitter = sharedPreferences.getBoolean("twit", false);

        String name = sharedPreferences.getString("user_name", "Please login to see profile");
        String email = sharedPreferences.getString("user_email", "");

        TextView name_text = (TextView) findViewById(R.id.header_name);
        TextView email_text = (TextView) findViewById(R.id.header_email);

        // If signed in, show profile info on header, make create restroom button visible
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
        toggleNavSignInText();
    }

    private void toggleNavSignInText() {
        // Change sign-in button text to reflect if currently signing in or out
        if(signedInTwitter || signedInGoogle || signedInFacebook) {
            signInButton.setVisibility(View.GONE);
            if(signOutOption != null)
                signOutOption.setVisible(true);
            if(optionsMenu != null && !optionsMenu.hasVisibleItems())
                optionsMenu.add(0,R.id.sign_out,Menu.NONE,"Sign Out");
            if(optionsMenu != null) {
                optionsMenu.add(0, R.id.sign_out, Menu.NONE, "Sign Out");
            }
        }
        else {
            signInButton.setVisibility(View.VISIBLE);
            if(signOutOption != null)
                signOutOption.setVisible(false);
        }
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
        else if(user!= null && user.getReportCount() > 3){
            Toast.makeText(getBaseContext(),
                    "You do not have access to this feature\n" +
                            "Reason: too many reports against content created by this user",
                    Toast.LENGTH_LONG).show();
        }
        else {
            // Show create restroom activity with current location
            Intent intent = new Intent(this, CreateRestroomActivity.class);
            double[] loc = {currentLocation.latitude, currentLocation.longitude};
            intent.putExtra("Location", loc);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            unregisterReceiver(receiver);
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        Log.d(TAG, "Menu inflated!");
        optionsMenu = menu;
        optionsMenu.clear();

        /* Filter */
        optionsMenu.add(0, R.id.filter, Menu.NONE, "Filter");

        /* Sign out */
        optionsMenu.add(0, R.id.sign_out, Menu.NONE, "Sign Out");
        signOutOption = optionsMenu.findItem(R.id.sign_out);
        signOutOption.setVisible(false);
        toggleNavSignInText();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.sign_out) {
            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
            startActivity(intent);
            return true;
        }

        else if (id == R.id.filter) {
            //registerReceiver(receiver, new IntentFilter("filter_done"));
            Intent intent = new Intent(getApplicationContext(), FilterActivity.class);
            startActivity(intent);
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
                    LatLng center = map.getCameraPosition().target;
                    Location centerLoc = new Location(LocationManager.GPS_PROVIDER);
                    centerLoc.setLatitude(center.latitude);
                    centerLoc.setLongitude(center.longitude);
                    showNearbyMarkers(centerLoc, 0.00727946446, filter, rated);
                    initialized = true;
                }

                float[] result = new float[2];
                Location.distanceBetween(lastKnownLocation.latitude, lastKnownLocation.longitude,
                        currentLocation.latitude, currentLocation.longitude, result);

                // 0.00727946446 latitude = 0.5 miles
                // 0.25 miles = 402.336 meters
                if(result[0] > 402.336) {
                    map.clear();
                    LatLng center = map.getCameraPosition().target;
                    Location centerLoc = new Location(LocationManager.GPS_PROVIDER);
                    centerLoc.setLatitude(center.latitude);
                    centerLoc.setLongitude(center.longitude);
                    showNearbyMarkers(centerLoc, 0.00727946446, filter, rated);
                    lastKnownLocation = currentLocation;
                }
                else {
                    if(restrooms != null && !restrooms.isEmpty())
                        generateListContent();
                }
            }
        };
    }

    public void generateListContent() {
        items.clear();
        for (Restroom restroom : restrooms) {

            float[] result = new float[2];
            Location.distanceBetween(restroom.getLatitude(), restroom.getLongitude(),
                    currentLocation.latitude, currentLocation.longitude, result);
            double meters = result[0];
            String distance = String.format("%.2f", meters) + " meters ";

            items.add(new RestroomItem(restroom.getID(), restroom.getDescription(), distance,
                    restroom.getRating(), restroom.getColor()));
        }
        adapter.notifyDataSetChanged();
    }

    private void showNearbyMarkers(Location location, double diameter, String filter, double rated) {
        if(!isExecuting) {
            isExecuting = true;
            new GetRestroomsTask(location.getLatitude(), location.getLongitude(), diameter, filter, rated).execute();
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

    private class GetUserTask extends AsyncTask<Void, Void, Void> {
        String email;

        public GetUserTask(String email) {
            this.email = email;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d(TAG, "doInBackground");
            user = User.load(email);
            return null;
        }

        protected void onPostExecute(Void result) {
            /* Do UI actions after getting User */

        }
    }

    private class GetRestroomsTask extends AsyncTask<Void, Void, Void> {
        double latitude;
        double longitude;
        double diameter;
        String filter;
        double rated;

        public GetRestroomsTask(double latitude, double longitude, double diameter, String filter, double rated) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.diameter = diameter;
            this.filter = filter;
            this.rated = rated;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d(TAG, "doInBackground");
            restrooms = Restroom.getRestrooms(latitude, longitude, diameter, filter, rated);
            originalRestrooms = Restroom.getRestrooms(latitude, longitude, diameter, "", 0.0);
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
            final ViewHolder mainViewHolder;

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(layout, parent, false);

                ViewHolder viewHolder = new ViewHolder();
                viewHolder.imageColor = (ImageView)convertView.findViewById(R.id.view_color);
                viewHolder.title = (TextView)convertView.findViewById(R.id.view_title);
                viewHolder.distance = (TextView)convertView.findViewById(R.id.view_dist);
                viewHolder.ratings = (RatingBar)convertView.findViewById(R.id.view_rating);
                viewHolder.details = (Button)convertView.findViewById(R.id.details_button);
                /*viewHolder.details.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(getContext(),
                        //        "Send user to Reviews Activity", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                        String [] name_dist = { mainViewHolder.title.getText(), dItem.getDistance()};
                        intent.putExtra("Title and Distance", name_dist);
                        startActivity(intent);
                    }
                });*/

                convertView.setTag(viewHolder);
            }

            mainViewHolder = (ViewHolder) convertView.getTag();

            final RestroomItem dItem =  this.restrooms.get(position);

            // Get color
            int color = Color.HSVToColor(new float[] { dItem.getColor(), 1.0f, 1.0f });

            // Set values
            mainViewHolder.title.setText(dItem.getTitle());
            mainViewHolder.distance.setText("" + dItem.getDistance());
            mainViewHolder.ratings.setRating((float) dItem.getRating());
            mainViewHolder.imageColor.setBackgroundColor(color);

            mainViewHolder.details.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        /*Toast.makeText(getContext(),
                                "Send user to Reviews Activity", Toast.LENGTH_SHORT).show();*/

                    Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                    String name = mainViewHolder.title.getText().toString();
                    String distance = mainViewHolder.distance.getText().toString();
                    Float rating = mainViewHolder.ratings.getRating();
                    intent.putExtra("name", name);
                    intent.putExtra("distance", distance);
                    intent.putExtra("ratings", rating);
                    intent.putExtra("restroomID", dItem.getRestroomID());
                    startActivity(intent);
                }
            });

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

