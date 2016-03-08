package com.teamoptimal.cse110project;

import android.app.ListActivity;
import android.media.Rating;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
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

public class CreateRestroomActivity extends ListActivity {
    private Restroom restroom;
    private User user;
    private AmazonClientManager clientManager;

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
        user = MainActivity.user;
        

        String email;
        if(SignInActivity.user == null){ email = "FakeUser@test.com";}
        else email = SignInActivity.user.getEmail();
        restroom.setUser(email);

        /* Get ACM */
        clientManager = MainActivity.clientManager;

        /* Get last known location from the map and set the location to the restroom */
        Bundle extra = getIntent().getExtras();
        double[] location = extra.getDoubleArray("Location");
        restroom.setLocation(location[0], location[1]);

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

        ListView tagList = getListView();
        tagList.setChoiceMode(tagList.CHOICE_MODE_MULTIPLE);
        tagList.setTextFilterEnabled(true);
        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_checked,
                Extraneous));
        tagList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CheckedTextView item = (CheckedTextView) view;
                restroom.setExtraneous(position, item.isChecked());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        /* Set rating bar */
        RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);
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

                EditText description = (EditText)findViewById(R.id.editText);
                EditText floor = (EditText) findViewById(R.id.editText2);

                restroom.setDescription(description.getText().toString());
                restroom.setFloor(floor.getText().toString());

                if(restroom.getDescription() != "") {
                    new CreateRestroomTask(restroom).execute();
                    Toast.makeText(getBaseContext(),
                            restroom.getDescription() + " has been created",
                            Toast.LENGTH_LONG).show();

                    finish();
                } else {
                    Toast.makeText(getBaseContext(), "Restroom must have a valid name",
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
        }
    }

}
