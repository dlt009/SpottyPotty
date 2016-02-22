package com.teamoptimal.cse110project;

import android.Manifest;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.teamoptimal.cse110project.data.*;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;
import android.widget.Button;

public class CreateRestroomActivity extends ListActivity {
    public static Restroom restroom;
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
    private static final float[] colors = {BitmapDescriptorFactory.HUE_YELLOW, BitmapDescriptorFactory.HUE_ROSE, BitmapDescriptorFactory.HUE_CYAN,
            BitmapDescriptorFactory.HUE_GREEN, BitmapDescriptorFactory.HUE_MAGENTA, BitmapDescriptorFactory.HUE_ORANGE,
            BitmapDescriptorFactory.HUE_RED, BitmapDescriptorFactory.HUE_BLUE, BitmapDescriptorFactory.HUE_VIOLET,
            BitmapDescriptorFactory.HUE_AZURE};
    private int cCount = 0;
    private RatingBar ratingBar;
    private static final String PREFERENCES = "AppPrefs";
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    public static AmazonClientManager clientManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_restroom);

        sharedPreferences = getSharedPreferences(PREFERENCES, 0);
        editor = sharedPreferences.edit();

        cCount = sharedPreferences.getInt("color", 0);

        Bundle extra = getIntent().getExtras();
        double[] location = extra.getDoubleArray("Location");


        restroom = new Restroom();

        restroom.setUser("FakeUser@test.com"/*SignInActivity.user.getEmail()*/);
        
        clientManager = new AmazonClientManager(this);

        if (cCount >= 10) cCount = 0;
        restroom.setColor(colors[cCount]);
        cCount++;
        editor.putInt("color", cCount).commit();

        restroom.setLocation(location[0], location[1]);

        ListView tagList = getListView();
        tagList.setChoiceMode(tagList.CHOICE_MODE_MULTIPLE);
        tagList.setTextFilterEnabled(true);
        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_checked,
                tags));

        EditText name = (EditText) findViewById(R.id.editText);
        EditText floor = (EditText) findViewById(R.id.editText2);

        this.addListenerOnRatingBar();

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
                            restroom.getDescription() + " has been created", Toast.LENGTH_LONG).show();

                    finish();
                }
                else{
                    Toast.makeText(getBaseContext(), "Unsuccessful", Toast.LENGTH_SHORT).show();
                    Toast.makeText(getBaseContext(), "You must have a valid location and name",
                            Toast.LENGTH_LONG).show();
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
