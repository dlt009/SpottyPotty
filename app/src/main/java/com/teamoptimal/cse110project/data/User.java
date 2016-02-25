package com.teamoptimal.cse110project.data;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIgnore;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.teamoptimal.cse110project.ReportActivity;
import com.teamoptimal.cse110project.SignInActivity;

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
    private int reports;

    public User(){}

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

    @DynamoDBAttribute(attributeName = "Times_Reported")
    public int getReportCount(){return reports;}
    public void setReportCount(int count){reports = count;}

    @DynamoDBIgnore
    public void addReport(){reports++;}

    // Create the user
    public void create() {
        AmazonDynamoDBClient ddb = SignInActivity.clientManager.ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        // Create or update the user
        mapper.save(this);
    }

    @DynamoDBIgnore
    public void reportRestroom(Restroom rest, String desc){
        Report report = new Report();
        rest.addReport();
        report.setObject("Restroom", rest.getID());
        report.setReporter(this.getEmail());
        report.setTarget(rest.getUser());
        report.setDescription(desc);

        AmazonDynamoDBClient ddb = ReportActivity.clientManager.ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);
        mapper.save(rest);

        /*User target = new User();
        mapper.load(target, rest.getUser());
        target.addReport();
        mapper.save(target);*/

        report.create();
    }

    @DynamoDBIgnore
    public void reportReview(Review review, String desc){
        Report report = new Report();
        review.addReport();
        report.setObject("Review", review.getID());
        report.setReporter(this.getEmail());
        report.setTarget(review.getUserEmail());
        report.setDescription(desc);

        AmazonDynamoDBClient ddb = ReportActivity.clientManager.ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);
        mapper.save(review);

        /*User target = new User();
        mapper.load(target, review.getUserEmail());
        target.addReport();
        mapper.save(target);*/

        report.create();
    }

}
