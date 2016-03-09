package com.teamoptimal.cse110project.tests;

import android.test.InstrumentationTestCase;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.teamoptimal.cse110project.AmazonClientManager;
import com.teamoptimal.cse110project.data.Restroom;
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
        dummy.setID("TestIDCSE110Test");

        AmazonClientManager clientManager = new AmazonClientManager(getInstrumentation().getContext());
        clientManager.validateCredentials();

        AmazonDynamoDBClient ddb = clientManager.ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);
        mapper.save(dummy);

        Restroom returned = mapper.load(Restroom.class, "TestIDCSE110Test");

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

        assertFalse(returned==null);
        assertEquals(returned.getEmail(), dummy.getEmail());
        assertEquals(returned.getProvider(),dummy.getProvider());
        assertEquals(returned.getUsername(),dummy.getUsername());

        mapper.delete(dummy);
    }
}