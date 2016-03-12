package com.teamoptimal.cse110project;

import android.app.ListActivity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;


import com.teamoptimal.cse110project.data.Restroom;

import java.util.ArrayList;
import java.util.List;

public class FilterActivity extends ListActivity implements AdapterView.OnItemSelectedListener {

    private Spinner spinnerGender, spinnerRating, lvPlaces;
    private ListView lvExtras;

    private Restroom filter = new Restroom();

    private static String TAG = "FilterActivity";

    public ArrayList<String> filters = new ArrayList<String>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_layout);

        // Spinner element
        spinnerGender = (Spinner) findViewById(R.id.spinner1);
        spinnerGender.setOnItemSelectedListener(this);

        spinnerRating = (Spinner) findViewById(R.id.spinner2);
        spinnerRating.setOnItemSelectedListener(this);

        //takes list of specialized tags from
        String[] Gender = CreateRestroomActivity.Gender;
        String[] Access = CreateRestroomActivity.Access;

        // Spinner Drop down elements
        List<String> genders = new ArrayList<>();
        genders.add("No Preference");
        for(int i=0; i<Gender.length; i++){
            genders.add(Gender[i]);
        }

        List<String> ratings = new ArrayList<>();
        for(int i=0; i<5; i++){
            ratings.add(i+"+");
        }

        List<String> accessibility = new ArrayList<>();
        accessibility.add("No Preference");
        for(int i=0; i<Access.length; i++){
            accessibility.add(Access[i]);
        }

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter1 = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, genders);
        ArrayAdapter<String> dataAdapter2 = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, ratings);
        ArrayAdapter<String> dataAdapter3 = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, accessibility);

        // Drop down layout style - list view with radio button
        dataAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dataAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dataAdapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinnerGender.setAdapter(dataAdapter1);
        spinnerGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filter.setGender(position-1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //attaches data adapter to spinner
        spinnerRating.setAdapter(dataAdapter2);
        spinnerRating.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filter.setRating((double) (position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //Set the tags
        lvPlaces = (Spinner) findViewById(R.id.filter_list_access);
        lvPlaces.setAdapter(dataAdapter3);
        lvPlaces.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filter.setAccess(position-1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        //initializes the list for the non-mutually exclusive tags
        lvExtras = getListView();
        lvExtras.setChoiceMode(lvExtras.CHOICE_MODE_MULTIPLE);
        lvExtras.setTextFilterEnabled(true);
        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_checked,
                CreateRestroomActivity.Extraneous));
        lvExtras.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckedTextView item = (CheckedTextView) view;
                filter.setExtraneous(position, item.isChecked());
            }
        });

        //create button for applying the filter to our map through a BroadcastReceiver
        Button apply = (Button)findViewById(R.id.button_apply);
        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.filter = filter.getTags();
                MainActivity.rated = filter.getRating();
                Intent intent = new Intent("filter_done");
                sendBroadcast(intent);
                finish();
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // TODO stuff
        CheckedTextView item = (CheckedTextView) view;
        filter.setExtraneous(position, item.isChecked());
    }
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO stuff
    }


}