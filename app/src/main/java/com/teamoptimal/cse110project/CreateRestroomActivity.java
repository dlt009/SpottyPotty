package com.teamoptimal.cse110project;

import android.Manifest;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.TextView;
import android.view.KeyEvent;
import android.widget.RatingBar;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.Location;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.teamoptimal.cse110project.data.*;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;
import android.widget.Button;

public class CreateRestroomActivity extends ListActivity implements LocationListener {
    private Restroom restroom;
    private User user = SignInActivity.user;
    private String[] Gender = {
            "Unisex",
            "Male-only",
            "Female-only"
    };
    private String[] Access = {
            "Public",
            "Private",
            "Pay-to-use"
    };
    private String[] Amenities = {
            "Handi-accessible",
            "Changing Stations",
            "Air Dryers",
    };
    private String[] Environment = {
            "Restaurant",
            "Store",
            "Hotel",
            "Portable",
            "Residence"
    };
    public final static String[] tags = {
            "Unisex",
            "Female-only",
            "Male-only",
            "Public",
            "Private",
            "Pay-to-use",
            "Handi-accessible",
            "Changing Stations",
            "Air Dryers",
            "Restaurant",
            "Store",
            "Hotel",
            "Portable",
            "Residence"
    };
    private RatingBar ratingBar;
    protected LocationManager locationManager;
    public static AmazonClientManager clientManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_restroom);


        restroom = new Restroom();

        restroom.setUser("FakeUser@test.com"/*SignInActivity.user.getEmail()*/);
        
        clientManager = new AmazonClientManager(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria cri = new Criteria();
        String provider = locationManager.getBestProvider(cri, false);

        if (provider != null & !provider.equals("")) {
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
            Location location = locationManager.getLastKnownLocation(provider);
            locationManager.requestLocationUpdates(provider,2000,1, (LocationListener) this);
            if(location!=null)
            {
                onLocationChanged(location);
            }
            else{
                Toast.makeText(getApplicationContext(),"Location not found",Toast.LENGTH_LONG ).show();
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Provider is null",Toast.LENGTH_LONG).show();
        }

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        final LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                double[] locate = {location.getLongitude(), location.getLatitude()};
                restroom.setLocation(locate[0], locate[1]);
                Toast.makeText(getBaseContext(),"Current Location is: " + locate[0] + " and "
                        + locate[1], Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d("Latitude", "status");
            }


            @Override
            public void onProviderEnabled(String provider) {
                Log.d("Latitude", "enable");
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d("Latitude", "disable");
            }
        };
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);




        ListView tagList = getListView();
        tagList.setChoiceMode(tagList.CHOICE_MODE_MULTIPLE);
        tagList.setTextFilterEnabled(true);
        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_checked,
                tags));

        EditText name = (EditText) findViewById(R.id.editText);
        EditText floor = (EditText) findViewById(R.id.editText2);


        this.addListenerOnRatingBar();

        /*name.setOnEditorActionListener(new TextView.OnEditorActionListener(){
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent){
                boolean handled = false;
                if(i== EditorInfo.IME_ACTION_NEXT){
                    String inputName = textView.getText().toString();
                    restroom.setName(inputName);
                    handled=true;
                }
                return handled;
            }
        });

        floor.setOnEditorActionListener(new TextView.OnEditorActionListener(){
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent){
                boolean handled = false;
                if(i== EditorInfo.IME_ACTION_NEXT){
                    String inputFloor = textView.getText().toString();
                    restroom.setFloor(inputFloor);
                    handled=true;
                }
                return handled;
            }
        });*/


        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText description = (EditText)findViewById(R.id.editText);
                EditText floor = (EditText) findViewById(R.id.editText2);

                restroom.setDescription(description.getText().toString());
                restroom.setFloor(floor.getText().toString());

                if (restroom.isInitialized()) {
                    new CreateRestroomTask(restroom).execute();
                    Toast.makeText(getBaseContext(),
                            restroom.getDescription()+" has been created" /*" under the user: "+
                                    restroom.getUser()*/
                            ,Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(view.getContext(), SearchActivity.class);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(getBaseContext(),"Unsuccessful",Toast.LENGTH_SHORT).show();
                    Toast.makeText(getBaseContext(), "You must have a valid location and name"
                            ,Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void onListItemClick(ListView parent, View v, int position, long id ){
        CheckedTextView item = (CheckedTextView) v;
        restroom.setTag(position, item.isChecked());
    }


    public void addListenerOnRatingBar(){
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                restroom.setRating((double)rating);
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {

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
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private class CreateRestroomTask extends AsyncTask<Void, Void, Void> {
        private Restroom restroom;

        public CreateRestroomTask(Restroom rest) {
            this.restroom = rest;
        }

        // To do in the background
        protected Void doInBackground(Void... inputs) {
            restroom.create(); // Use the method from the User class to create it
            return null;
        }

        // To do after doInBackground is executed
        // We can use UI elements here
        protected void onPostExecute(Void result) {
        }
    }

}
