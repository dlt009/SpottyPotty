package com.teamoptimal.cse110project;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Rating;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.teamoptimal.cse110project.data.Restroom;
import com.teamoptimal.cse110project.data.User;

import java.util.Random;

public class CreateRestroomActivity extends AppCompatActivity {
    private Restroom restroom;
    private User user;
    private AmazonClientManager clientManager;
    private static EditText description;
    private static EditText floor;

    private static String TAG = "CreateRestroomActivity";

    //static arrays for the tags and their relative position to each other
    public static final String[] Gender = {
            "Unisex",
            "Male-only",
            "Female-only"
    };
    public static final String[] Access = {
            "Public",
            "Private",
            "Pay-to-use"
    };
    public static final String[] Extraneous = {
            "Handi-accessible",
            "Changing Stations",
            "Air Dryers",
            "Restaurant",
            "Store",
            "Hotel",
            "Portable",
            "Residence"
    };
    public final static String[] tags = {
            "Unisex",
            "Male-only",
            "Female-only",
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_restroom);

        /* Initialize restroom with data from MainActivity */
        restroom = new Restroom();

        SharedPreferences pref = getSharedPreferences(MainActivity.PREFERENCES, 0);
        String email = pref.getString("user_email", "");
        restroom.setUser(email);

        /* Get ACM */
        clientManager = MainActivity.clientManager;

        /* Get last known location from the map and set the location to the restroom */
        Bundle extra = getIntent().getExtras();
        double[] location = extra.getDoubleArray("Location");
        restroom.setLocation(location[0], location[1]);

        /* Gets the editTexts from layout and sets their focus listener */
        description = (EditText)findViewById(R.id.editText);
        floor = (EditText) findViewById(R.id.editText2);
        description.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    closeKeyboard(v);
                }
            }
        });
        floor.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    closeKeyboard(v);
                }
            }
        });

        /* Set the tags */
        Spinner gender = (Spinner)findViewById(R.id.gender);
        ArrayAdapter<String> GenderAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, Gender);
        gender.setAdapter(GenderAdapter);
        gender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                restroom.setGender(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //initialize spinner for accessibility of restroom
        Spinner access = (Spinner)findViewById(R.id.access);
        ArrayAdapter<String> AccessAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, Access);
        access.setAdapter(AccessAdapter);
        access.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                restroom.setAccess(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // initialize list for restroom tags
        ListView tagList = (ListView)findViewById(R.id.list);
        tagList.setChoiceMode(tagList.CHOICE_MODE_MULTIPLE);
        tagList.setTextFilterEnabled(true);
        tagList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_checked,
                Extraneous));
        tagList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckedTextView item = (CheckedTextView) view;
                restroom.setExtraneous(position, item.isChecked());
            }
        });

        /* Set rating bar */
        RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        ratingBar.setRating(0);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                restroom.setRating((double)rating);
            }
        });

        /* Set button */
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                restroom.setDescription(description.getText().toString().trim());
                restroom.setFloor(floor.getText().toString());

                if(restroom.getDescription().length() > 20 ||
                        restroom.getDescription().length() < 6){
                    Toast.makeText(getBaseContext(),
                            "Description must be between 6 and 20 characters",
                            Toast.LENGTH_SHORT).show();
                }else if(restroom.getDescription().length() > 0) {

                    Log.d(TAG, "Restroom in process of being created");
                    Log.d(TAG, "description: " + restroom.getDescription());
                    Log.d(TAG, "user: " + restroom.getUser());
                    Log.d(TAG, "rating: " + restroom.getRating());
                    Log.d(TAG, "tags: " + restroom.getTags());
                    new CreateRestroomTask(restroom).execute();

                    Toast.makeText(getBaseContext(),
                            restroom.getDescription() + " has been created",
                            Toast.LENGTH_LONG).show();

                    MainActivity.lastCreatedRestroom = restroom;
                    Intent intent = new Intent("restroom_created");
                    sendBroadcast(intent);
                    finish();

                }else{
                    Toast.makeText(getBaseContext(), "Restroom must have a valid description",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private class CreateRestroomTask extends AsyncTask<Void, Void, Void> {
        private Restroom restroom;

        public CreateRestroomTask(Restroom restroom) {
            this.restroom = restroom;
        }

        // To do in the background
        protected Void doInBackground(Void... inputs) {
            restroom.create(); // Use the method from the User class to create it
            return null;
        }

        // To do after doInBackground is executed
        // We can use UI elements here
        protected void onPostExecute(Void result) {
            MainActivity.isDoneCreatingRestroom = true;
        }
    }

    public void closeKeyboard(View view){
        InputMethodManager manager =
                (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
