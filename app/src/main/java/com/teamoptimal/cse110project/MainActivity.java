package com.teamoptimal.cse110project;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Gravity;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.RequestResult;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
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
    public static Restroom lastCreatedRestroom;
    private ArrayList<Restroom> restrooms;
    private ArrayList<Restroom> originalRestrooms;
    private ArrayList<RestroomItem> items;
    private ListView listView;
    private MyListAdapter adapter;

    public static String lastRatedID;
    public static double lastRated;
    /* Map */
    private GoogleMap map;
    private Location currentLocation;
    private Location lastNavigatedLocation;
    private Location lastKnownLocation;
    private FloatingActionButton recenter;
    private boolean centeredSearch = true;

    private FusedLocationService fusedLocationService;
    private boolean directionsMode = false;
    private PolylineOptions polylineOptions;
    private Marker destinationMarker;
    private Polyline prevMarker; // previous direction polyline

    /* Navigation layout */
    RelativeLayout navigationLayout;

    /* Snackbar */
    private CoordinatorLayout coordinatorLayout;
    private Snackbar snackbar;
    private float currentZoom;
    private View.OnClickListener onRefreshRestroomsClick;
    private int shownReason; // -1 == moved out of location, 1 == zoomed in or out

    /* Vars */
    private boolean initialized = false;
    private boolean isExecuting = false; // Is executing the getting of restrooms

    protected static final String PREFERENCES = "AppPrefs";
    private static SharedPreferences sharedPreferences;

    protected static int reportCount;

    public static String filter = "";
    public static double rated = 0.0;

    private BroadcastReceiver receiver;


    /* UI Elements */
    private View header;
    private Button signInButton;
    private FloatingActionButton fab;
    private Menu optionsMenu;
    private MenuItem signOutOption;

    static boolean signedInGoogle;
    static boolean signedInFacebook;
    static boolean signedInTwitter;

    private static int broadcastType = -1; // 0 = filter, 1 = restroom, 2 = review

    public boolean isDoneLoadingList;
    public static boolean isDoneCreatingRestroom;

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

        /* Initialize Amazon Client Manager */
        clientManager = new AmazonClientManager(this);
        clientManager.validateCredentials();

        /* Initialize shared preferences object to get info from other activities */
        sharedPreferences = getSharedPreferences(PREFERENCES, 0);

        /* Load user */
        String userEmail = sharedPreferences.getString("user_email", "");

        user = new User();

        new GetUserTask(userEmail).execute();

        /* Location */
        fusedLocationService = new FusedLocationService(this, new FusedLocationReceiver() {
            @Override
            public void onLocationChanged() {
                Log.i(TAG, "Location changed");
                currentLocation = fusedLocationService.getLocation();
                if (!initialized) {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(),
                            currentLocation.getLongitude()), 18));
                    lastKnownLocation = currentLocation;
                    lastNavigatedLocation = currentLocation;
                    currentZoom = 18;
                    Log.d(TAG, fusedLocationService.getLocation().getLatitude() + ", " +
                            fusedLocationService.getLocation().getLongitude());
                    showNearbyMarkers(currentLocation, 0.00727946446);
                }

                if(directionsMode) {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(),
                            currentLocation.getLongitude()), map.getCameraPosition().zoom));
                }

                float[] result = new float[2];
                Location.distanceBetween(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(),
                        currentLocation.getLatitude(), currentLocation.getLongitude(), result);

                // 0.00727946446 latitude = 0.5 miles
                // 0.25 miles = 402.336 meters
                if (result[0] > 402.336) {
                    map.clear();
                    showNearbyMarkers(currentLocation, 0.00727946446);
                    lastKnownLocation = currentLocation;
                } else {
                    if (restrooms != null && !restrooms.isEmpty())
                        generateListContent();
                }


            }
        });

        /* Set up fab icons */
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(onAddRestroomClick());
        fab.setVisibility(View.GONE);
        recenter = (FloatingActionButton) findViewById(R.id.center);
        recenter.setOnClickListener(onRecenterClick());
        recenter.setVisibility(View.GONE);

        /* Drawer */
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        /* Navigation layout */
        navigationLayout = (RelativeLayout) findViewById(R.id.navigationLayout);
        navigationLayout.setVisibility(View.GONE);
        // Review restroom
        findViewById(R.id.addReview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Find restroom using destination marker's title
                Restroom restroom = new Restroom();
                Log.d(TAG, destinationMarker.getTitle());
                for (Restroom r: restrooms) {
                    Log.d(TAG, r.getDescription() + " =? " + destinationMarker.getTitle());
                    if(r.getDescription().equals(destinationMarker.getTitle())) {
                        restroom = r;
                        break;
                    }
                }

                broadcastType = 2;
                IntentFilter filter = new IntentFilter();
                filter.addAction("review_created");
                registerReceiver(receiver, filter);

                Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                String name = restroom.getDescription();
                intent.putExtra("name", name);
                intent.putExtra("distance", "");
                intent.putExtra("restroomID", restroom.getID());
                intent.putExtra("restroomTags", restroom.getFormattedTags());
                intent.putExtra("ratings", restroom.getRating());
                startActivity(intent);
            }
        });

        // Cancel navigation
        findViewById(R.id.cancelNavigation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDirectionsMode(false);
            }
        });

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
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,
                        map.getCameraPosition().zoom));
            }
        });

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent intent) {
                String action = intent.getAction();
                Log.d(TAG, "Received broadcast " + action);
                if (action.equals("filter_done")) {
                    Log.d(TAG, "Filter done");
                    Log.d(TAG, "" + filter);
                    Log.d(TAG, "" + rated);
                    Log.d(TAG, "" + restrooms.size());
                    restrooms = Restroom.filterRestrooms(originalRestrooms, filter, rated);
                    Log.d(TAG, "" + restrooms.size());

                    String rate = "";
                    if(rated != 0.0) {rate = " "+Double.toString(rated)+"+";}

                    String tags = Restroom.getFormattedTags(filter);
                    if (tags.equals("No tags")) tags = "ALL";
                    String tagText = "Showing " + tags + rate + " restrooms";
                    View filtersView = findViewById(R.id.filters);
                    ((TextView) filtersView.findViewById(R.id.filter_text)).setText(tagText);

                    generateListContent();
                    map.clear();
                    drawMap();


                } else if(action.equals("restroom_created")) {
                    restrooms.add(lastCreatedRestroom);

                    generateListContent();
                    map.clear();
                    drawMap();
                } else if(action.equals("review_created")) {
                    Log.d(TAG, "aaaaaaa " + lastRatedID);
                    for(Restroom r : restrooms) {
                        if(r.getID().equals(lastRatedID)) {
                            Log.d(TAG, "found");
                            r.setRating(lastRated);
                            break;
                        }
                    }
                    generateListContent();

                }
            }
        };

        IntentFilter filter = new IntentFilter();
        if(broadcastType == 0)
            filter.addAction("filter_done");
        else if(broadcastType == 1)
            filter.addAction("restroom_created");
        else if(broadcastType == 2)
            filter.addAction("review_created");
        registerReceiver(receiver, filter);

        /* Filter button */
        View filtersView = findViewById(R.id.filters);
        filtersView.findViewById(R.id.filter_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                broadcastType = 0;
                IntentFilter filter = new IntentFilter();
                filter.addAction("filter_done");
                registerReceiver(receiver, filter);

                Intent intent = new Intent(getApplicationContext(), FilterActivity.class);
                startActivity(intent);
            }
        });

        // Set snackbar stuff
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        setUpSnackBar();

        Log.d(TAG, " Activity Created");
    }

    private View.OnClickListener onAddRestroomClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToCreateRestroom();
            }
        };
    }

    private View.OnClickListener onRecenterClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng latLng = new LatLng(currentLocation.getLatitude(),
                        currentLocation.getLongitude());
                currentZoom = 18;
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
                if (!directionsMode && !centeredSearch) {
                    map.clear();
                    Location centerLoc = new Location(LocationManager.GPS_PROVIDER);
                    centerLoc.setLatitude(latLng.latitude);
                    centerLoc.setLongitude(latLng.longitude);
                    lastNavigatedLocation = currentLocation;
                    centeredSearch = true;
                    showNearbyMarkers(centerLoc, 0.00727946446);
                }
            }
        };
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
        } else {
            name_text.setText("Please login to see profile");
            email_text.setText("");
        }
        toggleNavSignInText();

        clientManager = new AmazonClientManager(this);
        clientManager.validateCredentials();

        IntentFilter filter = new IntentFilter();
        if(broadcastType == 0) filter.addAction("filter_done");
        if(broadcastType == 1) filter.addAction("restroom_created");
        if(broadcastType == 2) filter.addAction("review_created");
        registerReceiver(receiver, filter);

        String userEmail = sharedPreferences.getString("user_email", "");
        new GetUserTask(userEmail).execute();
    }

    private void toggleNavSignInText() {
        // Change sign-in button text to reflect if currently signing in or out
        if (signedInTwitter || signedInGoogle || signedInFacebook) {
            signInButton.setVisibility(View.GONE);
            if (initialized){
                setFABUI(true);
                isDoneLoadingList = true;
            }
            if (signOutOption != null)
                signOutOption.setVisible(true);
        } else {
            signInButton.setVisibility(View.VISIBLE);
            if (initialized) setFABUI(false);
            if (signOutOption != null)
                signOutOption.setVisible(false);
        }
    }

    private void setFABUI(boolean showAddRestroom) {
        if (showAddRestroom) {
            fab.setImageResource(android.R.drawable.ic_input_add);
            fab.setBackgroundTintList(new ColorStateList(new int[][]{new int[]{0}},
                    new int[]{getResources().getColor(R.color.colorPrimary)}));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fab.setImageTintList(new ColorStateList(new int[][]{new int[]{0}},
                        new int[] { getResources().getColor(R.color.white) } ));
            }
            fab.setOnClickListener(onAddRestroomClick());

            recenter.setVisibility(View.VISIBLE);
        }
        else {
            fab.setImageResource(R.mipmap.ic_pin);
            fab.setBackgroundTintList(new ColorStateList(new int[][]{ new int[]{0} },
                    new int[]{getResources().getColor(R.color.white)}));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fab.setImageTintList(new ColorStateList(new int[][] { new int[]{0} },
                        new int[] { getResources().getColor(R.color.colorPrimary) } ));
            }
            fab.setOnClickListener(onRecenterClick());

            recenter.setVisibility(View.GONE);
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
        } else if(user == null) {
            Toast.makeText(getBaseContext(),
                    "You do not have access to this feature\n" +
                            "Reason: You must be signed in to create a restroom",
                    Toast.LENGTH_LONG).show();
        } else if (reportCount > 4) {
            Toast.makeText(getBaseContext(),
                    "You do not have access to this feature\n" +
                            "Reason: too many reports against content created by this user",
                    Toast.LENGTH_LONG).show();
        } else {
            // Show create restroom activity with current location
            // Show create restroom activity with current location
            broadcastType = 1;
            IntentFilter filter = new IntentFilter();
            filter.addAction("restroom_created");
            registerReceiver(receiver, filter);

            Intent intent = new Intent(this, CreateRestroomActivity.class);
            double[] loc = { currentLocation.getLatitude(), currentLocation.getLongitude() };
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

        toggleNavSignInText();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.sign_out) {
            ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);
            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.filter) {
            ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);
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
            return;
        }

        onRefreshRestroomsClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.clear();
                // Get restrooms nearby the new point
                LatLng center = map.getCameraPosition().target;
                Location centerLoc = new Location(LocationManager.GPS_PROVIDER);
                centerLoc.setLatitude(center.latitude);
                centerLoc.setLongitude(center.longitude);
                showNearbyMarkers(centerLoc, 0.00727946446);
                currentZoom = map.getCameraPosition().zoom;
                lastNavigatedLocation = centerLoc;
                snackbar.dismiss();
                shownReason = 0;
                centeredSearch = false;
                setUpSnackBar();
            }
        };

        setUpSnackBar();
        map = googleMap;
        map.getUiSettings().setZoomGesturesEnabled(true);
        map.getUiSettings().setMapToolbarEnabled(false);
        map.setMyLocationEnabled(true);
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setRotateGesturesEnabled(false);
        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                // If not initialized and not zoomed in to the default zoom in value, return,
                // otherwise, set initialized to true
                if (!initialized && cameraPosition.zoom != 18)
                    return;
                else {
                    initialized = true;
                    fab.setVisibility(View.VISIBLE);
                    toggleNavSignInText();
                }

                if(directionsMode)
                    return;

                // Get difference in distance from target screen to currentLocation
                float[] result = new float[2];
                Location.distanceBetween(lastNavigatedLocation.getLatitude(), lastNavigatedLocation.getLongitude(),
                        map.getCameraPosition().target.latitude, map.getCameraPosition().target.longitude,
                        result);

                // If zoom level is different or moved out of lastKnownLocation,
                // show restroom search ui
                // shownReason: -1 = moved out of location, 1 = zoomed in or out
                if (currentZoom != cameraPosition.zoom || result[0] > 402.336) {
                    if (!snackbar.isShown())
                        snackbar.show();

                    if (currentZoom != cameraPosition.zoom) {
                        shownReason = -1;
                    } else if (result[0] > 402.336) {
                        shownReason = 1;
                    }
                }

                if ((shownReason == -1 && currentZoom == cameraPosition.zoom) ||
                        (shownReason == 1 && result[0] <= 402.336)) {
                    snackbar.dismiss();
                    setUpSnackBar();
                    shownReason = 0;
                }
            }
        });
        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                Context context = getApplicationContext(); //or getActivity(), YourActivity.this, etc.

                LinearLayout info = new LinearLayout(context);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(context);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(context);
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                TextView directions = new TextView(context);
                directions.setTextColor(Color.BLACK);
                directions.setGravity(Gravity.CENTER);
                directions.setTypeface(null, Typeface.BOLD);
                directions.setText("\nGet directions");

                info.addView(title);
                info.addView(snippet);
                info.addView(directions);

                return info;
            }
        });
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(final Marker marker) {
                Log.d(TAG, "Getting direction");
                String serverKey = "";
                LatLng origin = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                LatLng destination = marker.getPosition();
                GoogleDirection.withServerKey(serverKey)
                        .from(origin)
                        .to(destination)
                        .transportMode(TransportMode.WALKING)
                        .execute(new DirectionCallback() {
                            @Override
                            public void onDirectionSuccess(Direction direction, String rawBody) {
                                String status = direction.getStatus();
                                if (status.equals(RequestResult.OK)) {
                                    Log.d(TAG, "Direction success");
                                    if (prevMarker != null)
                                        prevMarker.remove();
                                    Route route = direction.getRouteList().get(0);
                                    Leg leg = route.getLegList().get(0);
                                    ArrayList<LatLng> directionPositionList = leg.getDirectionPoint();
                                    polylineOptions =
                                            DirectionConverter.createPolyline(getApplicationContext(),
                                                    directionPositionList, 5, Color.RED);
                                    prevMarker = map.addPolyline(polylineOptions);
                                    destinationMarker = marker;
                                    setDirectionsMode(true);
                                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                            new LatLng(currentLocation.getLatitude(),
                                            currentLocation.getLongitude()), 18));

                                    if(snackbar.isShown()) snackbar.dismiss();
                                } else if (status.equals(RequestResult.NOT_FOUND)) {
                                    Log.d(TAG, "Direction not found");
                                }
                            }

                            @Override
                            public void onDirectionFailure(Throwable t) {
                                Log.d(TAG, "Failure");
                            }
                        });

                marker.hideInfoWindow();
            }
        });

    }

    private void setDirectionsMode(boolean isOn) {
        if(isOn) {
            navigationLayout.setVisibility(View.VISIBLE);
            map.getUiSettings().setAllGesturesEnabled(false);
            map.getUiSettings().setZoomGesturesEnabled(true);
            directionsMode = true;
        } else {
            navigationLayout.setVisibility(View.GONE);
            map.getUiSettings().setAllGesturesEnabled(true);
            map.getUiSettings().setRotateGesturesEnabled(false);
            prevMarker.remove();
            directionsMode = false;
            polylineOptions = null;
            destinationMarker = null;
        }
    }

    private void setUpSnackBar() {
        snackbar = Snackbar
                .make(coordinatorLayout, "Search restrooms near this point", Snackbar.LENGTH_INDEFINITE)
                .setAction("Search", onRefreshRestroomsClick);
        snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
    }

    public void generateListContent() {
        items.clear();
        int colorIndex = 0;
        for (Restroom restroom : restrooms) {
            float[] result = new float[2];
            Location.distanceBetween(restroom.getLatitude(), restroom.getLongitude(),
                    currentLocation.getLatitude(), currentLocation.getLongitude(), result);
            double meters = result[0];
            String distance = String.format("%.2f", meters) + " meters ";

            // Get tags
            String tags = restroom.getFormattedTags(true);

            items.add(new RestroomItem(restroom.getID(), restroom.getDescription(), distance,
                    tags, restroom.getRating(), restroom.getColor()));
        }
        // Sort location
        Collections.sort(restrooms, new RestroomComparator(currentLocation.getLatitude(),
                currentLocation.getLongitude()));
        adapter.notifyDataSetChanged();
    }

    private void showNearbyMarkers(Location location, double diameter) {
        if (!isExecuting) {
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
            reportCount = user.getReportCount();
            Log.d(TAG, "User "+user.getEmail()+" has been loaded with "+reportCount+" reports");
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
            originalRestrooms = Restroom.getRestrooms(latitude, longitude, diameter);
            return null;
        }

        protected void onPostExecute(Void result) {
            Log.d(TAG, "Found " + restrooms.size() + " restrooms");
            // Sort location
            Collections.sort(restrooms, new RestroomComparator(latitude, longitude));

            // Add markers
            drawMap();

            isExecuting = false;

            // Generate drawer list of restrooms
            generateListContent();
        }
    }

    private void drawMap() {
        if(directionsMode) {
            map.addPolyline(polylineOptions);
            map.addMarker(new MarkerOptions()
                .position(destinationMarker.getPosition())
                .title(destinationMarker.getTitle())
                .snippet(destinationMarker.getSnippet())
                .icon(BitmapDescriptorFactory.defaultMarker(destinationMarker.getAlpha())));
        }
        createMarkers();
    }

    private void createMarkers(){
        for(Restroom restroom : restrooms) {
            LatLng latLng = new LatLng(restroom.getLatitude(), restroom.getLongitude());
            float markerColor = restroom.getColor();

            map.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(restroom.getDescription())
                    .snippet("Ratings: " + restroom.getRating() + "\n" +
                            "Tags: " + restroom.getFormattedTags())
                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor)
                    ));
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        //unregisterReceiver(receiver);
        Log.d(TAG, " STOPPING THE APP");
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
                viewHolder.tags = (TextView)convertView.findViewById(R.id.view_tags);
                viewHolder.ratings = (RatingBar)convertView.findViewById(R.id.view_rating);
                viewHolder.details = (Button)convertView.findViewById(R.id.details_button);

                convertView.setTag(viewHolder);
            }

            mainViewHolder = (ViewHolder) convertView.getTag();

            final RestroomItem dItem =  this.restrooms.get(position);

            // Get color
            int color = Color.HSVToColor(new float[] {dItem.getColor(), 1, 1});

            // Set values
            mainViewHolder.title.setText(dItem.getTitle());
            mainViewHolder.distance.setText("" + dItem.getDistance());
            mainViewHolder.ratings.setRating((float) dItem.getRating());
            mainViewHolder.tags.setText("" + dItem.getTags());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mainViewHolder.imageColor.setBackgroundTintList(ColorStateList.valueOf(color));
            }

            mainViewHolder.details.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                     ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);

                    broadcastType = 2;
                    IntentFilter filter = new IntentFilter();
                    filter.addAction("review_created");
                    registerReceiver(receiver, filter);

                    Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                    String name = mainViewHolder.title.getText().toString();
                    String distance = mainViewHolder.distance.getText().toString();
                    intent.putExtra("name", name);
                    intent.putExtra("restroomID", dItem.getRestroomID());
                    intent.putExtra("restroomTags", dItem.getTags());
                    intent.putExtra("ratings", dItem.getRating());
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
        TextView tags;
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

    public ArrayList<Restroom> getCurrRestroom() {
        return new ArrayList<Restroom>(restrooms);
    }
}

