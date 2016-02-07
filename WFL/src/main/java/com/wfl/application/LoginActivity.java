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
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.waffle.wfl.R;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class LoginActivity extends Activity implements View.OnClickListener {

    private Button loginButton;
    private Button signUpButton;
    private Button fbLoginButton;
    private EditText usernameField;
    private EditText passwordField;

    private Intent intent;
    private Intent serviceIntent;

    private static String LOG_TAG = "LOGIN_ACTIVITY";

    private String fbEmail;
    private String fbName;
    private Profile mFbProfile;

    private Firebase myFirebaseRef;

    ProfilePhotoAsync mProfilePhotoAsync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        intent = new Intent(getApplicationContext(), ListUsersActivity.class);
        intent = new Intent(getApplicationContext(), InviteFriendsActivity.class); //temp
        serviceIntent = new Intent(getApplicationContext(), MessageService.class);
        myFirebaseRef = MainDAO.getInstance().getFirebase();

        //See if there is a user already logged in.
        if (myFirebaseRef.getAuth() != null) {
            startActivity(intent);
            startService(serviceIntent);
        }

        setContentView(R.layout.activity_login);

        loginButton = (Button) findViewById(R.id.loginButton);
        signUpButton = (Button) findViewById(R.id.signupButton);
        fbLoginButton = (Button) findViewById(R.id.fbLogin);
        usernameField = (EditText) findViewById(R.id.loginUsername);
        passwordField = (EditText) findViewById(R.id.loginPassword);

        mFbProfile = Profile.getCurrentProfile();

        loginButton.setOnClickListener(this);
        signUpButton.setOnClickListener(this);
        fbLoginButton.setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        stopService(new Intent(this, MessageService.class));
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
        else if (view == fbLoginButton) {
            handleFbLogin();
        }
    }

    private void handleLogin() {
        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();

        loginUser(username, password);
    }

    private void handleSignUp() {
        final String username = usernameField.getText().toString();
        final String password = passwordField.getText().toString();

        if (username.length() > 0 && password.length() > 0) {
            myFirebaseRef.createUser(username, password, new Firebase.ValueResultHandler<Map<String, Object>>() {
                @Override
                public void onSuccess(Map<String, Object> result) {
                    Log.d(LOG_TAG, "Successfully created user account with uid: " + result.get("uid"));
                    loginUser(username, password);
                }
                @Override
                public void onError(FirebaseError firebaseError) {
                    Toast.makeText(getApplicationContext(),
                            firebaseError.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void loginUser(String username, String password) {
        myFirebaseRef.authWithPassword(username, password,  new Firebase.AuthResultHandler() {
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

    private void handleFbLogin() {
        List<String> mPermissions = Arrays.asList("public_profile", "user_friends", "email");
        ParseFacebookUtils.logInWithReadPermissionsInBackground(LoginActivity.this, mPermissions, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                if (user == null) {
                    Log.d(LOG_TAG, "Uh oh. The user cancelled the Facebook login.");
                } else if (user.isNew()) {
                    Log.d(LOG_TAG, "User signed up and logged in through Facebook!");
                    getUserDetailsFromFB();
                } else {
                    Log.d(LOG_TAG, "User logged in through Facebook!");
                    getUserDetailsFromParse();
                }
            }
        });
    }

    private void getUserDetailsFromFB() {
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
                            Log.d(LOG_TAG, AccessToken.getCurrentAccessToken().getToken());
                            fbEmail = response.getJSONObject().getString("email");
//                            mEmailID.setText(email);
                            fbName = response.getJSONObject().getString("name");
//                            mUsername.setText(name);
                            saveNewUser();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).executeAsync();
//        mProfilePhotoAsync = new ProfilePhotoAsync(mFbProfile);
//        mProfilePhotoAsync.execute();
    }

    private void saveNewUser() {
        final ParseUser parseUser = ParseUser.getCurrentUser();
        parseUser.setUsername(fbName);
        parseUser.setEmail(fbEmail);
        /*
//        Saving profile photo as a ParseFile
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        Bitmap bitmap = ((BitmapDrawable) mProfileImage.getDrawable()).getBitmap(); //Todo: set mprofileImage somewhere
        Bitmap bitmap = mProfilePhotoAsync.bitmap;
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        byte[] data = stream.toByteArray();

        String thumbName = parseUser.getUsername().replaceAll("\\s+", "");
        final ParseFile parseFile = new ParseFile(thumbName + "_thumb.jpg", data);
        parseFile.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                parseUser.put("profileThumb", parseFile);
                //Finally save all the user details
                parseUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        Toast.makeText(LoginActivity.this, "New user:" + fbName + " Signed up", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        */
        parseUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Toast.makeText(LoginActivity.this, "New user:" + fbName + " Signed up", Toast.LENGTH_SHORT).show();
                loginDone();
            }
        });
    }

    private void getUserDetailsFromParse() {
        ParseUser parseUser = ParseUser.getCurrentUser();
        /* Don't need this right now
        //Fetch profile photo
        try {
            ParseFile parseFile = parseUser.getParseFile("profileThumb");
            byte[] data = parseFile.getData();
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            mProfileImage.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
        fbEmail = parseUser.getEmail();
        fbName = parseUser.getUsername();
        Toast.makeText(LoginActivity.this, "Welcome back " + fbName, Toast.LENGTH_SHORT).show();
        loginDone();
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
