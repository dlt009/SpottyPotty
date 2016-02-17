package com.teamoptimal.cse110project;

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
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.teamoptimal.cse110project.data.User;

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

    /* Amazon */
    public static AmazonClientManager clientManager;

    /* Google */
    private GoogleApiClient mGoogleApiClient; // API client
    private GoogleSignInAccount googleUser;
    private SignInButton googleSignInButton; // Button

    private static boolean signedInGoogle = false;

    /* Facebook */
    private LoginButton facebookSignInButton;
    private Profile facebookUser = null;
    private CallbackManager callbackManager;

    private static boolean signedInFacebook = false;

    /* Twitter */
    private TwitterLoginButton twitterLoginButton;
    private TwitterSession twitterUser = null;

    private static boolean signedInTwitter = false;

    /* Our user */
    public static User user  = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Initialize Amazon Client Manager */
        clientManager = new AmazonClientManager(this);

        /* Initialize Facebook */
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        /* Initialize Twitter */
        TwitterAuthConfig authConfig =
                new TwitterAuthConfig(getResources().getString(R.string.consumer_key),
                        getResources().getString(R.string.consumer_key_secret));
        Fabric.with(this, new Twitter(authConfig), new Crashlytics());

        /* Set content view */
        setContentView(R.layout.activity_sign_in);

        /* Initialize buttons  */
        googleSignInButton = (SignInButton) findViewById(R.id.google_login_button);
        facebookSignInButton = (LoginButton) findViewById(R.id.facebook_login_button);
        twitterLoginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);

        /* Set Facebook */
        facebookSignInButton.setReadPermissions(Arrays.asList("email"));
        facebookSignInButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                signedInFacebook = true;
                facebookUser = Profile.getCurrentProfile();
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject object,
                                    GraphResponse response) {
                                try {
                                    // Validate credentials with Amazon
                                    Map<String, String> logins = new HashMap<String, String>();
                                    logins.put("graph.facebook.com",
                                            AccessToken.getCurrentAccessToken().getToken());
                                    clientManager.validateCredentials(logins);

                                    // Create/update new user
                                    user = new User();
                                    user.setEmail(object.getString("email"));
                                    user.setProvider("Facebook");
                                    user.setProviderID(facebookUser.getId());
                                    user.setUsername(facebookUser.getName());
                                    new CreateUserTask(user).execute();

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

        /* Set Twitter */
        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                signedInTwitter = true;
                twitterUser = result.data;
                TwitterAuthClient authClient = new TwitterAuthClient();
                authClient.requestEmail(twitterUser, new Callback<String>() {
                    @Override
                    public void success(Result<String> result) {
                        // Validate credentials with Amazon
                        Map<String, String> logins = new HashMap<String, String>();
                        String value = twitterUser.getAuthToken().token + ";" +
                                twitterUser.getAuthToken().secret;
                        logins.put("api.twitter.com", value);
                        clientManager.validateCredentials(logins);

                        // Create/update user
                        user = new User();
                        user.setEmail(twitterUser.getUserName());
                        user.setProvider("Twitter");
                        user.setProviderID(googleUser.getId());
                        user.setUsername(twitterUser.getUserName());
                        new CreateUserTask(user).execute();

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

        /* Initialize and set up Google */
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.google_web_client_id))
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        googleSignInButton.setSize(SignInButton.SIZE_STANDARD);
        googleSignInButton.setScopes(gso.getScopeArray());
        findViewById(R.id.google_login_button).setOnClickListener(this);
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

        /* Cached sign-in with Google */
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult()");

        super.onActivityResult(requestCode, resultCode, data);

        /* Twitter */
        twitterLoginButton.onActivityResult(requestCode, resultCode, data);

        /* Facebook */
        callbackManager.onActivityResult(requestCode, resultCode, data);

        /* Google */
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult: " + result.isSuccess());

        if (result.isSuccess()) {
            googleUser = result.getSignInAccount();
            signedInGoogle = true;

            updateUI(true);

            // Access with token
            String token = googleUser.getIdToken();

            // Validate credentials with Amazon
            Map<String, String> logins = new HashMap<String, String>();
            logins.put("accounts.google.com", token);
            clientManager.validateCredentials(logins);

            // Create/update user
            user = new User();
            user.setEmail(googleUser.getEmail());
            user.setProvider("Google");
            user.setProviderID(googleUser.getId());
            user.setUsername(googleUser.getDisplayName());
            new CreateUserTask(user).execute();
        } else {
            // Signed out, show unauthenticated UI.
            updateUI(false);
        }
    }

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
        else if (signedInFacebook) {
            LoginManager.getInstance().logOut();
            signedInFacebook = false;
        }
        else if (signedInTwitter) {
            TwitterSession twitterSession = TwitterCore.getInstance().getSessionManager().getActiveSession();
            if (twitterSession != null) {
                CookieSyncManager.createInstance(this);
                android.webkit.CookieManager cookieManager = android.webkit.CookieManager.getInstance();
                cookieManager.removeSessionCookie();
                Twitter.getSessionManager().clearActiveSession();
                Twitter.logOut();
                signedInTwitter = false;
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
            findViewById(R.id.google_login_button).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.google_login_button).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.google_login_button:
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
            goToMain();
        }
    }

    private void goToMain() {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }
}