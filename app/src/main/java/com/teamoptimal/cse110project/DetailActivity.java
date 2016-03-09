package com.teamoptimal.cse110project;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.teamoptimal.cse110project.data.Report;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.teamoptimal.cse110project.data.ReviewItem;
import com.teamoptimal.cse110project.data.Review;
import com.teamoptimal.cse110project.data.User;

import java.util.ArrayList;
import java.util.List;


public class DetailActivity extends ListActivity {
    private static String TAG = "DetailActivity";
    private static final String PREFERENCES = "AppPrefs";
    private SharedPreferences sharedPreferences;
    private ListView reviewList;
    private static Review review;
    private String currentID;
    private Intent intentExtra;
    private ArrayList<ReviewItem> itemComments;
    private ArrayList<Review> reviews;
    private MyAdapter adapter;
    private TextView numView;
    private double ratings;
    private RatingBar averageRating;
    private static String user_email;
    private int reportCount;
    private boolean signedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        /* Grabs the passed intent from MainActivity */
        intentExtra = getIntent();
        String name = intentExtra.getStringExtra("name");
        String distance = intentExtra.getStringExtra("distance");

        signedIn = MainActivity.signedInFacebook || MainActivity.signedInGoogle ||
                MainActivity.signedInTwitter;

        /* Sets the TextView of the name and distance of the current bathroom displayed */
        TextView nameView = (TextView) findViewById(R.id.textView2);
        TextView distanceView = (TextView) findViewById(R.id.textView3);
        Button report = (Button) findViewById(R.id.report_rest);

        nameView.setText(name);
        distanceView.setText(distance);

        averageRating = (RatingBar) findViewById(R.id.ratingBar2);
        numView = (TextView) findViewById(R.id.num_reviews);

        reviewList = getListView();
        itemComments = new ArrayList<>();

        /* Grabs the Restroom ID that is clicked from MainActivity */
        currentID = intentExtra.getStringExtra("restroomID");

        /* Initialize new Review with data from MainActivity */
        review = new Review();
        review.setRestroomID(currentID);

        sharedPreferences = getSharedPreferences(PREFERENCES, 0);

        Log.d(TAG, "hey "+ sharedPreferences.getString("user_email",""));

        user_email = sharedPreferences.getString("user_email", "");
        review.setUserEmail(user_email);

        reportCount = sharedPreferences.getInt("times_reported", 0);

        /* Sets the ListView of comments/ratings */
        itemComments = new ArrayList<>();
        adapter = new MyAdapter(this, R.layout.review_item, itemComments);

        new GetReviewsTask(currentID).execute();

