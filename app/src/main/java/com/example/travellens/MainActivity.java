package com.example.travellens;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // this class creates the launch animation
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this);
                Intent intent=new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent, options.toBundle());
                finish();
            }
        },1000);
    }
}