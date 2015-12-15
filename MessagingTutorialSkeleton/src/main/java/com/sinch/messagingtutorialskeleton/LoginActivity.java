package com.sinch.messagingtutorialskeleton;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.messagingtutorialskeleton.R;
import com.parse.LogInCallback;
import com.parse.ParseUser;
import com.parse.SignUpCallback;


public class LoginActivity extends Activity implements View.OnClickListener {

    private Button loginButton;
    private Button signUpButton;
    private EditText usernameField;
    private EditText passwordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //See if there is a user already logged in.
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            //start next activity
            //start sinch service
        }

        loginButton = (Button) findViewById(R.id.loginButton);
        signUpButton = (Button) findViewById(R.id.signupButton);
        usernameField = (EditText) findViewById(R.id.loginUsername);
        passwordField = (EditText) findViewById(R.id.loginPassword);

        loginButton.setOnClickListener(this);
        signUpButton.setOnClickListener(this);
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
                    //start next activity
                    //start sinch service
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
                    //start next activity
                    //start sinch service
                } else {
                    Toast.makeText(getApplicationContext(),
                            "There was an error signing up."
                            , Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
