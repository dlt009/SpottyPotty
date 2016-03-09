package com.teamoptimal.cse110project;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

    private Report report;
    private User user;
    private User target;
    private EditText description;
    private TextView text;
    private Button submitButton;

    private String TAG = "ReportActivity";

    private SharedPreferences sharedPreferences;

    private static final String PREFERENCES = "AppPrefs";

    private static Restroom restroom;
    private static Review review;

    private String reportObj;
    private String objId;
    private static DynamoDBMapper mapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_report);

        clientManager = new AmazonClientManager(this);
        mapper = new DynamoDBMapper(clientManager.ddb());

        report = new Report();

        sharedPreferences = getSharedPreferences(PREFERENCES, 0);
        new getUserTask(sharedPreferences.getString("user_email", ""), "User").execute();

        submitButton = (Button) findViewById(R.id.submitButton);
        submitButton.setVisibility(View.INVISIBLE);

        text = (TextView) findViewById(R.id.Title);
        description = (EditText) findViewById(R.id.reportDesc);
        description.setVisibility(View.INVISIBLE);
        description.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    closeKeyboard(v);
                }
            }
        });

        Intent intentExtra = getIntent();
        reportObj = intentExtra.getStringExtra("object");
        objId = intentExtra.getStringExtra("objId");

        Log.d(TAG, "Activity Started with "+reportObj+" of id: "+objId+"\n Reported by "
                +sharedPreferences.getString("user_email",""));

        if(reportObj.equals("Restroom")){
            Log.d(TAG, "reportObj indicates a restroom");
            new getRestroomTask(objId).execute();
        }
        else if(reportObj.equals("Review")){
            Log.d(TAG, "reportObj indicates a review");
            new getReviewTask(objId).execute();
        }
        else{
            Toast.makeText(getBaseContext(), "Cannot find the item to be reported",
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String desc = description.getText().toString().trim();

                if(reportObj.equals("Restroom")){
                    new ReportRestroomTask(user, restroom, desc, target).execute();
                }
                else if(reportObj.equals("Review")){
                    new ReportReviewTask(user, review, desc, target).execute();
                }
                else{
                    Toast.makeText(getBaseContext(), "Cannot find the item to be reported",
                            Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private class getUserTask extends AsyncTask<Void, Void, Void> {
        private String user_email;
        private String thisUser;

        public getUserTask(String user_email, String target) {
            this.user_email = user_email;
            this.thisUser = target;
        }

        // To do in the background
        protected Void doInBackground(Void... inputs) {
            if(this.thisUser.equals("User")){
                user = mapper.load(new User().getClass(), this.user_email);
            }else if(thisUser.equals("Target")){
                target = mapper.load(new User().getClass(), this.user_email);
            }
            return null;
        }

        // To do after doInBackground is executed
        // We can use UI elements here
        protected void onPostExecute(Void result) {
            if(this.thisUser.equals("Target")){
                Log.d(TAG, "Target User has been loaded with ID:"+target.getEmail());
                submitButton.setVisibility(View.VISIBLE);
            }
            else{
                Log.d(TAG, "Current User has been loaded with ID:"+user.getEmail());
                description.setVisibility(View.VISIBLE);
            }
        }
    }

    private class getRestroomTask extends AsyncTask<Void, Void, Void> {
        private String restroomId;

        public getRestroomTask(String restroomId) {
            this.restroomId = restroomId;
        }

        // To do in the background
        protected Void doInBackground(Void... inputs) {
            restroom = mapper.load(new Restroom().getClass(), this.restroomId);
            Log.d(TAG, "Restroom: " + restroom.getDescription() + " has been loaded " +
                    "\n made by "+restroom.getUser());
            return null;
        }

        // To do after doInBackground is executed
        // We can use UI elements here
        protected void onPostExecute(Void result) {
            new getUserTask(restroom.getUser(), "Target").execute();
        }
    }

    private class getReviewTask extends AsyncTask<Void, Void, Void> {
        private String reviewId;

        public getReviewTask(String reviewId) {
            this.reviewId = reviewId;
        }

        // To do in the background
        protected Void doInBackground(Void... inputs) {
            review = mapper.load(new Review().getClass(), this.reviewId); // Use the method from the User class to create it

            return null;
        }

        // To do after doInBackground is executed
        // We can use UI elements here
        protected void onPostExecute(Void result) {
            Log.d(TAG, "Review: "+review.getMessage()+" has been loaded with user "+review.getUserEmail());
            new getUserTask(review.getUserEmail(), "Target").execute();
        }
    }

    private class ReportRestroomTask extends AsyncTask<Void, Void, Void> {
        private User user;
        private Restroom restroom;
        private String description;
        private User target;

        public ReportRestroomTask(User user, Restroom restroom, String description, User target) {
            this.user = user;
            this.description = description;
            this.restroom = restroom;
            this.target = target;
        }

        // To do in the background
        protected Void doInBackground(Void... inputs) {
            user.reportRestroom(this.restroom,this.description, this.target); // Use the method from the User class to create it
            return null;
        }

        // To do after doInBackground is executed
        // We can use UI elements here
        protected void onPostExecute(Void result) {
            Toast.makeText(getBaseContext(), "Restroom has been reported\n" +
                            "Thank you for your diligence!",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private class ReportReviewTask extends AsyncTask<Void, Void, Void> {
        private User user;
        private Review review;
        private String description;
        private User target;

        public ReportReviewTask(User user, Review review, String description, User target) {
            this.user = user;
            this.description = description;
            this.review = review;
            this.target = target;
        }

        // To do in the background
        protected Void doInBackground(Void... inputs) {
            user.reportReview(this.review,this.description, this.target); // Use the method from the User class to create it
            return null;
        }

        // To do after doInBackground is executed
        // We can use UI elements here
        protected void onPostExecute(Void result) {
            Toast.makeText(getBaseContext(), "Review has been reported\n" +
                            "Thank you for your diligence!",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void closeKeyboard(View view){
        InputMethodManager manager =
                (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
