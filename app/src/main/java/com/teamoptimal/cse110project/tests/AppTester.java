package com.teamoptimal.cse110project.tests;

import android.test.InstrumentationTestCase;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.teamoptimal.cse110project.AmazonClientManager;
import com.teamoptimal.cse110project.data.Report;
import com.teamoptimal.cse110project.data.Restroom;
import com.teamoptimal.cse110project.data.Review;
import com.teamoptimal.cse110project.data.User;

import org.junit.Test;

public class AppTester extends InstrumentationTestCase{

    @Test
    public void testCreateRestroom() {
        Restroom dummy = new Restroom();
        dummy.setUser("testerEmailCS110@ucsd.cu");
        dummy.setDescription("This is a test bathroom. It doesn't actually exist!");
        dummy.setFloor("1");
        dummy.setLocation(90, 0);
        dummy.setID("TestIDCSE110TestRr");

        AmazonClientManager clientManager = new AmazonClientManager(getInstrumentation().getContext());
        clientManager.validateCredentials();

        AmazonDynamoDBClient ddb = clientManager.ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);
        mapper.save(dummy);

        Restroom returned = mapper.load(Restroom.class, "TestIDCSE110TestRr");

        assertTrue(returned!=null);
        assertEquals(returned.getID(), dummy.getID());
        assertEquals(returned.getUser(),dummy.getUser());
        assertEquals(returned.getDescription(),dummy.getDescription());
        assertEquals(returned.getFloor(),dummy.getFloor());
        assertEquals(returned.getLatitude(),dummy.getLatitude());
        assertEquals(returned.getLongitude(),dummy.getLongitude());

        mapper.delete(dummy);
    }

    @Test
    public void testCreateUser() {
        User dummy = new User();
        dummy.setEmail("testerEmailCS110@ucsd.cu");
        dummy.setProvider("NONEWHATSOEVER");
        dummy.setUsername("John Finkle");

        AmazonClientManager clientManager = new AmazonClientManager(getInstrumentation().getContext());
        clientManager.validateCredentials();

        AmazonDynamoDBClient ddb = clientManager.ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);
        mapper.save(dummy);

        User returned = mapper.load(User.class, "testerEmailCS110@ucsd.cu");

        assertFalse(returned == null);
        assertEquals(returned.getEmail(), dummy.getEmail());
        assertEquals(returned.getProvider(),dummy.getProvider());
        assertEquals(returned.getUsername(),dummy.getUsername());

        mapper.delete(dummy);
    }

    @Test
    public void testCreateReview() {
        Review dummy = new Review();
        dummy.setID("TestIDCSE110TestRw");
        dummy.setUserEmail("testerEmailCS110@ucsd.cu");
        dummy.setMessage("This is not a real review!");

        AmazonClientManager clientManager = new AmazonClientManager(getInstrumentation().getContext());
        clientManager.validateCredentials();

        AmazonDynamoDBClient ddb = clientManager.ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);
        mapper.save(dummy);

        Review returned = mapper.load(Review.class, "TestIDCSE110TestRw");

        assertFalse(returned == null);
        assertEquals(returned.getID(), dummy.getID());
        assertEquals(returned.getUserEmail(),dummy.getUserEmail());
        assertEquals(returned.getMessage(),dummy.getMessage());

        mapper.delete(dummy);
    }

    @Test
    public void testReport() {
        Report dummyRestroomRep = new Report();
        dummyRestroomRep.setID("TestIDCSE110TestRpRr");
        dummyRestroomRep.setDescription("This is not a real restroom report!");
        dummyRestroomRep.setObject("Restroom");
        dummyRestroomRep.setReporter("JohnDoe@fake.edu");
        dummyRestroomRep.setTarget("JaneDoe@fake.edu");

        Report dummyReviewRep = new Report();
        dummyReviewRep.setID("TestIDCSE110TestRpRw");
        dummyReviewRep.setDescription("This is not a real review report!");
        dummyReviewRep.setObject("Review");
        dummyReviewRep.setReporter("JohnDoe@fake.edu");
        dummyReviewRep.setTarget("JaneDoe@fake.edu");

        AmazonClientManager clientManager = new AmazonClientManager(getInstrumentation().getContext());
        clientManager.validateCredentials();

        AmazonDynamoDBClient ddb = clientManager.ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);
        mapper.save(dummyRestroomRep);
        mapper.save(dummyReviewRep);

        Report returnedRestRep = mapper.load(Report.class, "TestIDCSE110TestRpRr");
        Report returnedRevRep = mapper.load(Report.class, "TestIDCSE110TestRpRw");

        assertFalse(returnedRestRep == null);
        assertEquals(returnedRestRep.getID(), dummyRestroomRep.getID());
        assertEquals(returnedRestRep.getDescription(),dummyRestroomRep.getDescription());
        assertEquals(returnedRestRep.getReporter(),dummyRestroomRep.getReporter());
        assertEquals(returnedRestRep.getTarget(),dummyRestroomRep.getTarget());

        assertFalse(returnedRevRep == null);
        assertEquals(returnedRevRep.getID(), dummyReviewRep.getID());
        assertEquals(returnedRevRep.getDescription(),dummyReviewRep.getDescription());
        assertEquals(returnedRevRep.getReporter(),dummyReviewRep.getReporter());
        assertEquals(returnedRevRep.getTarget(),dummyReviewRep.getTarget());

        mapper.delete(dummyRestroomRep);
        mapper.delete(dummyReviewRep);
    }
}