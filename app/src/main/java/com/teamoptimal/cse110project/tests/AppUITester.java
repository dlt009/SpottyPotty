package com.teamoptimal.cse110project.tests;

import android.app.LauncherActivity;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.LargeTest;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.teamoptimal.cse110project.AmazonClientManager;
import com.teamoptimal.cse110project.MainActivity;
import com.teamoptimal.cse110project.R;
import com.teamoptimal.cse110project.data.Restroom;
import com.teamoptimal.cse110project.data.RestroomItem;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.support.test.espresso.Espresso.closeSoftKeyboard;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;

@RunWith(AndroidJUnit4.class)
@LargeTest

//////////////////////////// IMPORTANT !!!!!!!!!!!!!!!!!!//////////////////////////////
/* Before running any of these tests, you must manually sign in with facebook,google,twitter into
 * the app or tests will not be accurate. Also, must be in a location where there are registered
 * bathrooms nearby.
 */
public class AppUITester extends InstrumentationTestCase{
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule(MainActivity.class);

    @Test
    public void TestCreateRestroomUI() throws InterruptedException {
        while(!mActivityRule.getActivity().isDoneLoadingList) {
            Thread.sleep(1000);
        }
        onView(withId(R.id.fab)).perform(click());

        AmazonDynamoDBClient ddb = mActivityRule.getActivity().clientManager.ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        onView(withId(R.id.editText)).perform(typeText("Not Real!!!!!"));
        onView(withId(R.id.editText2)).perform(typeText("7"));
        closeSoftKeyboard();
        onView(withId(R.id.ratingBar)).perform(click());
        onView(withId(R.id.button)).perform(click());

        while (!mActivityRule.getActivity().isDoneCreatingRestroom) {
            Thread.sleep(1000);
        }

        Condition testDescrp = new Condition()
                .withComparisonOperator(ComparisonOperator.CONTAINS)
                .withAttributeValueList(new AttributeValue().withS("Not Real!!!!!"));
        Condition testFloor = new Condition()
                .withComparisonOperator(ComparisonOperator.CONTAINS)
                .withAttributeValueList(new AttributeValue().withS("7"));

        Map<String, Condition> conditions = new HashMap<String, Condition>();
        conditions.put("Description", testDescrp);
        conditions.put("Floor", testFloor);

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        scanExpression.setScanFilter(conditions);

        List<Restroom> scanResult = mapper.scan(Restroom.class, scanExpression);
        assertEquals(scanResult.size(),1);
        for (Restroom r : scanResult)
            mapper.delete(r);
    }

    @Test
    public void testDrawerRestroomList() throws InterruptedException {
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());

        while(!mActivityRule.getActivity().isDoneLoadingList) {
            Thread.sleep(1000);
        }

        ListView list = (ListView)mActivityRule.getActivity().findViewById(R.id.restrooms_list);
        int uICount = list.getAdapter().getCount();

        assertEquals(uICount, mActivityRule.getActivity().getCurrRestroom().size());
    }

    @Test
    public void testDetailsActivityUI() throws InterruptedException {
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());

        while(!mActivityRule.getActivity().isDoneLoadingList) {
            Thread.sleep(1000);
        }

        ListView list = (ListView)mActivityRule.getActivity().findViewById(R.id.restrooms_list);

        onView(allOf(withText("Details"), hasSibling(withText(((RestroomItem) list.getItemAtPosition(0)).getTitle()))))
                .perform(click());

        List<Restroom> currRestrooms = mActivityRule.getActivity().getCurrRestroom();
        Restroom toCheck = currRestrooms.get(0);

        onView(withId(R.id.textView2)).check(matches(withText(toCheck.getDescription())));
    }
}
