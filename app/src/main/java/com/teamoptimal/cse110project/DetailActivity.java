package com.teamoptimal.cse110project;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.teamoptimal.cse110project.data.Report;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.teamoptimal.cse110project.data.Restroom;
import com.teamoptimal.cse110project.data.RestroomItem;
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
    private EditText comments;
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

        sharedPreferences = getSharedPreferences(PREFERENCES, 0);

        Log.d(TAG, "hey "+ sharedPreferences.getString("user_email",""));

        /* Grabs the passed intent from MainActivity */
        intentExtra = getIntent();
        String name = intentExtra.getStringExtra("name");
        String tag = intentExtra.getStringExtra("restroomTags");

        /*determine the existence of a signed in user */
        signedIn = MainActivity.signedInFacebook || MainActivity.signedInGoogle ||
                MainActivity.signedInTwitter;

        /* Grabs the Restroom ID that is clicked from MainActivity */
        currentID = intentExtra.getStringExtra("restroomID");
        reportCount = sharedPreferences.getInt("times_reported", 0);
        user_email = sharedPreferences.getString("user_email", "");

        /* Initialize new Review with data from MainActivity */
        review = new Review();
        review.setRestroomID(currentID);
        review.setUserEmail(user_email);
        
        if(signedIn)
            findViewById(R.id.bottom).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.bottom).setVisibility(View.GONE);

        /* Sets the TextView of the name and distance of the current bathroom displayed */
        TextView nameView = (TextView) findViewById(R.id.textView2);
        TextView tags = (TextView)findViewById(R.id.tag_list);
        Button report = (Button) findViewById(R.id.report_rest);

        nameView.setText(name);
        tags.setText(tag);

        /* Initialize the rating bar and number of reviews for the restroom tile */
        averageRating = (RatingBar) findViewById(R.id.ratingBar2);
        numView = (TextView) findViewById(R.id.num_reviews);

        /* Set the layout of details to focusable */
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.detail_layout);
        layout.setClickable(true);
        layout.setFocusableInTouchMode(true);

        /* Sets the ListView of comments/ratings */
        reviewList = getListView();
        itemComments = new ArrayList<>();
        adapter = new MyAdapter(this, R.layout.review_item, itemComments);

        /* Retrieve Reviews from database*/
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

                comments = (EditText) findViewById(R.id.newComments);
                comments.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (!hasFocus) {
                            closeKeyboard(v);
                        }
                    }
                });

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
                    new CreateReviewTask(review).execute();
                    Toast.makeText(getBaseContext(), "Review has been created", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(getBaseContext(), "Unsuccessful", Toast.LENGTH_SHORT).show();
                }
            }
        });

        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!signedIn){
                    Toast.makeText(getBaseContext(), "Cannot report this restroom\n" +
                                    "Reason: User is not signed in",
                            Toast.LENGTH_SHORT).show();
                } else if (reportCount > 4) {
                    Toast.makeText(getBaseContext(), "Cannot report a restroom\n" +
                                    "Reason: too many reports against content created by this user",
                            Toast.LENGTH_SHORT).show();
                } else new goToReportTask(user_email, currentID, "Restroom").execute();
            }
        });

        reviewList.setAdapter(adapter);

    }

    public void generateReviews() {
        itemComments.clear();

        for(Review review : reviews) {
            if(review.getReportCount()<5) {
                itemComments.add(new ReviewItem(review.getMessage(), review.getRating(),
                        review.getID(), review.getThumbsUp(), review.getThumbsDown(), 0));
            }
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
            MainActivity.lastRated = ratings;
            MainActivity.lastRatedID = currentID;
            Intent intent = new Intent("review_created");
            sendBroadcast(intent);
            finish();
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

        public View getView(final int position, View convertView, ViewGroup parent){
            final ViewHolder mainViewHolder;

            if(convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(layout, parent, false);

                ViewHolder viewHolder = new ViewHolder();
                viewHolder.comments = (TextView)convertView.findViewById(R.id.comments);
                viewHolder.ratings = (RatingBar)convertView.findViewById(R.id.setRating);
                viewHolder.reportReview = (ImageButton)convertView.findViewById(R.id.button_report);
                viewHolder.thumbsUp = (ImageButton) convertView.findViewById(R.id.thumbs_up);
                viewHolder.thumbsDown = (ImageButton) convertView.findViewById(R.id.thumbs_down);
                viewHolder.thumbs = (TextView) convertView.findViewById(R.id.thumbs_sum);

                convertView.setTag(viewHolder);
            }
            mainViewHolder = (ViewHolder) convertView.getTag();
            ReviewItem item = this.reviews.get(position);

            mainViewHolder.comments.setText(item.getComments());
            mainViewHolder.ratings.setRating(item.getRating());
            mainViewHolder.thumbs.setText("" + (item.getThumbsUp() - item.getThumbsDown()));

            mainViewHolder.thumbsUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(reportCount < 5) {
                        Log.d(TAG, "thumbsupclick");
                        ReviewItem reviewItem = reviews.get(position);
                        if (reviewItem.getAction() != 0)
                            return;

                        reviewItem.setThumbsUp(reviewItem.getThumbsUp() + 1);
                        reviewItem.setAction(1);
                        mainViewHolder.thumbs.setText("" + (reviewItem.getThumbsUp() -
                                reviewItem.getThumbsDown()));
                        new getReviewTask(reviewItem.getReviewID(), 1).execute();
                    } else {
                        Toast.makeText(getBaseContext(), "Cannot create a review\n" +
                                        "Reason: too many reports against content created by this user",
                                Toast.LENGTH_SHORT).show();
                    }

                }
            });

            mainViewHolder.thumbsDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(reportCount<5) {
                        Log.d(TAG, "thumbsdownclick");
                        ReviewItem reviewItem = reviews.get(position);
                        if (reviews.get(position).getAction() != 0)
                            return;

                        reviewItem.setThumbsDown(reviewItem.getThumbsDown() + 1);
                        reviewItem.setAction(-1);
                        mainViewHolder.thumbs.setText("" + (reviewItem.getThumbsUp() -
                                reviewItem.getThumbsDown()));
                        new getReviewTask(reviewItem.getReviewID(), -1).execute();
                    }else{
                        Toast.makeText(getBaseContext(), "Cannot rate a review\n" +
                                        "Reason: too many reports against content created by this user",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });

            mainViewHolder.reportReview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ReviewItem reviewItem = reviews.get(position);
                    if (!signedIn){
                        Toast.makeText(getBaseContext(), "Cannot report a review\n" +
                                        "Reason: User is not signed in",
                                Toast.LENGTH_SHORT).show();
                    }else if (reportCount > 4) {
                        Toast.makeText(getBaseContext(), "Cannot report a review\n" +
                                        "Reason: too many reports against content created by user",
                                Toast.LENGTH_SHORT).show();
                    } else new goToReportTask(user_email, reviewItem.getReviewID(),"Review").execute();
                }
            });
            return convertView;
        }
    }

    private class ViewHolder {
        TextView comments;
        TextView thumbs;
        RatingBar ratings;
        ImageButton thumbsUp;
        ImageButton thumbsDown;
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

    private class getReviewTask extends AsyncTask<Void, Void, Void> {
        String id;
        Review review;
        int amount;

        public getReviewTask(String id, int amount) {
            this.id = id;
            this.amount = amount;
        }

        protected Void doInBackground(Void... inputs) {
            this.review = Review.getReview(id);
            return null;
        }

        protected void onPostExecute(Void result) {
            if(amount == 1) review.setThumbsUp(review.getThumbsUp() + 1);
            else review.setThumbsDown(review.getThumbsDown() + 1);
            Log.d(TAG, review.getID() + ", " + review.getThumbsUp() + ", " + review.getThumbsDown());
            new updateReviewThumbsTask(review).execute();
        }
    }

    private class updateReviewThumbsTask extends AsyncTask<Void, Void, Void> {
        Review newThumbsReview;

        public updateReviewThumbsTask(Review review) {
            this.newThumbsReview = review;
        }

        protected Void doInBackground(Void... inputs) {
            newThumbsReview.createReview();
            return null;
        }

        protected void onPostExecute(Void result) {
            Log.d(TAG, "Updated");
        }
    }

    public void closeKeyboard(View view){
        InputMethodManager manager =
                (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (view != null && view instanceof EditText) {
                Rect r = new Rect();
                view.getGlobalVisibleRect(r);
                int rawX = (int)ev.getRawX();
                int rawY = (int)ev.getRawY();
                if (!r.contains(rawX, rawY)) {
                    view.clearFocus();
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}


