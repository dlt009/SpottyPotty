package com.teamoptimal.cse110project;

import android.app.ListActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;

public class FilterActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Spinner spinnerGender, spinnerRating;
    private ListView lvPlaces, lvExtras;

    public final static String[] places = {
            "Restaurant",
            "Store",
            "Portable"
    };
    public final static String[] extras = {
            "Handi-accessible",
            "Changing Stations",
            "Air Dryers"
    };

    public ArrayList<String> filters = new ArrayList<String>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_layout);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        double widthAdjust = 0.8;
        double heightAdjust = 0.75;

        getWindow().setLayout((int) (width * widthAdjust), (int) (height * heightAdjust));

        // Spinner element
        spinnerGender = (Spinner) findViewById(R.id.spinner1);
        spinnerGender.setOnItemSelectedListener(this);

        spinnerRating = (Spinner) findViewById(R.id.spinner2);
        spinnerRating.setOnItemSelectedListener(this);


        // Spinner Drop down elements
        List<String> genders = new ArrayList<>();
        genders.add("Unisex");
        genders.add("Male");
        genders.add("Female");

        List<String> ratings = new ArrayList<>();
        ratings.add("4+");
        ratings.add("3+");
        ratings.add("2+");
        ratings.add("1+");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter1 = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, genders);
        ArrayAdapter<String> dataAdapter2 = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, ratings);

        // Drop down layout style - list view with radio button
        dataAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dataAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinnerGender.setAdapter(dataAdapter1);
        spinnerRating.setAdapter(dataAdapter2);


        //Set the tags
        lvPlaces = (ListView) findViewById(R.id.filter_list_place);
        lvPlaces.setChoiceMode(lvPlaces.CHOICE_MODE_MULTIPLE);
        lvPlaces.setTextFilterEnabled(true);
        lvPlaces.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice,
                places));


        lvExtras = (ListView) findViewById(R.id.filter_list_extra);
        lvExtras.setChoiceMode(lvExtras.CHOICE_MODE_MULTIPLE);
        lvExtras.setTextFilterEnabled(true);
        lvExtras.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice,
                extras));


    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // TODO stuff
    }
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO stuff
    }

}


