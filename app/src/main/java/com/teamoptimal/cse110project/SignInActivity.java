package com.teamoptimal.cse110project;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.CookieSyncManager;

import com.crashlytics.android.Crashlytics;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.teamoptimal.cse110project.data.User;

import java.net.CookieManager;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.facebook.FacebookSdk;

import io.fabric.sdk.android.Fabric;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import org.json.JSONException;
import org.json.JSONObject;

public class SignInActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener, GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 905;

    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInAccount acct;
    private CallbackManager callbackManager;

    private TwitterLoginButton twitterLoginButton;

    private Profile faceUser = null;
    private TwitterSession twitUser = null;

    // This will be the client manager used by other classes too
    public static AmazonClientManager clientManager = null;

    // The user that will be persistent throughout the app
    public static User user  = null;

    private static boolean signedInGoogle = false;
    private static boolean signedInFace = false;
    private static boolean signInTwit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /////////////////////facebook////////////////////
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        //////////////////////facebook///////////////////

        /////////////////////twitter/////////////////////
        TwitterAuthConfig authConfig =  new TwitterAuthConfig(getResources().getString(R.string.consumer_key),
                                                              getResources().getString(R.string.consumer_key_secret));
        Fabric.with(this, new Twitter(authConfig), new Crashlytics());
        //////////////////////twitter////////////////////

        setContentView(R.layout.activity_sign_in);

        boolean test = getIntent().getBooleanExtra("test_boolean",false);
        Log.d("Testing intent passing", test+"");

        // Set Amazon Client Manager
        clientManager = new AmazonClientManager(this);

        // Button listeners
        findViewById(R.id.sign_in_button).setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.google_web_client_id))
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);

        signInButton.setSize(SignInButton.SIZE_STANDARD);
        //signInButton.setScopes(gso.getScopeArray());
        signInButton.setScopes(gso.getScopeArray());

        ///////////////////////facebook////////////////////////////////////////////
        LoginButton loginButton = (LoginButton) findViewById(R.id.facebook_login_button);
        loginButton.setReadPermissions(Arrays.asList("email"));
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                signedInFace = true;
                faceUser = Profile.getCurrentProfile();
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                            new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(
                                        JSONObject object,
                                        GraphResponse response) {
                                    try {
                                        Map<String, String> logins = new HashMap<String, String>();
                                        logins.put("graph.facebook.com", AccessToken.getCurrentAccessToken().getToken());
                                        clientManager.validateCredentials(logins);

                                        user = new User();
                                        user.setEmail(object.getString("email"));
                                        user.setProvider("Facebook");
                                        user.setProviderID(faceUser.getId());
                                        user.setUsername(faceUser.getName());
                                        new CreateUserTask(user).execute();
                                        goToMain();

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "email");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
            }
        });
        //////////////////////////////facebook//////////////////////////////////////

        /////////////////////////////twitter////////////////////////////////
        twitterLoginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                signInTwit = true;
                twitUser = result.data;
                TwitterAuthClient authClient = new TwitterAuthClient();
                authClient.requestEmail(twitUser, new Callback<String>() {
                    @Override
                    public void success(Result<String> result) {
                        Log.d("Email", result.data);
                        goToMain();
                    }

                    @Override
                    public void failure(TwitterException exception) {
                        exception.printStackTrace();
                    }
                });
            }
            @Override
            public void failure(TwitterException exception) {
                Log.d("TwitterKit", "Login with Twitter failure", exception);
            }
        });
        ////////////////////////////twitter////////////////////////////////////
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnected(Bundle t) {

    }

    @Override
    public void onStart() {
        super.onStart();
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    // [START onActivityResult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult()");

        super.onActivityResult(requestCode, resultCode, data);

        ////////////////////twittter///////////////////////
        twitterLoginButton.onActivityResult(requestCode,resultCode,data);
        ////////////////////twitter////////////////////////

        //////////////////facebook//////////////////////////////////
        callbackManager.onActivityResult(requestCode, resultCode, data);
        //////////////////facebook///////////////////////////////////

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }
    // [END onActivityResult]

    // [START handleSignInResult]
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult: " + result.isSuccess());
        if (result.isSuccess()) {
            signedInGoogle = true;
            // Signed in successfully, show authenticated UI.
            acct = result.getSignInAccount();
            //mStatusTextView.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));
            updateUI(true);

            // Access with token
            String token = acct.getIdToken();
            Map<String, String> logins = new HashMap<String, String>();
            logins.put("accounts.google.com", token);
            clientManager.validateCredentials(logins);

            user = new User();
            user.setEmail(acct.getEmail());
            user.setProvider("Google");
            user.setProviderID(acct.getId());
            user.setUsername(acct.getDisplayName());
            new CreateUserTask(user).execute();
            goToMain();
        } else {
            // Signed out, show unauthenticated UI.
            updateUI(false);
        }
    }
    // [END handleSignInResult]

    private void signIn() {
        Log.d(TAG, "signIn()");
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {

        if (signedInGoogle) {
            ResultCallback<Status> signOutCallBack = new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    updateUI(false);
                }
            };
            Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(signOutCallBack);
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(signOutCallBack);
            signedInGoogle = false;
        }
        else if (signedInFace) {
            LoginManager.getInstance().logOut();
            signedInFace = false;
        }
        else if (signInTwit) {
            TwitterSession twitterSession = TwitterCore.getInstance().getSessionManager().getActiveSession();
            if (twitterSession != null) {
                CookieSyncManager.createInstance(this);
                android.webkit.CookieManager cookieManager = android.webkit.CookieManager.getInstance();
                cookieManager.removeSessionCookie();
                Twitter.getSessionManager().clearActiveSession();
                Twitter.logOut();
                signInTwit = false;
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    private void updateUI(boolean signedIn) {
        if (signedIn) {
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }

    // Put tasks on Activities since we can use it to control UI elements
    private class CreateUserTask extends AsyncTask<Void, Void, Void> {
        private User user;

        public CreateUserTask(User user) {
            this.user = user;
        }

        // To do in the background
        protected Void doInBackground(Void... inputs) {
            user.create(); // Use the method from the User class to create it
            return null;
        }

        // To do after doInBackground is executed
        // We can use UI elements here
        protected void onPostExecute(Void result) {
        }
    }

    private void goToMain() {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }
}