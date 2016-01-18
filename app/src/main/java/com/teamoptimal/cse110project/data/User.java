package com.teamoptimal.cse110project.data;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

/**
 * Represents a User
 * Created by Fabian Choi on 1/18/2016.
 */
@DynamoDBTable(tableName = "Y4R_Users")
public class User {
    private int id;
    private String username;
    private String gender;

    @DynamoDBHashKey(attributeName = "ID")
    public int getID() {
        return id;
    }
    public void setID(int id) {
        this.id = id;
    }

    @DynamoDBHashKey(attributeName = "Username")
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    @DynamoDBHashKey(attributeName = "Gender")
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
