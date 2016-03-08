package com.teamoptimal.cse110project;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
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
import com.teamoptimal.cse110project.data.Restroom;
import com.teamoptimal.cse110project.data.ReviewItem;
import com.teamoptimal.cse110project.data.Review;

import java.util.ArrayList;
import java.util.List;


public class DetailActivity extends ListActivity {
    private static String TAG = "DetailActivity";
    private ListView reviewList;
    private Review review;
    private String currentID;
    private Intent intentExtra;
    private ArrayList<ReviewItem> itemComments;
    private ArrayList<Review> reviews;
    private MyAdapter adapter;
    private TextView numView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        /* Grabs the passed intent from MainActivity */
        intentExtra = getIntent();
        String name = intentExtra.getStringExtra("name");;
        String distance = intentExtra.getStringExtra("distance");
        Float ratings = intentExtra.getFloatExtra("ratings", 0.0f);

        /* Sets the TextView of the name and distance of the current bathroom displayed */
        TextView nameView = (TextView) findViewById(R.id.textView2);
        TextView distanceView = (TextView) findViewById(R.id.textView3);
        RatingBar  ratingBar2 = (RatingBar) findViewById(R.id.ratingBar2);
        Button report = (Button) findViewById(R.id.report_rest);

        numView = (TextView) findViewById(R.id.num_reviews);

        nameView.setText(name);
        distanceView.setText(distance);
        ratingBar2.setRating(ratings);

        //reviewList = (ListView)findViewById(R.id.list_reviews);
        reviewList = getListView();
        itemComments = new ArrayList<>();

        /* Grabs the Restroom ID that is clicked from MainActivity */
        currentID = intentExtra.getStringExtra("restroomID");

        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ReportActivity.class);
                intent.putExtra("object", "Restroom");
                intent.putExtra("objId", currentID);
                startActivity(intent); // Go to ReportActivity
            }
        });

        /* Initialize new Review with data from MainActivity */
        review = new Review();
        review.setRestroomID(currentID);
        review.setUserEmail(MainActivity.user.getEmail());

        // Set rating bar
        RatingBar ratingBar = (RatingBar) findViewById(R.id.getRating);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                review.setRating(rating);
            }
        });


        /* Sets the ListView of comments/ratings */
        itemComments = new ArrayList<>();
        adapter = new MyAdapter(this, R.layout.review_item, itemComments);

        new GetReviewsTask(currentID).execute();

        /* Sets the button */
        Button button = (Button) findViewById(R.id.buttonComment);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                EditText comments = (EditText) findViewById(R.id.newComments);
                review.setMessage(comments.getText().toString());

                if (review.isInitialized()) {
                    //review.updateRating(review.getRestroomID(), review.getRating());

                    new CreateReviewTask(review).execute();

                    Toast.makeText(getBaseContext(), "Review has been created", Toast.LENGTH_SHORT).show();

                    finish();
                } else {
                    Toast.makeText(getBaseContext(), "Unsuccessful", Toast.LENGTH_SHORT).show();
                }
            }
        });

        reviewList.setAdapter(adapter);
    }

    public void generateReviews() {
        itemComments.clear();

        for(Review review : reviews) {
            itemComments.add(new ReviewItem(review.getMessage(), review.getRating()));
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
            return null;
        }

        // To do after doInBackground is executed
        // We can use UI elements here
        protected void onPostExecute(Void result) {
            Log.d(TAG, "Found " + reviews.size() + " reviews");

            numView.setText(reviews.size() + " Reviews");

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
            return null;
        }

        // To do after doInBackground is executed
        // We can use UI elements here
        protected void onPostExecute(Void result) {
            //execute
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
                viewHolder.report = (ImageButton)convertView.findViewById(R.id.button_report);

                convertView.setTag(viewHolder);


            }
            mainViewHolder = (ViewHolder) convertView.getTag();
            final ReviewItem rItem = this.reviews.get(position);

            mainViewHolder.comments.setText(rItem.getComments());
            mainViewHolder.ratings.setRating(rItem.getRating());
            mainViewHolder.report.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), ReportActivity.class);
                    intent.putExtra("object", "Review");
                    intent.putExtra("objId", review.getID());
                    startActivity(intent); // Go to ReportActivity
                }
            });
            return convertView;
        }
    }

    private class ViewHolder {
        TextView comments;
        RatingBar ratings;
        ImageButton report;
    }
}

