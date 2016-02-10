package com.teamoptimal.cse110project;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import com.teamoptimal.cse110project.data.Review;
import com.teamoptimal.cse110project.data.Restroom;
import android.app.ListActivity;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import java.util.*;


public class Reviews extends ListActivity implements OnRatingBarChangeListener{

    User users = new
    RatingBar getRatingBar;
    TextView countText;
    Review newReview;
    int currentID;
    Restroom currentRestroom;
    DynamoDBMapper mapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);
        R
        EditText comments = (EditText) findViewById(.id.editComments);

        newReview = new Review();
        currentRestroom = new Restroom();
        newReview.setID(currentRestroom.getID());
        newReview.setUser(currentRestroom.getUser)
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
        ArrayList<Item> itemComments = new ArrayList<Item>();
        Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
        eav.put(":val1", new AttributeValue().withS(currentRestroom.getID()));
        //DynamoDBMapper mapper;
        DynamoDBQueryExpression<Review> queryExpression = new DynamoDBQueryExpresion<Review>()
                .withKeyConditionExpression("restroomID = :val1")
                .withExpressionAttributeValues(eav);
        items = mapper.query(Review.class, queryExpression);
        for(Review review : items)
        {
            itemComments.add(new Item(review.getUserEmail, review.getMessage()));
        }

        return itemComments;

    }

}
