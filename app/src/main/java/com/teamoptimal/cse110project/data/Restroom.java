package com.teamoptimal.cse110project.data;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

/**
 * Represents a Restroom created by a User
 * Created by Fabian Choi on 2/1/2016.
 */
@DynamoDBTable(tableName = "Y4R_Restrooms")
public class Restroom {
    private int id;
    private String location;
    private String userEmail;
    private double rating;
    private int flags; // Number of flags

    @DynamoDBHashKey(attributeName = "ID")
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @DynamoDBAttribute(attributeName = "UserEmail")
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    @DynamoDBAttribute(attributeName = "Location")
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    @DynamoDBAttribute(attributeName = "Rating")
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    @DynamoDBAttribute(attributeName = "Flags")
    public int getFlags() { return flags; }
    public void setFlags(int flags) { this.flags = flags; }
}
