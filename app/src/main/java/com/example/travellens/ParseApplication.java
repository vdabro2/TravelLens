package com.example.travellens;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ParseObject.registerSubclass(Post.class);
        Parse.initialize(new Parse.Configuration.Builder(this)
                        .applicationId("qooh18KmRxw11QIHGgIU2GlmB5Lu56yWnEUgb3bS")
                //.applicationId("dszd14eVQBIEIrMqRKyDwShjKaLMUhrP9x61k5Nn") // should correspond to Application Id env variable
                        .clientKey("4cSvdYqnHKO8Bw0q6un3mqDMNvUjMiRF8mcih5bG")
                //.clientKey("eraiFOFKswQtgL51wipYveurMqtR3sgA6qGeK68l")  // should correspond to Client key env variable
                .server("https://parseapi.back4app.com").build());
    }
}
