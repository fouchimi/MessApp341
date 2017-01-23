package com.social.messapp34;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
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
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.social.messapp34.utils.Constants;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.User;
import com.twitter.sdk.android.core.services.AccountService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;

public class SignInActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "5liuPi4bQUdx8qUBaitkc3Dcx";
    private static final String TWITTER_SECRET = "Oi2s9atv3dDHRcGbBjxdGOclNZzxUtsTI18fm15HBUSL1evcbT";
    private static final String TAG = SignInActivity.class.getSimpleName();
    private ProgressDialog loginProgressDlg;


    private EditText usernameText;
    private EditText passwordText;
    private Button mLoginButton;
    private Button mRegisterButton;
    private LoginButton mFbLoginButton;
    private TwitterLoginButton mTwitterLoginButton;
    private ParseUser mCurrentUser;
    private AccessToken mAccessToken;
    private AccessTokenTracker mAccessTokenTracker;
    private static final int RC_GET_TOKEN = 9002;
    private GoogleApiClient mGoogleApiClient;
    private Button mGoogleSignInButton;

    private CallbackManager mCallbackManager;
    private FacebookCallback<LoginResult> mCallBack = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            if(Profile.getCurrentProfile() == null){
                GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                final String id = object.optString("id");
                                final String email = object.optString("email");
                                final String name = object.optString("name");
                                final String profile_picture = Constants.FACEBOOK_BASE_URL + "/"+id + "/picture?type=large";
                                final String username = (email.isEmpty()) ? name+"_"+id : email;
                                final String password = getString(R.string.default_password);
                                String location="";
                                try {
                                    location = object.getJSONObject("location").getString("name");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                ParseQuery<ParseUser> queryUser = ParseUser.getQuery();
                                queryUser.whereEqualTo(Constants.USERNAME, username);
                                final String finalLocation = location;
                                queryUser.findInBackground(new FindCallback<ParseUser>() {
                                    @Override
                                    public void done(List<ParseUser> users, ParseException e) {
                                        if(e == null) {
                                            if (users.size() == 0) {
                                                Toast.makeText(SignInActivity.this, getString(R.string.login_text), Toast.LENGTH_LONG).show();
                                                mCurrentUser = new ParseUser();
                                                mCurrentUser.setUsername(username);
                                                mCurrentUser.setPassword(password);
                                                mCurrentUser.put(Constants.PROFILE_PICTURE, profile_picture);
                                                mCurrentUser.put(Constants.LOCATION, finalLocation);
                                                if(email.isEmpty()) mCurrentUser.setEmail(name+"@messapp.com");
                                                else mCurrentUser.setEmail(email);

                                                mCurrentUser.signUpInBackground(new SignUpCallback() {
                                                    @Override
                                                    public void done(ParseException e) {
                                                        if (e != null) {
                                                            Toast.makeText(SignInActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                            Log.w(TAG, "Error : " + e.getMessage() + ":" + e.getCode());
                                                            loginProgressDlg.dismiss();
                                                            if (e.getCode() == 202) {
                                                                Toast.makeText(SignInActivity.this, getString(R.string.username_taken), Toast.LENGTH_LONG).show();
                                                            }

                                                        } else {
                                                            //Toast.makeText(getActivity(), getString(R.string.user_saved), Toast.LENGTH_SHORT).show();
                                                            ParseUser.logInInBackground(username, password, new LogInCallback() {
                                                                @Override
                                                                public void done(ParseUser user, ParseException e) {
                                                                    if(e == null){
                                                                        goToHomeActivity();
                                                                    }else {
                                                                        Log.d(TAG, e.getMessage());
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }else {
                                                Toast.makeText(SignInActivity.this, getString(R.string.login_text), Toast.LENGTH_LONG).show();
                                                ParseUser.logInInBackground(username, password, new LogInCallback() {
                                                    @Override
                                                    public void done(ParseUser user, ParseException e) {
                                                        goToHomeActivity();
                                                        loginProgressDlg.dismiss();
                                                    }
                                                });
                                            }
                                        }else {
                                            //An error has occured
                                            Toast.makeText(SignInActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                            loginProgressDlg.dismiss();
                                        }
                                    }
                                });
                                Log.d(TAG, response.getRawResponse());

                            }
                        }).executeAsync();
            }
            loginProgressDlg = ProgressDialog.show(SignInActivity.this, null,
                    getString(R.string.alert_wait));
        }
        @Override
        public void onCancel() {
            Toast.makeText(SignInActivity.this, getString(R.string.auth_canceled), Toast.LENGTH_LONG).show();
            loginProgressDlg.dismiss();
        }

        @Override
        public void onError(FacebookException error) {
            Toast.makeText(SignInActivity.this, getString(R.string.facebook_error), Toast.LENGTH_LONG).show();
            loginProgressDlg.dismiss();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Crashlytics(), new Twitter(authConfig));
        mCallbackManager = CallbackManager.Factory.create();
        AppEventsLogger.activateApp(this);

        mAccessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
            }
        };

        LoginManager.getInstance().logOut();

        mAccessToken = AccessToken.getCurrentAccessToken();
        mAccessTokenTracker.startTracking();
        setContentView(R.layout.activity_sign_in);

        mLoginButton = (Button) findViewById(R.id.btnLogin);
        mRegisterButton = (Button) findViewById(R.id.btnReg);
        usernameText = (EditText) findViewById(R.id.user);
        passwordText = (EditText) findViewById(R.id.pwd);

        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/Raleway-ExtraBold.ttf");

        mFbLoginButton = (LoginButton) findViewById(R.id.login_button);
        mTwitterLoginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        mGoogleSignInButton = (Button) findViewById(R.id.google_sign_in_button);

        mGoogleSignInButton.setTypeface(typeFace);

        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignInActivity.this, RegisterActivity.class);
                SignInActivity.this.startActivity(intent);
            }
        });

        mFbLoginButton.setReadPermissions(Arrays.asList("email,public_profile,user_location"));

        mFbLoginButton.registerCallback(mCallbackManager, mCallBack);
        mFbLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runThread();
            }
        });

        mTwitterLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runThread();
            }
        });
        mTwitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                TwitterSession session = result.data;
                Toast.makeText(SignInActivity.this, session.getUserName(), Toast.LENGTH_LONG).show();
                Twitter twitter = Twitter.getInstance();
                TwitterApiClient api = twitter.core.getApiClient(session);
                AccountService service = api.getAccountService();
                Call<User> user = service.verifyCredentials(true, true);

                user.enqueue(new Callback<User>()
                {
                    @Override
                    public void success(Result<User> userResult)
                    {
                        String name = userResult.data.name;
                        String email = userResult.data.email;
                        final String username = userResult.data.screenName;
                        final String profile_picture = userResult.data.profileImageUrl;
                        final String location = userResult.data.location;
                        Log.d(TAG, userResult.data.screenName);
                        Log.d(TAG, name +" " + email);
                        final String parseEmail = (email == null) ? userResult.data.screenName+"@messapp.com" : email;
                        final String password = getString(R.string.default_password);
                        ParseQuery<ParseUser> queryUser = ParseUser.getQuery();
                        queryUser.whereEqualTo(Constants.USERNAME, username);
                        queryUser.findInBackground(new FindCallback<ParseUser>() {
                            @Override
                            public void done(List<ParseUser> users, ParseException e) {
                                if(e == null) {
                                    if (users.size() == 0) {
                                        mCurrentUser = new ParseUser();
                                        mCurrentUser.setUsername(username);
                                        mCurrentUser.setEmail(parseEmail);
                                        mCurrentUser.setPassword(password);
                                        mCurrentUser.put(Constants.PROFILE_PICTURE, profile_picture);
                                        mCurrentUser.put(Constants.LOCATION, location);
                                        mCurrentUser.signUpInBackground(new SignUpCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if(e == null){
                                                    //Toast.makeText(getActivity(), getString(R.string.login_text), Toast.LENGTH_LONG).show();
                                                    ParseUser.logInInBackground(username, password, new LogInCallback() {
                                                        @Override
                                                        public void done(ParseUser user, ParseException e) {
                                                            if( e == null) {
                                                                loginProgressDlg.dismiss();
                                                                goToHomeActivity();
                                                            }
                                                            else {
                                                                Log.d(TAG, e.getMessage());
                                                                loginProgressDlg.dismiss();
                                                                Toast.makeText(SignInActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                                            }
                                                        }
                                                    });
                                                }else {
                                                    Log.d(TAG, e.getMessage());
                                                    Toast.makeText(SignInActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                                    loginProgressDlg.dismiss();
                                                }
                                            }
                                        });
                                    }else {
                                        // Go To home Screen
                                        if(users.size() == 1){
                                            Toast.makeText(SignInActivity.this, getString(R.string.login_text), Toast.LENGTH_LONG).show();
                                            ParseUser.logInInBackground(username, password, new LogInCallback() {
                                                @Override
                                                public void done(ParseUser user, ParseException e) {
                                                    if( e == null) {
                                                        goToHomeActivity();
                                                        loginProgressDlg.dismiss();
                                                    }
                                                    else {
                                                        Log.d(TAG, e.getMessage());
                                                        Toast.makeText(SignInActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                                        loginProgressDlg.dismiss();
                                                    }
                                                }
                                            });
                                        }else {
                                            Toast.makeText(SignInActivity.this, "Sorry, try again later", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }else {
                                    Toast.makeText(SignInActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                    Log.d(TAG, e.getMessage());
                                }
                            }
                        });

                    }

                    @Override
                    public void failure(TwitterException e)
                    {
                        Log.d(TAG, e.getMessage());
                        Toast.makeText(SignInActivity.this, getString(R.string.twitter_email_failed), Toast.LENGTH_LONG).show();
                        loginProgressDlg.dismiss();
                    }

                });
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(SignInActivity.this, getString(R.string.twitter_failure), Toast.LENGTH_LONG).show();
                loginProgressDlg.dismiss();
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mGoogleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_GET_TOKEN);
            }
        });
    }

    private void handleSignInResult(GoogleSignInResult result) {
        loginProgressDlg = ProgressDialog.show(SignInActivity.this, null,
                getString(R.string.alert_wait));
        if (result.isSuccess()) {
            //String idToken = result.getSignInAccount().getIdToken();
            //Toast.makeText(this, idToken, Toast.LENGTH_LONG).show();
            GoogleSignInAccount userGoogleAccount = result.getSignInAccount();
            Log.d(TAG, "email: " + userGoogleAccount.getEmail());
            Log.d(TAG, "Display name: " + userGoogleAccount.getDisplayName());
            final String email = userGoogleAccount.getEmail();
            final String username = userGoogleAccount.getGivenName();
            Uri profile_uri = userGoogleAccount.getPhotoUrl();
            Log.d(TAG, profile_uri.getAuthority());
            Log.d(TAG, profile_uri.getPath());
            final String profile_picture = "http://" + profile_uri.getAuthority() + profile_uri.getPath();
            final String password = getString(R.string.default_password);
            ParseQuery<ParseUser> queryUser = ParseUser.getQuery();
            queryUser.whereEqualTo(Constants.EMAIL, email);
            queryUser.getFirstInBackground(new GetCallback<ParseUser>() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if(e != null){
                        Log.d(TAG, e.getMessage());
                        mCurrentUser = new ParseUser();
                        mCurrentUser.setUsername(username);
                        mCurrentUser.setPassword(password);
                        mCurrentUser.setEmail(email);
                        mCurrentUser.put(Constants.PROFILE_PICTURE, profile_picture);
                        mCurrentUser.signUpInBackground(new SignUpCallback() {
                            @Override
                            public void done(ParseException e) {
                                if(e == null){
                                    Log.d(TAG, "Account created !!!");
                                    Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    SignInActivity.this.startActivity(intent);
                                    loginProgressDlg.dismiss();
                                }else {
                                    Toast.makeText(SignInActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }else {
                        ParseUser.logInInBackground(username, password, new LogInCallback() {
                            @Override
                            public void done(ParseUser user, ParseException e) {
                                if(e == null){
                                    Log.d(TAG, "Logging in ...");
                                    Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    SignInActivity.this.startActivity(intent);
                                    loginProgressDlg.dismiss();
                                }else {
                                    Toast.makeText(SignInActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }
            });
        } else {
            Log.d(TAG, "" + result.getStatus());
        }
    }

    private void runThread() {
        new Thread() {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                            loginProgressDlg = ProgressDialog.show(SignInActivity.this, null,
                                    getString(R.string.alert_wait));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        mAccessTokenTracker.stopTracking();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(loginProgressDlg != null){
            loginProgressDlg.dismiss();
        }
    }

    private void goToHomeActivity(){
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        mTwitterLoginButton.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_GET_TOKEN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Log.d(TAG, "onActivityResult:GET_TOKEN:success:" + result.getStatus().isSuccess());
            handleSignInResult(result);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void logIn(View view){
        String username = usernameText.getText().toString();
        String password = passwordText.getText().toString();
        if(username.length() > 0 && password.length() > 0){
            ParseUser.logInInBackground(username, password, new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if(e == null){
                        Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        SignInActivity.this.startActivity(intent);
                    }else {
                        Log.d(TAG, "Error occured");
                        Toast.makeText(SignInActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }else {
            Log.d(TAG, "Fill all fields");
            Toast.makeText(this, "Enter username and password", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, connectionResult.getErrorMessage(), Toast.LENGTH_LONG).show();
        Log.d(TAG, connectionResult.getErrorMessage());
    }
}
