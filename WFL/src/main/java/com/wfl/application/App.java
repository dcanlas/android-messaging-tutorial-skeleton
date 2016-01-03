package com.wfl.application;


import android.app.Application;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.waffle.wfl.R;

public class App extends Application {

    @Override
    public void onCreate() {
        Parse.initialize(this, getString(R.string.parse_app_id), getString(R.string.parse_client_key));
        ParseFacebookUtils.initialize(this);
        super.onCreate();
    }

}
