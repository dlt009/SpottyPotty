package com.teamoptimal.cse110project;

import android.Manifest;
import android.app.ListActivity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
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

public class CreateRestroomActivity extends ListActivity {
    Restroom restroom;
    User user;
    String[] tags = {
            "Public",
            "Private",
            "Pay-to-use",
            "Changing Stations",
            "Restaurant",
            "Store",
            "Unisex",
            "Female-only",
            "Male-only",
            "Clean",
            "Somewhat-Clean",
            "Somewhat-Dirty",
            "Dirty"
    };
    RatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_restroom);

        AmazonClientManager client = new AmazonClientManager(this);

        final DynamoDBMapper mapper = new DynamoDBMapper(client.ddb());

        restroom = new Restroom();

        restroom.setUser("NewUser@test.com");

        /*LocationListener locListen = new MyLocationListener();
        LocationManager locMan = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListen);*/

        ListView tagList = getListView();
        tagList.setChoiceMode(tagList.CHOICE_MODE_MULTIPLE);
        tagList.setTextFilterEnabled(true);
        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_checked,
                tags));

        EditText name = (EditText) findViewById(R.id.editText);
        EditText floor = (EditText) findViewById(R.id.editText2);
        EditText description = (EditText) findViewById(R.id.editText3);

        this.addListenerOnRatingBar();

        name.setOnEditorActionListener(new TextView.OnEditorActionListener(){
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
        });

        description.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if (i == EditorInfo.IME_ACTION_NEXT) {
                    String inputDesc = textView.getText().toString();
                    restroom.setDesc(inputDesc);
                    handled=true;
                }
                return handled;
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (restroom.isInitialized()) {
                    mapper.save(restroom);
                }
            }
        });
    }

    public void onListItemClick(ListView parent, View v, int position, long id ){
        CheckedTextView item = (CheckedTextView) v;
        restroom.setTag(position, item.isChecked());
    }

    /*public void onLocationChanged(Location location) {
        double[] locate = {location.getLatitude(), location.getLongitude()};
        restroom.setLoc(locate[0], locate[1]);
    }*/

    public void addListenerOnRatingBar(){
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                restroom.setRating((double) rating);
            }
        });
    }
}
