package com.teamoptimal.cse110project.data;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.CheckBox;


import com.teamoptimal.cse110project.R;

import java.util.ArrayList;

/**
 * Created by Sydney on 2/23/2016.
 */
public class FilterActivity extends AppCompatActivity {

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

    public ArrayList<String> filters = new ArrayList<String>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_layout);

        CheckBox unisex = (CheckBox) findViewById(R.id.checkBox);
        unisex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((CheckBox) view).isChecked()) {
                    filters.add("Unisex");
                }
            }
        });
        CheckBox female = (CheckBox) findViewById(R.id.checkBox2);
        female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((CheckBox)view).isChecked()){
                    filters.add("Female-only");
                }
            }
        });
        CheckBox male = (CheckBox) findViewById(R.id.checkBox3);
        female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((CheckBox)view).isChecked()){
                    filters.add("Male-only");
                }
            }
        });
        CheckBox pub = (CheckBox) findViewById(R.id.checkBox4);
        pub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((CheckBox)view).isChecked()){
                    filters.add("Public");
                }
            }
        });
        CheckBox priv = (CheckBox) findViewById(R.id.checkBox5);
        priv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((CheckBox)view).isChecked()){
                    filters.add("Private");
                }
            }
        });

        CheckBox pay = (CheckBox) findViewById(R.id.checkBox6);
        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((CheckBox)view).isChecked()){
                    filters.add("Pay-to-use");
                }
            }
        });

        CheckBox restaurant = (CheckBox) findViewById(R.id.checkBox7);
        restaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((CheckBox)view).isChecked()){
                    filters.add("Restaurant");
                }
            }
        });

        CheckBox store = (CheckBox) findViewById(R.id.checkBox8);
        store.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((CheckBox)view).isChecked()){
                    filters.add("Store");
                }
            }
        });

        CheckBox hotel = (CheckBox) findViewById(R.id.checkBox9);
        hotel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((CheckBox)view).isChecked()){
                    filters.add("Hotel");
                }
            }
        });

        CheckBox portable = (CheckBox) findViewById(R.id.checkBox10);
        portable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((CheckBox)view).isChecked()){
                    filters.add("Portable");
                }
            }
        });

        CheckBox residence = (CheckBox) findViewById(R.id.checkBox11);
        residence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((CheckBox)view).isChecked()){
                    filters.add("Residence");
                }
            }
        });

        CheckBox handi = (CheckBox) findViewById(R.id.checkBox12);
        handi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((CheckBox)view).isChecked()){
                    filters.add("Handi-accessible");
                }
            }
        });

        CheckBox dry = (CheckBox) findViewById(R.id.checkBox13);
        dry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((CheckBox)view).isChecked()){
                    filters.add("AirDryers");
                }
            }
        });

        CheckBox changing = (CheckBox) findViewById(R.id.checkBox14);
        changing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((CheckBox)view).isChecked()){
                    filters.add("Changing Stations");
                }
            }
        });

        Button apply = (Button) findViewById(R.id.button);
        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((CheckBox)view).isChecked()){
                    //SEARCH(filters)
                }
            }
        });

    }
}
