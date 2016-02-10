package com.teamoptimal.cse110project;

import android.Manifest;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.TextView;
import android.view.KeyEvent;
import android.widget.RatingBar;
import com.teamoptimal.cse110project.data.*;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;
import android.widget.Button;

public class CreateRestroomActivity extends ListActivity {
    Restroom restroom;
    User user;
    String[] tags = {
            "Public",
            "Private",
            "Pay-to-use",
            "Changing Stations",
            "Restaurant",
            "Store",
            "Unisex",
            "Female-only",
            "Male-only",
            "Clean",
            "Somewhat-Clean",
            "Somewhat-Dirty",
            "Dirty"
    };
    RatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_restroom);

        AmazonClientManager client = new AmazonClientManager(this);

        final DynamoDBMapper mapper = new DynamoDBMapper(client.ddb());

        restroom = new Restroom();

        restroom.setUser("NewUser@test.com");

        ListView tagList = getListView();
        tagList.setChoiceMode(tagList.CHOICE_MODE_MULTIPLE);
        tagList.setTextFilterEnabled(true);
        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_checked,
                tags));

        EditText name = (EditText) findViewById(R.id.editText);
        EditText floor = (EditText) findViewById(R.id.editText2);
        EditText description = (EditText) findViewById(R.id.editText3);

        this.addListenerOnRatingBar();

        name.setOnEditorActionListener(new TextView.OnEditorActionListener(){
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent){
                boolean handled = false;
                if(i== EditorInfo.IME_ACTION_NEXT){
                    String inputName = textView.getText().toString();
                    restroom.setName(inputName);
                    handled=true;
                }
                return handled;
            }
        });

        floor.setOnEditorActionListener(new TextView.OnEditorActionListener(){
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent){
                boolean handled = false;
                if(i== EditorInfo.IME_ACTION_NEXT){
                    String inputFloor = textView.getText().toString();
                    restroom.setFloor(inputFloor);
                    handled=true;
                }
                return handled;
            }
        });

        description.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if (i == EditorInfo.IME_ACTION_NEXT) {
                    String inputDesc = textView.getText().toString();
                    restroom.setDesc(inputDesc);
                    handled=true;
                }
                return handled;
            }
        });

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (restroom.isInitialized()) {
                    mapper.save(restroom);
                    Toast.makeText(getBaseContext(),
                            restroom.getName()+" has been created under the user: "+
                                    restroom.getUser()
                            ,Toast.LENGTH_LONG).show();
                }
                else{Toast.makeText(getBaseContext(),"Unsuccessful",Toast.LENGTH_SHORT).show();}
            }
        });
    }

    public void onListItemClick(ListView parent, View v, int position, long id ){
        CheckedTextView item = (CheckedTextView) v;
        restroom.setTag(position, item.isChecked());
    }

    public void addListenerOnRatingBar(){
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                restroom.setRating((double) rating);
            }
        });
    }
}
