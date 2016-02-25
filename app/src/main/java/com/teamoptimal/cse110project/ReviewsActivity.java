package com.teamoptimal.cse110project;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.teamoptimal.cse110project.data.CombinedReview;
import com.teamoptimal.cse110project.data.Restroom;
import com.teamoptimal.cse110project.data.RestroomItem;
import com.teamoptimal.cse110project.data.Review;
import com.teamoptimal.cse110project.data.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ReviewsActivity extends ListActivity {
    private ListView reviewList;
    private RatingBar getRatingBar;
    public static Review review;
    private String currentID;
    private AmazonClientManager client = new AmazonClientManager(this);
    private DynamoDBMapper mapper = new DynamoDBMapper(client.ddb());
    public static AmazonClientManager clientManager = null;
    //private User user = SignInActivity.user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bathroom_page);

        clientManager = new AmazonClientManager(this);

        EditText comments = (EditText) findViewById(R.id.comments);

        currentID = MainActivity.clickedRestroomID;

        review = new Review();
        review.setRestroomID(currentID);
        review.setUserEmail(MainActivity.user.getEmail());
        this.addListenerToRatingBar();

        comments.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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
        });

        Button button = (Button) findViewById(R.id.buttonComment);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                review.create();
                EditText comments = (EditText)findViewById(R.id.comments);
                review.setMessage(comments.getText().toString());
                finish();

            }
        });

        MyAdapter adapter = new MyAdapter(this, generateData());
        reviewList = (ListView)findViewById(R.id.listView);
        reviewList.setAdapter(adapter);
    }

    public void addListenerToRatingBar(){
        getRatingBar = (RatingBar) findViewById(R.id.getRating);
        getRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar rateBar, float rating, boolean fromUser) {
                review.setRating(rating);
            }
        });
    }

    public ArrayList<CombinedReview> generateData(){
        List<Review> items = new ArrayList<Review>();
        ArrayList<CombinedReview> itemComments = new ArrayList<CombinedReview>();
        Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
        eav.put(":val1", new AttributeValue().withS(currentID));
        DynamoDBQueryExpression<Review> queryExpression = new DynamoDBQueryExpression<Review>()
                //.withString(restroom.getID())
                .withExpressionAttributeValues(eav);
        items = mapper.query(Review.class, queryExpression);
        for(int i = 0; i < items.size();i++)
        {
            itemComments.add(new CombinedReview(items.get(i).getMessage(), items.get(i).getRating()));
        }

        return itemComments;

    }

    private class MyAdapter extends ArrayAdapter<CombinedReview> {
        private final Context context;
        private final ArrayList<CombinedReview> reviews;

        public MyAdapter(Context context, ArrayList<CombinedReview> reviews) {
            super(context, R.layout.row, reviews);
            this.context = context;
            this.reviews = reviews;
        }

        public View getView(int position, View convertView, ViewGroup parent){
            LayoutInflater inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView = inflator.inflate(R.layout.row, parent, false);

            TextView commentView = (TextView) rowView.findViewById(R.id.comments);
            RatingBar ratingView = (RatingBar) rowView.findViewById(R.id.setRating);

            commentView.setText(reviews.get(position).getComments());
            ratingView.setRating(reviews.get(position).getRating());

            return rowView;
        }
    }
}

