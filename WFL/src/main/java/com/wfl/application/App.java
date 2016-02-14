package com.wfl.application;


import android.app.Application;

import com.facebook.FacebookSdk;
import com.firebase.client.Firebase;
import com.parse.Parse;
import com.waffle.wfl.R;

public class App extends Application {

    @Override
    public void onCreate() {
        Parse.initialize(this, getString(R.string.parse_app_id), getString(R.string.parse_client_key));
        Firebase.setAndroidContext(this);
        FacebookSdk.sdkInitialize(this);
        initSingletons();
        super.onCreate();
    }

    public void initSingletons() {
        MainDAO.initInstance();
    }

}

