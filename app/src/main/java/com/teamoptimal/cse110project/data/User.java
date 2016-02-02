package com.teamoptimal.cse110project.data;

import android.os.AsyncTask;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.teamoptimal.cse110project.SignInActivity;

import java.util.SimpleTimeZone;
import java.util.logging.StreamHandler;

/**
 * Represents a User
 * Created by Fabian Choi on 1/18/2016.
 */
@DynamoDBTable(tableName = "Y4R_Users")
public class User {
    private String email;
    private String provider; // google, facebook, twitter
    private String providerID;
    private String username;
    private String gender; // The preferred gender for restrooms
    private int flags;

    @DynamoDBHashKey(attributeName = "Email")
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @DynamoDBAttribute(attributeName = "Provider")
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    @DynamoDBAttribute(attributeName = "ProviderID")
    public String getProviderID() { return providerID; }
    public void setProviderID(String providerID) { this.providerID = providerID; }

    @DynamoDBAttribute(attributeName = "Username")
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    @DynamoDBAttribute(attributeName = "Gender")
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    @DynamoDBAttribute(attributeName = "Flags")
    public int getFlags() { return flags; }
    public void setFlags(int flags) { this.flags = flags; }

    // Create the user
    public void create() {
        AmazonDynamoDBClient ddb = SignInActivity.clientManager.ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        // Create or update the user
        mapper.save(this);
    }
}
