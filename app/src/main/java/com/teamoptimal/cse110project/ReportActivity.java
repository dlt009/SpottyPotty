package com.teamoptimal.cse110project;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.teamoptimal.cse110project.AmazonClientManager;
import com.teamoptimal.cse110project.R;
import com.teamoptimal.cse110project.data.Report;
import com.teamoptimal.cse110project.data.Restroom;
import com.teamoptimal.cse110project.data.Review;
import com.teamoptimal.cse110project.data.User;

public class ReportActivity extends AppCompatActivity {

    public static AmazonClientManager clientManager = null;

    private Report report = new Report();

    private User user = new User();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*Bundle extra = getIntent().getExtras();
        String currUser = extra.getString("User");
        String reportObj = extras.getString("Reported_Object");

        TextView text = (TextView) findViewById(R.id.Title);

        if(currRestroom != null){
            text.setText("Restroom");
        }
        else if(currReview != null){
            text.setText("Review");
        }
        else return;
        */
        user.setEmail("FakeUser@test.com");

        clientManager = new AmazonClientManager(this);

        /*DynamoDBMapper mapper = new DynamoDBMapper(clientManager.ddb());
        mapper.load(user, currUser);*/


        Button submitButton = (Button) findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText description = (EditText) findViewById(R.id.reportDesc);
                Restroom restroom = new Restroom();
                Review review = new Review();
                if(restroom != null){
                    user.reportRestroom(restroom, description.getText().toString());
                    finish();
                }
                else if(review != null){
                    user.reportReview(review, description.getText().toString());
                    finish();
                }
                else{
                    Toast.makeText(getBaseContext(), "Cannot find the item to be reported",
                            Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

}
