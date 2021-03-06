package com.teamoptimal.cse110project.data;

import android.util.Log;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAutoGeneratedKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIgnore;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.teamoptimal.cse110project.MainActivity;
import com.teamoptimal.cse110project.ReportActivity;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@DynamoDBTable (tableName = "Y4R_Reports")
public class Report {
    private String ID;
    private String Object;
    private String objectType;
    private String Target;
    private String Description;
    private String Reporter;
    private static String TAG = "Report";

    public Report(){}

    @DynamoDBHashKey (attributeName = "ID")
    @DynamoDBAutoGeneratedKey
    public String getID(){return ID;}
    public void setID(String newID){ID = newID;}

    @DynamoDBAttribute(attributeName = "ObjectType")
    public String getObjectType(){return objectType;}
    public void setObjectType(String type){objectType = type;}

    @DynamoDBAttribute (attributeName = "ObjectID")
    public String getObjectID(){return Object;}
    public void setObjectID(String obj){Object = obj;}

    @DynamoDBAttribute (attributeName = "Target")
    public String getTarget(){return Target;}
    public void setTarget(String newT){Target = newT;}

    @DynamoDBAttribute (attributeName = "Decription")
    public String getDescription(){return Description;}
    public void setDescription(String desc){Description = desc;}

    @DynamoDBAttribute (attributeName = "Reporter")
    public String getReporter(){return Reporter;}
    public void setReporter(String report){Reporter = report;}

    @DynamoDBIgnore
    public String getObject(){return Object;}
    public void setObject( String obj){Object = obj;}


    public void create() {
        AmazonDynamoDBClient ddb = ReportActivity.clientManager.ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        mapper.save(this);
    }

    @DynamoDBIgnore
    public static boolean hasReported(String user_email, String objID , String type){
        Log.d("Reports", "Determining previous report existance");
        AmazonDynamoDBClient ddb = MainActivity.clientManager.ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);


        Condition userEmail = new Condition()
                .withComparisonOperator(ComparisonOperator.EQ)
                .withAttributeValueList(new AttributeValue().withS(user_email));
        Condition objectID = new Condition()
                .withComparisonOperator(ComparisonOperator.EQ)
                .withAttributeValueList(new AttributeValue().withS(objID));
        Condition objectType = new Condition()
                .withComparisonOperator(ComparisonOperator.EQ)
                .withAttributeValueList(new AttributeValue().withS(type));

        Map<String, Condition> conditions = new HashMap<String, Condition>();
        conditions.put("Reporter", userEmail);
        conditions.put("ObjectID", objectID);
        conditions.put("ObjectType", objectType);


        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        scanExpression.setScanFilter(conditions);
        List<Report> scanResult;

        try{
            scanResult = mapper.scan(Report.class, scanExpression);
        }catch(AmazonClientException s){
            Log.d(TAG, "AmazonClientException thrown, returning true to hasReported");
            return true;
        }
        Log.d("Report", "scanResult, " + scanResult.size());

        if(scanResult.size() > 0) return true;
        return false;
    }

}
