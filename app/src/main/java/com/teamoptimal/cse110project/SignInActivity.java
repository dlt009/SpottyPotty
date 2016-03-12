package com.teamoptimal.cse110project;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.digits.sdk.android.Digits;
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
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import org.json.JSONException;
import org.json.JSONObject;

public class SignInActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 905;
    private static final String PREFERENCES = "AppPrefs";

    /* Amazon */
    public static AmazonClientManager clientManager;

    /* Google */
    private GoogleApiClient mGoogleApiClient; // API client
    private GoogleSignInAccount googleUser;
    private SignInButton googleSignInButton; // Button

    private boolean signedInGoogle;

    /* Facebook */
    private LoginButton facebookSignInButton;
    private Profile facebookUser = null;
    private CallbackManager callbackManager;

    private boolean signedInFacebook;

    /* Twitter */
    private TwitterLoginButton twitterLoginButton;
    private TwitterSession twitterUser;

    private boolean signedInTwitter;

    private Button signOutButton;
    TextView signInText;

    /* Our user */
    public static User user  = null;

    /* sharedPreferences to store log in status */
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    private boolean isGoogleSilentSignIn = false;

    private ProgressDialog loadingSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* initialize sharedpreferences and editor */
        sharedPreferences = getSharedPreferences(PREFERENCES, 0);
        editor = sharedPreferences.edit();

        /* set current login status */
        signedInGoogle = sharedPreferences.getBoolean("goog", false);
        signedInFacebook = sharedPreferences.getBoolean("face", false);
        signedInTwitter = sharedPreferences.getBoolean("twit", false);

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

        Log.d("Twitter Session",""+(twitterUser != null));

        /* Initialize buttons  */
        googleSignInButton = (SignInButton) findViewById(R.id.google_login_button);
        facebookSignInButton = (LoginButton) findViewById(R.id.facebook_login_button);
        twitterLoginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        signOutButton = (Button) findViewById(R.id.sign_out_button);

        signInText = (TextView) findViewById(R.id.sign_in_text);

        /* Set Facebook */
        facebookSignInButton.setReadPermissions(Arrays.asList("email"));
        facebookSignInButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                loadingSpinner.show();

                signedInFacebook = true;
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

                                    facebookUser = Profile.getCurrentProfile();

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
                Toast.makeText(SignInActivity.this, "Facebook sign in unsuccessful", Toast.LENGTH_LONG).show();
            }
        });

        /* Set Twitter */
        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                loadingSpinner.show();

                signedInTwitter = true;
                twitterUser = result.data;

                TwitterAuthToken twitToken = twitterUser.getAuthToken();

                Map<String, String> logins = new HashMap<String, String>();
                String value = twitToken.token + ";" + twitToken.secret;
                logins.put("api.twitter.com", value);
                clientManager.validateCredentials(logins);

                // Create/update user
                user = new User();
                user.setEmail(twitterUser.getUserName());
                user.setProvider("Twitter");
                user.setProviderID(twitterUser.getUserId()+"");
                user.setUsername(twitterUser.getUserName());
                new CreateUserTask(user).execute();
        }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(SignInActivity.this, "Twitter sign in unsuccessful", Toast.LENGTH_LONG).show();
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
        googleSignInButton.setOnClickListener(this);
        signOutButton.setOnClickListener(this);

        updateUI(signedInFacebook || signedInTwitter || signedInGoogle);

        loadingSpinner = new ProgressDialog(this);
        loadingSpinner.setTitle("Signing In");
        loadingSpinner.setMessage("Please wait");
    }

    @Override
    public void onStart() {
        super.onStart();
        isGoogleSilentSignIn = true;
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
            if(!isGoogleSilentSignIn)
                loadingSpinner.show();

            googleUser = result.getSignInAccount();
            signedInGoogle = true;

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
            if(!isGoogleSilentSignIn)
                Toast.makeText(SignInActivity.this, "Google sign in unsuccessful", Toast.LENGTH_LONG).show();
            else
                isGoogleSilentSignIn = false;
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
                    //updateUI(false);
                }
            };
            Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(signOutCallBack);
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(signOutCallBack);
            signedInGoogle = false;
            editor.putBoolean("goog", signedInGoogle);
            editor.commit();
        }
        if (signedInFacebook) {
            LoginManager.getInstance().logOut();
            signedInFacebook = false;
            editor.putBoolean("face", signedInFacebook);
            editor.commit();
        }
        if (signedInTwitter) {
            Twitter.getSessionManager().clearActiveSession();
            signedInTwitter = false;
            editor.putBoolean("twit", signedInTwitter);
            editor.commit();
        }
        goToMain();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    private void updateUI(boolean signedIn) {
        if (signedIn) {
            signOutButton.setVisibility(View.VISIBLE);
            signInText.setText("Are you sure you want to log out?");
            googleSignInButton.setVisibility(View.GONE);
            facebookSignInButton.setVisibility(View.GONE);
            twitterLoginButton.setVisibility(View.GONE);
        } else {
            signOutButton.setVisibility(View.GONE);
            signInText.setText("Choose a sign-in option");
            googleSignInButton.setVisibility(View.VISIBLE);
            facebookSignInButton.setVisibility(View.VISIBLE);
            twitterLoginButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.google_login_button:
                signIn();
                break;
            case R.id.sign_out_button:
                signOut();
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
        editor.putBoolean("goog", signedInGoogle);
        editor.putBoolean("face", signedInFacebook);
        editor.putBoolean("twit", signedInTwitter);
        if(user != null) {
            Log.d(TAG, "not null");
            editor.putString("user_email", user.getEmail());
            editor.putString("user_name", user.getUsername());
            editor.putInt("times_reported", user.getReportCount());
        }
        editor.commit();
        if(isGoogleSilentSignIn)
            isGoogleSilentSignIn = false;
        else {
            loadingSpinner.dismiss();
            finish();
        }
    }
}