        // Set rating bar
        RatingBar ratingBar = (RatingBar) findViewById(R.id.getRating);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                review.setRating(rating);
            }
        });

        /* Sets the button */
        Button button = (Button) findViewById(R.id.buttonComment);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                EditText comments = (EditText) findViewById(R.id.newComments);
                review.setMessage(comments.getText().toString().trim());
                if(!signedIn){
                    Toast.makeText(getBaseContext(), "Cannot create a review\n"+
                            "Reason: User is not signed in",
                            Toast.LENGTH_SHORT).show();
                }else if(reportCount > 4){
                    Toast.makeText(getBaseContext(), "Cannot create a review\n"+
                                    "Reason: too many reports against content created by current user",
                            Toast.LENGTH_SHORT).show();
                }
                else if (review.isInitialized()) {
                    //review.updateRating(review.getRestroomID(), review.getRating());
                    new CreateReviewTask(review).execute();
                    Toast.makeText(getBaseContext(), "Review has been created", Toast.LENGTH_SHORT).show();
                    finish();

                } else {
                    Toast.makeText(getBaseContext(), "Unsuccessful", Toast.LENGTH_SHORT).show();
                }
            }
        });

        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!signedIn){
                    Toast.makeText(getBaseContext(), "Cannot report a review\n" +
                                    "Reason: User is not signed in",
                            Toast.LENGTH_SHORT).show();
                }else if (reportCount > 4) {
                    Toast.makeText(getBaseContext(), "Cannot report a review\n" +
                                    "Reason: too many reports against content created by this user",
                            Toast.LENGTH_SHORT).show();
                }else new goToReportTask(user_email, currentID, "Restroom").execute();
            }
        });

        reviewList.setAdapter(adapter);

    }

    public void generateReviews() {
        itemComments.clear();

        for(Review review : reviews) {
            itemComments.add(new ReviewItem(review.getMessage(), review.getRating(), review.getID()));
        }
        adapter.notifyDataSetChanged();
    }

    private class GetReviewsTask extends AsyncTask<Void, Void, Void> {
        private String restID;

        public GetReviewsTask(String restID) {
            this.restID = restID;
        }

        // To do in the background
        protected Void doInBackground(Void...params) {
            Log.d(TAG, "doInBackground");
            reviews = Review.getReviews(restID);
            ratings = Review.getRating(restID);
            return null;
        }

        // To do after doInBackground is executed
        // We can use UI elements here
        protected void onPostExecute(Void result) {
            Log.d(TAG, "Found " + reviews.size() + " reviews");

            numView.setText(reviews.size() + " Reviews");
            averageRating.setRating((float)ratings);

            generateReviews();
        }
    }

    private class CreateReviewTask extends AsyncTask<Void, Void, Void> {
        private Review review;

        public CreateReviewTask(Review review) {
            this.review = review;
        }

        // To do in the background
        protected Void doInBackground(Void... inputs) {
            review.createReview(); // Use the method from the User class to create it
            review.updateRating(review.getRestroomID(), review.getRating());
            ratings = review.getRating();
            return null;
        }

        // To do after doInBackground is executed
        // We can use UI elements here
        protected void onPostExecute(Void result) {
            averageRating.setRating((float) ratings);
        }
    }

    private class MyAdapter extends ArrayAdapter<ReviewItem> {
        private List<ReviewItem> reviews;
        private int layout;

        public MyAdapter(Context context, int resource, List<ReviewItem> reviews) {
            super(context, resource, reviews);
            this.reviews = reviews;
            layout = resource;
        }

        public View getView(int position, View convertView, ViewGroup parent){
            final ViewHolder mainViewHolder;

            if(convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(layout, parent, false);

                ViewHolder viewHolder = new ViewHolder();
                viewHolder.comments = (TextView)convertView.findViewById(R.id.comments);
                viewHolder.ratings = (RatingBar)convertView.findViewById(R.id.setRating);
                viewHolder.reportReview = (ImageButton)convertView.findViewById(R.id.button_report);

                convertView.setTag(viewHolder);
            }
            mainViewHolder = (ViewHolder) convertView.getTag();
            final ReviewItem rItem = this.reviews.get(position);

            mainViewHolder.comments.setText(rItem.getComments());
            mainViewHolder.ratings.setRating(rItem.getRating());

            mainViewHolder.reportReview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!signedIn){
                        Toast.makeText(getBaseContext(), "Cannot report a review\n" +
                                        "Reason: User is not signed in",
                                Toast.LENGTH_SHORT).show();
                    }else if (reportCount > 4) {
                        Toast.makeText(getBaseContext(), "Cannot report a review\n" +
                                        "Reason: too many reports against content created by user",
                                Toast.LENGTH_SHORT).show();
                    } else new goToReportTask(user_email, rItem.getReviewID(),"Review").execute();
                }
            });
            return convertView;
        }
    }

    private class ViewHolder {
        TextView comments;
        RatingBar ratings;
        ImageButton reportReview;
    }

    private class goToReportTask extends AsyncTask<Void, Void, Void> {
        private String user;
        private String objectId;
        private String objectType;
        private boolean reportable;

        public goToReportTask(String user_email, String objectId, String objectType) {
            this.user = user_email;
            this.objectId = objectId;
            this.objectType = objectType;
        }

        // To do in the background
        protected Void doInBackground(Void... inputs) {
            reportable = !Report.hasReported(user, objectId, objectType);
            return null;
        }

        // To do after doInBackground is executed
        // We can use UI elements here
        protected void onPostExecute(Void result) {
            if(reportable){
                Log.d(TAG, objectType+" of ID: "+objectId+" has report status of:"+reportable );
                Intent intent = new Intent(getApplicationContext(), ReportActivity.class);
                intent.putExtra("object", objectType);
                intent.putExtra("objId", objectId);
                startActivity(intent); // Go to ReportActivity
            } else {
                Toast.makeText(getBaseContext(), "You have already reported this "+objectType,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}


