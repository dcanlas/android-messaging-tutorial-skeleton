package com.wfl.application;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.login.widget.LoginButton;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.waffle.wfl.R;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;

public class LoginActivity extends Activity implements View.OnClickListener {

    private Button loginButton;
    private Button signUpButton;
    private LoginButton fbLoginButton;
    private EditText usernameField;
    private EditText passwordField;

    private Intent intent;
    private Intent serviceIntent;
    private Intent signupIntent;

    private static String LOG_TAG = "LOGIN_ACTIVITY";

    private String fbEmail;
    private String fbName;
    private Profile mFbProfile;

    private Firebase myFirebaseRef;
    /* The callback manager for Facebook */
    private CallbackManager mFacebookCallbackManager;
    /* Used to track user logging in/out off Facebook */
    private AccessTokenTracker mFacebookAccessTokenTracker;

    ProfilePhotoAsync mProfilePhotoAsync;
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        intent = new Intent(getApplicationContext(), ListUsersActivity.class);
        intent = new Intent(getApplicationContext(), InviteFriendsActivity.class); //temp
        serviceIntent = new Intent(getApplicationContext(), MessageService.class);
        signupIntent = new Intent(getApplicationContext(), SignupActivity.class);
        myFirebaseRef = MainDAO.getInstance().getFirebase();
        userDAO = new UserDAO();

        //See if there is a user already logged in.
        if (myFirebaseRef.getAuth() != null) {
            startActivity(intent);
            startService(serviceIntent);
        }

        setContentView(R.layout.activity_login);

        loginButton = (Button) findViewById(R.id.loginButton);
        signUpButton = (Button) findViewById(R.id.signupButton);
        usernameField = (EditText) findViewById(R.id.loginUsername);
        passwordField = (EditText) findViewById(R.id.loginPassword);

        /* Fb Login shenanigans */
        mFacebookCallbackManager = CallbackManager.Factory.create();
        fbLoginButton = (LoginButton) findViewById(R.id.fbLoginButton);
        List<String> mPermissions = Arrays.asList("public_profile", "user_friends", "email");
        fbLoginButton.setReadPermissions(mPermissions);
        mFacebookAccessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                Log.i(LOG_TAG, "Facebook.AccessTokenTracker.OnCurrentAccessTokenChanged");
                LoginActivity.this.onFacebookAccessTokenChange(currentAccessToken);
            }
        };


        mFbProfile = Profile.getCurrentProfile();

        loginButton.setOnClickListener(this);
        signUpButton.setOnClickListener(this);
        fbLoginButton.setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        stopService(new Intent(this, MessageService.class));
        // if user logged in with Facebook, stop tracking their token
        if (mFacebookAccessTokenTracker != null) {
            mFacebookAccessTokenTracker.stopTracking();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        if (view == loginButton) {
            handleLogin();
        }
        else if (view == signUpButton) {
            handleSignUp();
        }
    }

    private void handleLogin() {
        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();

        loginUser(username, password);
    }

    private void handleSignUp() {
        startActivity(signupIntent);
    }

    private void loginUser(String username, String password) {
        myFirebaseRef.authWithPassword(username, password, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                Log.d(LOG_TAG, "User ID: " + authData.getUid() + ", Provider: " + authData.getProvider());
                loginDone();
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                Toast.makeText(getApplicationContext(),
                        firebaseError.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void onFacebookAccessTokenChange(AccessToken token) {
        if (token != null) {
            myFirebaseRef.authWithOAuthToken("facebook", token.getToken(), new Firebase.AuthResultHandler() {
                @Override
                public void onAuthenticated(AuthData authData) {
                    // The Facebook user is now authenticated with your Firebase app
                    checkUserData(authData.getUid());
                }
                @Override
                public void onAuthenticationError(FirebaseError firebaseError) {
                    // there was an error
                    Log.e(LOG_TAG, "Error getting authData with fbLogin, " + firebaseError.getMessage());
                }
            });
        } else {
            myFirebaseRef.unauth();
        }
    }

    private void checkUserData(String uid) {
        Firebase userRef = userDAO.getUserRef(uid);
        final String mUid = uid;
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //we already have this user's details
                    loginDone();
                }
                else {
                    //get user details from facebook and add user
                    getUserDetailsFromFB(mUid);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(LOG_TAG, "checkUserdata error: " + firebaseError.getMessage());
            }
        });
    }

    private void getUserDetailsFromFB(final String uid) {
        Bundle parameters = new Bundle();
        parameters.putString("fields", "email,name");

        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me",
                parameters,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
           /* handle the result */
                        try {
                            Log.d(LOG_TAG, response.getJSONObject().toString());
                            fbEmail = response.getJSONObject().getString("email");
                            fbName = response.getJSONObject().getString("name");
                            UserModel user = new UserModel(fbName, fbName.toLowerCase(), fbEmail);
                            userDAO.addUser(uid, user);
                            loginDone();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).executeAsync();
//        mProfilePhotoAsync = new ProfilePhotoAsync(mFbProfile);
//        mProfilePhotoAsync.execute();
    }

    private void loginDone() {
        startActivity(intent);
        startService(serviceIntent);
    }

    class ProfilePhotoAsync extends AsyncTask<String, String, String> {
        Profile profile;
        public Bitmap bitmap;

        public ProfilePhotoAsync(Profile profile) {
            this.profile = profile;
        }

        @Override
        protected String doInBackground(String... params) {
            // Fetching data from URI and storing in bitmap
            bitmap = DownloadImageBitmap(profile.getProfilePictureUri(200, 200).toString());

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            //image download is complete we can use the image in our view
            super.onPostExecute(s);
//            mProfileImage.setImageBitmap(bitmap);
        }
    }

    public static Bitmap DownloadImageBitmap(String url) {
        Bitmap bm = null;
        try {
            URL aURL = new URL(url);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
        } catch (IOException e) {
            Log.e("IMAGE", "Error getting bitmap", e);
        }
        return bm;
    }

}
