package com.wfl.messagingtutorialskeleton;


import android.app.Application;

import com.parse.Parse;

public class App extends Application {

    @Override public void onCreate() {
        super.onCreate();

        Parse.initialize(this, "VbMeH3TbACtNQKKl4y8EocBFT4LuSjHQrz5kWMWv", "lzRt3lHE6V2i3yHYmiUxSrRKfCvXJxQoNX6h8mVJ"); // Your Application ID and Client Key are defined elsewhere
    }
}
