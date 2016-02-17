package com.teamoptimal.cse110project.data;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

/**
 * Represents a Review made by a User on a Restroom
 * Created by Fabian Choi on 2/1/2016.
 */
@DynamoDBTable(tableName = "Y4R_Reviews")
public class Review {
    private String ID;
    private String userEmail;
    private String message;
    private String restroomID;
    private double rating;
    private int thumbsUp;
    private int thumbsDown;
    private int flags;
    private int size;

    @DynamoDBHashKey(attributeName = "ID")
    public String getID() { return ID; }
    public void setID(String ID) { this.ID = ID; }

    @DynamoDBAttribute(attributeName = "UserEmail")
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    @DynamoDBAttribute(attributeName = "Message")
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    @DynamoDBAttribute(attributeName = "Rating")
    public double getRating() { return rating; }
    public void setRating(String val) { rating = Double.parseDouble(val); }

    @DynamoDBAttribute(attributeName = "ThumbsUp")
    public int getThumbsUp() { return thumbsUp; }
    public void setThumbsUp(int thumbsUp) { this.thumbsUp = thumbsUp; }

    @DynamoDBAttribute(attributeName = "ThumbsDown")
    public int getThumbsDown() { return thumbsDown; }
    public void setThumbsDown(int thumbsDown) { this.thumbsDown = thumbsDown; }

    @DynamoDBAttribute(attributeName = "Flags")
    public int getFlags() { return flags; }
    public void setFlags(int flags) { this.flags = flags; }

    @DynamoDBAttribute(attributeName = "Restroom")
    public String getRestroomId() { return restroomID; }
    public void setRestroomId(String restroomId) { this.restroomID = restroomId; }
}
