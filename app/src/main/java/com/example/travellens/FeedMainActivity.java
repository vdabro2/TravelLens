package com.example.travellens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.travellens.fragments.ComposeFragment;
import com.example.travellens.fragments.ProfileFragment;
import com.google.android.libraries.places.api.Places;

import com.example.travellens.fragments.PostsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class FeedMainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_main);

        final FragmentManager fragmentManager = getSupportFragmentManager();
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = new Fragment();
                switch (item.getItemId()) {
                    case R.id.action_post:
                        fragment = new ComposeFragment();
                        break;
                    case R.id.action_home:
                        fragment = new PostsFragment();
                        break;
                    case R.id.action_prof:
                        fragment = new ProfileFragment();
                        break;
                    default: break;
                }
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                return true;
            }
        });
        bottomNavigationView.setSelectedItemId(R.id.action_home);
        // initializes the client
        if (!Places.isInitialized()) {
            // initialize the api with key
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_api_key));
        }
    }








}