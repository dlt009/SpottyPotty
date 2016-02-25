package com.teamoptimal.cse110project.data;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.teamoptimal.cse110project.R;

public class DisplayBathroom extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bathroom_page);

        TextView bathroomName = (TextView) findViewById(R.id.textView2);
        //bathroomName.setText(name); <-- SET NAME TO BATHROOM NAME AFTER GETTING BATHROOM NAME

        TextView bathroomDesc = (TextView) findViewById(R.id.textView3);
        //bathroomDesc.setText(desc); <-- SET BATHROOM DESC

        ListView comments = (ListView) findViewById(R.id.listView);

    }
}
