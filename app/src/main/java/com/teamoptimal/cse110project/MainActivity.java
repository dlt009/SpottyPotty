package com.teamoptimal.cse110project;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    ImageButton imgButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imgButton =(ImageButton)findViewById(R.id.findrr_button);
        imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //goToMaps();
            }
        });
        imgButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        imgButton.setColorFilter(Color.argb(255, 255, 255, 255)); // Tint button
                        goToMaps();
                        return true;
                    case MotionEvent.ACTION_UP:
                        imgButton.clearColorFilter(); // Undo Tint
                        return true;
                }
                return false;
            }
        });
    }



    private void goToMaps() {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

}
