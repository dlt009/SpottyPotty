package com.teamoptimal.cse110project;

import android.content.Context;
import android.util.Log;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import java.util.Map;

/**
 * Represents an Amazon DynamoDB Database Client
 * Created by Fabian Choi on 1/31/2016.
 */
public class AmazonClientManager {

    private static final String LOG_TAG = "AmazonClientManager";
    private static final String ACCOUNT_ID = "379023374138";
    private static final String IDENTITY_POOL_ID = "us-east-1:f577c8d8-69d1-4706-855a-68dd834c06a1";
    private static final String UNAUTH_ROLE_ARN = "arn:aws:iam::379023374138:role/Cognito_Yelp4RestroomsUnauth_Role";

    private AmazonDynamoDBClient ddb = null;
    private Context context;

    public AmazonClientManager(Context context) {
        this.context = context;
    }

    public AmazonDynamoDBClient ddb() {
        validateCredentials();
        return ddb;
    }

    public boolean hasCredentials() {
        return (!(ACCOUNT_ID.equalsIgnoreCase("379023374138")
                || IDENTITY_POOL_ID.equalsIgnoreCase("us-east-1:f577c8d8-69d1-4706-855a-68dd834c06a1")
                || UNAUTH_ROLE_ARN.equalsIgnoreCase("arn:aws:iam::379023374138:role/Cognito_Yelp4RestroomsUnauth_Role")));
    }

    public void validateCredentials() {
        if (ddb == null) {
            initClients();
        }
    }

    public void validateCredentials(Map<String, String> logins) {
        if (ddb == null) {
            initClients(logins);
        }
    }

    private void initClients() {
        CognitoCachingCredentialsProvider credentials = new CognitoCachingCredentialsProvider(
                context,
                "us-east-1:f577c8d8-69d1-4706-855a-68dd834c06a1", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );

        ddb = new AmazonDynamoDBClient(credentials);
    }

    private void initClients(Map<String, String> logins) {
        CognitoCachingCredentialsProvider credentials = new CognitoCachingCredentialsProvider(
                context,
                "us-east-1:f577c8d8-69d1-4706-855a-68dd834c06a1", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );
        credentials.setLogins(logins);
        ddb = new AmazonDynamoDBClient(credentials);
    }

    public boolean wipeCredentialsOnAuthError(AmazonServiceException ex) {
        Log.e(LOG_TAG, "Error, wipeCredentialsOnAuthError called" + ex);
        if (
            // STS
            // http://docs.amazonwebservices.com/STS/latest/APIReference/CommonErrors.html
                ex.getErrorCode().equals("IncompleteSignature")
                        || ex.getErrorCode().equals("InternalFailure")
                        || ex.getErrorCode().equals("InvalidClientTokenId")
                        || ex.getErrorCode().equals("OptInRequired")
                        || ex.getErrorCode().equals("RequestExpired")
                        || ex.getErrorCode().equals("ServiceUnavailable")

                        // DynamoDB
                        // http://docs.amazonwebservices.com/amazondynamodb/latest/developerguide/ErrorHandling.html#APIErrorTypes
                        || ex.getErrorCode().equals("AccessDeniedException")
                        || ex.getErrorCode().equals("IncompleteSignatureException")
                        || ex.getErrorCode().equals(
                        "MissingAuthenticationTokenException")
                        || ex.getErrorCode().equals("ValidationException")
                        || ex.getErrorCode().equals("InternalFailure")
                        || ex.getErrorCode().equals("InternalServerError")) {

            return true;
        }

        return false;
    }
}
