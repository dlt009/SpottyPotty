package com.teamoptimal.cse110project;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.teamoptimal.cse110project.data.ReviewItem;
import com.teamoptimal.cse110project.data.Review;

import java.util.ArrayList;


public class DetailActivity extends ListActivity {
    private ListView reviewList;
    private RatingBar getRatingBar;
    private Review review;
    private String currentID;
    private Intent intentExtra;
    private ArrayList<ReviewItem> itemComments = new ArrayList<ReviewItem>();
    private Context context = this;

    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

       // mapper = new DynamoDBMapper(client.ddb());
       // mapper.load(User.class, )

        /* Grabs the passed intent from MainActivity */
        intentExtra = getIntent();
        String name = /*intentExtra.getStringExtra("name");*/ "test";
        String distance = /*intentExtra.getStringExtra("distance");*/ "test distance";

        EditText comments = (EditText) findViewById(R.id.comments);

        /* Grabs the Restroom ID that is clicked from MainActivity */
        currentID = /*MainActivity.clickedRestroomID*/ "testing";

        /* Initialize new Review with data from MainActivity */
        review = new Review();
        review.setRestroomID(currentID);
        review.setUserEmail(MainActivity.user.getEmail());


         /* Set rating bar */
        RatingBar ratingBar = (RatingBar) findViewById(R.id.getRating);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                review.setRating(rating);
            }
        });


        //this.addListenerToRatingBar();

        /*comments.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if (i == EditorInfo.IME_ACTION_DONE) {
                    String inputMessage = textView.getText().toString();
                    review.setMessage(inputMessage);
                    handled = true;
                }
                return handled;
            }
        });*/

        /* Sets the button */
        Button button = (Button) findViewById(R.id.buttonComment);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                EditText comments = (EditText)findViewById(R.id.comments);
                review.setMessage(comments.getText().toString());

                if(review.isInitialized()) {
                    new CreateReviewTask(review, context).execute();
                    Toast.makeText(getBaseContext(), "Review has been created", Toast.LENGTH_SHORT).show();

                    finish();
                }
                else {
                    Toast.makeText(getBaseContext(), "Unsuccessful", Toast.LENGTH_SHORT).show();
                }
            }
        });

        /* Sets the TextView of the name and distance of the current bathroom displayed */
        TextView nameView = (TextView) findViewById(R.id.textView2);
        TextView distanceView = (TextView) findViewById(R.id.textView3);

        nameView.setText(name);
        distanceView.setText(distance);

        /* Sets the ListView of comments/ratings */
        new GetReviewsTask(review, this);
        //ListView reviewList = getListView();

    }

   /* public void addListenerToRatingBar(){
        getRatingBar = (RatingBar) findViewById(R.id.getRating);
        getRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar rateBar, float rating, boolean fromUser) {
                review.setRating(rating);
            }
        });
    }*/

    public ArrayList<ReviewItem> generateData(){
        if(itemComments.size() != 0) {
            itemComments.clear();
        }

        /*List<Review> items = new ArrayList<Review>();
        //ArrayList<ReviewItem> itemComments = new ArrayList<ReviewItem>();
        Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
        eav.put(":val1", new AttributeValue().withS(currentID));
        DynamoDBQueryExpression<Review> queryExpression = new DynamoDBQueryExpression<Review>()
                .withHashKeyValues(review)
                .withFilterExpression(currentID);
                //.withConsistentRead(false);
                //.setQueryFilter(eav);
                //.withString(restroom.getID())
                //.withExpressionAttributeValues(eav);
        items = mapper.query(Review.class, queryExpression);
        for(int i = 0; i < items.size();i++)
        {
            itemComments.add(new ReviewItem(items.get(i).getMessage(), items.get(i).getRating()));
        }*/

        itemComments = Review.getReviews(currentID);
        adapter.notifyDataSetChanged();

        return itemComments;

    }

    private class MyAdapter extends ArrayAdapter<ReviewItem> {
        private final Context context;
        private final ArrayList<ReviewItem> reviews;

        public MyAdapter(Context context, ArrayList<ReviewItem> reviews) {
            super(context, R.layout.review_item, reviews);
            this.context = context;
            this.reviews = reviews;
        }

        public View getView(int position, View convertView, ViewGroup parent){
            LayoutInflater inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView = inflator.inflate(R.layout.review_item, parent, false);

            TextView commentView = (TextView) rowView.findViewById(R.id.comments);
            RatingBar ratingView = (RatingBar) rowView.findViewById(R.id.setRating);

            commentView.setText(reviews.get(position).getComments());
            ratingView.setRating(reviews.get(position).getRating());

            return rowView;
        }
    }


    private class GetReviewsTask extends AsyncTask<Void, Void, Void> {
        private Review review;
        private Context context;
        ArrayList<ReviewItem> reviewItems;

        public GetReviewsTask(Review review, Context context) {
            this.review = review;
            this.context = context;
        }

        // To do in the background
        protected Void doInBackground(Void... inputs) {
            reviewItems = review.getReviews(currentID);
            return null;
        }

        // To do after doInBackground is executed
        // We can use UI elements here
        protected void onPostExecute(Void result) {
            adapter = new MyAdapter(context, reviewItems);
            adapter.notifyDataSetChanged();
            reviewList = (ListView)findViewById(android.R.id.list);
            reviewList.setAdapter(adapter);
        }
    }

    private class CreateReviewTask extends AsyncTask<Void, Void, Void> {
        private Review review;
        private Context context;

        public CreateReviewTask(Review review, Context context) {
            this.review = review;
            this.context = context;
        }

        // To do in the background
        protected Void doInBackground(Void... inputs) {
            review.createReview(); // Use the method from the User class to create it
            return null;
        }

        // To do after doInBackground is executed
        // We can use UI elements here
        protected void onPostExecute(Void result) {
            adapter = new MyAdapter(context, generateData());
            reviewList.setAdapter(adapter);
        }
    }
}

