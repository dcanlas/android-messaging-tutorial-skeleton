package com.wfl.application;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.waffle.wfl.R;

public class LoginActivity extends Activity implements View.OnClickListener {

    private Button loginButton;
    private Button signUpButton;
    private EditText usernameField;
    private EditText passwordField;

    private Intent intent;
    private Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        intent = new Intent(getApplicationContext(), ListUsersActivity.class);
        serviceIntent = new Intent(getApplicationContext(), MessageService.class);

        //See if there is a user already logged in.
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            startActivity(intent);
            startService(serviceIntent);
        }

        setContentView(R.layout.activity_login);

        loginButton = (Button) findViewById(R.id.loginButton);
        signUpButton = (Button) findViewById(R.id.signupButton);
        usernameField = (EditText) findViewById(R.id.loginUsername);
        passwordField = (EditText) findViewById(R.id.loginPassword);

        loginButton.setOnClickListener(this);
        signUpButton.setOnClickListener(this);
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
    }

    private void handleLogin() {
        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();

        ParseUser.logInInBackground(username, password, new LogInCallback() {
            public void done(ParseUser user, com.parse.ParseException e) {
                if (user != null) {
                    startActivity(intent);
                    startService(serviceIntent);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "There was an error logging in.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void handleSignUp() {
        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();

        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);

        user.signUpInBackground(new SignUpCallback() {
            public void done(com.parse.ParseException e) {
                if (e == null) {
                    startActivity(intent);
                    startService(serviceIntent);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "There was an error signing up."
                            , Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
