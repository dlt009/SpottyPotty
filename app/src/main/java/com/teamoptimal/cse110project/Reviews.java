package com.teamoptimal.cse110project;

import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.RatingBar;
import android.widget.RatingBar.*;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;
import com.teamoptimal.cse110project.data.*;
import android.app.ListActivity;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import android.widget.ListView;
import java.util.*;
import android.widget.ArrayAdapter;
import android.view.KeyEvent;


public class Reviews extends ListActivity implements OnRatingBarChangeListener{

    User users;
    RatingBar getRatingBar;
    TextView countText;
    Review newReview;
    int currentID;
    Restroom currentRestroom

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        AmazonClientManager client = new AmazonClientManager(this);
        final DynamoDBMapper mapper = new DynamoDBMapper(client.ddb());

        EditText comments = (EditText) findViewById(.id.editComments);

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



        ListView reviewList = (ListView)findViewById(R.id.listView2);
        reviewList.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, generateData()));
        mapper.save(newReview);
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

    public ArrayList<Review> generateData(){
        List<Review> items = new List<Review>();
        ArrayList<String> itemComments = new ArrayList<String>();
        Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
        eav.put(":val1", new AttributeValue().withS(currentRestroom.getID()));
        //DynamoDBMapper mapper;
        DynamoDBQueryExpression<Review> queryExpression = new DynamoDBQueryExpresion<Review>()
                .withKeyConditionExpression("restroomID = :val1")
                .withExpressionAttributeValues(eav);
        items = mapper.query(Review.class, queryExpression);
        for(Review review : items)
        {
            itemComments.add(new String(review.getUserEmail(), review.getMessage()));
        }

        return itemComments;

    }

}
