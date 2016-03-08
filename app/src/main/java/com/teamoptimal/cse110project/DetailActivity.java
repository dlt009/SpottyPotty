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
    private ArrayList<ReviewItem> itemComments;
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
        String name = intentExtra.getStringExtra("name");
        String distance = intentExtra.getStringExtra("distance");
        Float ratings = intentExtra.getFloatExtra("ratings", 0.0f);
        EditText comments = (EditText) findViewById(R.id.comments);

        itemComments = new ArrayList<>();
        /* Grabs the Restroom ID that is clicked from MainActivity */
        currentID = intentExtra.getStringExtra("restroomID");

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

        /* Sets the TextView of the name and distance of the current bathroom displayed */
        TextView nameView = (TextView) findViewById(R.id.textView2);
        TextView distanceView = (TextView) findViewById(R.id.textView3);
        RatingBar  ratingBar2 = (RatingBar) findViewById(R.id.ratingBar2);

        nameView.setText(name);
        distanceView.setText(distance);
        ratingBar2.setRating(ratings);

        /* Sets the ListView of comments/ratings */
        reviewList = (ListView) findViewById(android.R.id.list);
        adapter = new MyAdapter(this, R.layout.review_item, itemComments);
        new GetReviewsTask(review, this).execute();
        //ListView reviewList = getListView();

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

    }

    public ArrayList<ReviewItem> generateData(){
        if(itemComments.size() != 0) {
            itemComments.clear();
        }
        ArrayList<Review> reviews = new ArrayList(review.getReviews(currentID));
         for(Review review : reviews) {
            itemComments.add(new ReviewItem(review.getMessage(), review.getRating()));
         }
        adapter.notifyDataSetChanged();

        return itemComments;

    }

    private class MyAdapter extends ArrayAdapter<ReviewItem> {
        private final Context context;
        private final ArrayList<ReviewItem> reviews;
        private int layout;

        public MyAdapter(Context context, int resource, ArrayList<ReviewItem> reviews) {
            super(context, resource, reviews);
            this.context = context;
            this.reviews = reviews;
            layout = resource;
        }

        public View getView(int position, View convertView, ViewGroup parent){
            final ViewHolder mainViewHolder;

            if(convertView == null) {
                LayoutInflater inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflator.inflate(layout, parent, false);

                View rowView = inflator.inflate(layout, parent, false);

            TextView commentView = (TextView) rowView.findViewById(R.id.comments);
            RatingBar ratingView = (RatingBar) rowView.findViewById(R.id.setRating);

            commentView.setText(reviews.get(position).getComments());
            ratingView.setRating(reviews.get(position).getRating());

                /*ViewHolder viewHolder = new ViewHolder();
                viewHolder.comments = (TextView) convertView.findViewById(R.id.comments);
                viewHolder.ratings = (RatingBar) convertView.findViewById(R.id.setRating);
                viewHolder.report = (Button) convertView.findViewById(R.id.button_report);*/

               // convertView.setTag(viewHolder);


            }
            mainViewHolder = (ViewHolder) convertView.getTag();
            final ReviewItem rItem = this.reviews.get(position);

            mainViewHolder.comments.setText(rItem.getComments());
            mainViewHolder.ratings.setRating(rItem.getRating());
            return convertView;
        }
    }

    private class ViewHolder {
        TextView comments;
        RatingBar ratings;
        Button report;
    }

    private class GetReviewsTask extends AsyncTask<Void, Void, Void> {
        private Review review;
        private Context context;
        //ArrayList<ReviewItem> reviewItems;

        public GetReviewsTask(Review review, Context context) {
            this.review = review;
            this.context = context;
        }

        // To do in the background
        protected Void doInBackground(Void... inputs) {
            //reviewItems = review.getReviews(currentID);
            itemComments = generateData();
            return null;
        }

        // To do after doInBackground is executed
        // We can use UI elements here
        protected void onPostExecute(Void result) {
            adapter = new MyAdapter(context, R.layout.review_item, itemComments);
            adapter.notifyDataSetChanged();
            reviewList = (ListView)findViewById(android.R.id.list);
            reviewList.setAdapter(adapter);
        }
    }

    private class CreateReviewTask extends AsyncTask<Void, Void, Void> {
        private Review review;
        private Context context;
        private ArrayList<ReviewItem> list;

        public CreateReviewTask(Review review, Context context) {
            this.review = review;
            this.context = context;
        }

        // To do in the background
        protected Void doInBackground(Void... inputs) {
            review.createReview(); // Use the method from the User class to create it
            //list = review.getReviews(currentID);
            itemComments = generateData();
            return null;
        }

        // To do after doInBackground is executed
        // We can use UI elements here
        protected void onPostExecute(Void result) {
            adapter = new MyAdapter(context, R.layout.review_item, itemComments);
            adapter.notifyDataSetChanged();
            reviewList = (ListView)findViewById(android.R.id.list);
            reviewList.setAdapter(adapter);
        }
    }
}

