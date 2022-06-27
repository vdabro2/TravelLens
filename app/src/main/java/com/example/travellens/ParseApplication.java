package com.example.travellens;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // this is my database application info
        ParseObject.registerSubclass(Post.class);
        ParseObject.registerSubclass(Likes.class);

        Parse.initialize(new Parse.Configuration.Builder(this)
                        .applicationId(getString(R.string.application_id))
                        .clientKey(getString(R.string.client_id))
                .server("https://parseapi.back4app.com").build());
    }
}
