package com.wfl.application;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.waffle.wfl.R;

import java.util.Map;

public class SignupActivity extends Activity implements View.OnClickListener {

    private String LOG_TAG = "SIGNUP_ACTIVITY";
    private Intent inviteIntent;
    private Firebase myFirebaseRef;
    private UserDAO userDAO;

    private EditText nameField;
    private EditText emailField;
    private EditText passwordField;

    private Button signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        inviteIntent = new Intent(getApplicationContext(), InviteFriendsActivity.class);
        myFirebaseRef = MainDAO.getInstance().getFirebase();
        userDAO = new UserDAO();

        nameField = (EditText) findViewById(R.id.nameField);
        emailField = (EditText) findViewById(R.id.emailField);
        passwordField = (EditText) findViewById(R.id.passwordField);

        signupButton = (Button) findViewById(R.id.signupButton);

        signupButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == signupButton) {
            handleSignup();
        }
    }

    private void handleSignup() {
        final String name = nameField.getText().toString();
        final String email = emailField.getText().toString();
        final String password = passwordField.getText().toString();

        if (name.length() > 0 && email.length() > 0 && password.length() > 0) {
            myFirebaseRef.createUser(email, password, new Firebase.ValueResultHandler<Map<String, Object>>() {
                @Override
                public void onSuccess(Map<String, Object> result) {
                    Log.d(LOG_TAG, "Successfully created user account with uid: " + result.get("uid"));
                    UserModel user = new UserModel(name, name.toLowerCase(), email);
                    userDAO.addUser(result.get("uid").toString(), user);
                    loginUser(email, password);
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
                startActivity(inviteIntent);
            }
            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                Toast.makeText(getApplicationContext(),
                        firebaseError.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
