package com.teamoptimal.cse110project.data;

import android.app.ListActivity;

import android.os.Bundle;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;


import com.teamoptimal.cse110project.CreateRestroomActivity;
import com.teamoptimal.cse110project.MainActivity;
import com.teamoptimal.cse110project.R;

import java.util.ArrayList;

/**
 * Created by Sydney on 2/23/2016.
 */
public class FilterActivity extends ListActivity {

    public final String[] rating = {
            "1 or higher",
            "2 or higher",
            "3 or higher",
            "4 or higher"
    };

    public ArrayList<String> filters = new ArrayList<String>();

    private Restroom filter = new Restroom();


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_layout);

        Spinner gender = (Spinner)findViewById(R.id.spinner1);
        ArrayAdapter<String> GenderAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, CreateRestroomActivity.Gender);
        gender.setAdapter(GenderAdapter);
        gender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filter.setGender(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Spinner rating = (Spinner)findViewById(R.id.spinner2);
        ArrayAdapter<String> RatingAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, this.rating);
        rating.setAdapter(GenderAdapter);
        rating.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filter.setRating((double) (position + 1));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Spinner access = (Spinner)findViewById(R.id.filter_list_access);
        ArrayAdapter<String> AccessAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, CreateRestroomActivity.Access);
        gender.setAdapter(GenderAdapter);
        gender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filter.setAccess(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ListView tagList = (ListView)findViewById(android.R.id.list);
        tagList.setChoiceMode(tagList.CHOICE_MODE_MULTIPLE);
        tagList.setTextFilterEnabled(true);
        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_checked,
                CreateRestroomActivity.Extraneous));
        tagList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CheckedTextView item = (CheckedTextView) view;
                filter.setExtraneous(position, item.isChecked());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Button apply = (Button)findViewById(R.id.button_apply);
        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.restrooms = filter.filterRestrooms(MainActivity.restrooms,
                        filter.getTags(), filter.getRating());
                finish();
            }
        });
    }
}
