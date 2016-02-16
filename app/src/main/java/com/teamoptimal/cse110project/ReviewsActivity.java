package com.teamoptimal.cse110project;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.RatingBar.*;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.teamoptimal.cse110project.data.*;
import android.app.ListActivity;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import android.widget.ListView;
import java.util.*;
import android.widget.ArrayAdapter;
import android.view.KeyEvent;


public class ReviewsActivity extends ListActivity{
    User users;
    RatingBar getRatingBar;
    TextView countText;
    Review newReview;
    int currentID;
    Restroom currentRestroom;
    AmazonClientManager client = new AmazonClientManager(this);
    DynamoDBMapper mapper = new DynamoDBMapper(client.ddb());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        EditText comments = (EditText) findViewById(R.id.comments);

        newReview = new Review();
        currentRestroom = new Restroom();
        newReview.setID(currentRestroom.getID());
        newReview.setUserEmail(currentRestroom.getUser());
        this.addListenerToRatingBar();

        comments.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if (i == EditorInfo.IME_ACTION_DONE) {
                    String inputMessage = textView.getText().toString();
                    newReview.setMessage(inputMessage);
                }
                return handled;
            }
        });

        Button button = (Button) findViewById(R.id.buttonComment);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapper.save(newReview);

            }
        });

        ListView reviewList = (ListView)findViewById(R.id.listView2);
        reviewList.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, generateData()));
    }

    public void addListenerToRatingBar(){
        getRatingBar = (RatingBar) findViewById(R.id.getRating);
        getRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar rateBar, float rating, boolean fromUser) {
                newReview.setRating((double) rating);
            }
        });
    }

    public ArrayList<CombinedReview> generateData(){
        List<Review> items = new ArrayList<Review>();
        ArrayList<CombinedReview> itemComments = new ArrayList<CombinedReview>();
        Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
        eav.put(":val1", new AttributeValue().withS(currentRestroom.getID()));
        DynamoDBQueryExpression<Review> queryExpression = new DynamoDBQueryExpression<Review>()
                //.withString(currentRestroom.getID())
                .withExpressionAttributeValues(eav);
        items = mapper.query(Review.class, queryExpression);
        for(int i = 0; i < items.size();i++)
        {
            itemComments.add(new CombinedReview(items.get(i).getUserEmail(), items.get(i).getMessage()));
        }

        return itemComments;

    }

}