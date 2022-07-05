package com.example.travellens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toolbar;

import com.example.travellens.fragments.ComposeFragment;
import com.example.travellens.fragments.ProfileFragment;
import com.google.android.libraries.places.api.Places;

import com.example.travellens.fragments.PostsFragment;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class FeedMainActivity extends AppCompatActivity {
    private ImageView ivIcon;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_main);
        ivIcon = findViewById(R.id.ivIconShowOnProf);
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                this.getSupportFragmentManager().findFragmentById(R.id.afSearchAPI);
        autocompleteFragment.getView().setEnabled(true);
        autocompleteFragment.getView().setVisibility(View.VISIBLE);
        ((EditText)autocompleteFragment.getView().findViewById(com.google.android.libraries.places.R.id.places_autocomplete_search_input)).setHintTextColor(getResources().getColor(R.color.silver));
        final FragmentManager fragmentManager = getSupportFragmentManager();
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        sendToProfileIfNeeded();


        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = new Fragment();
                switch (item.getItemId()) {
                    case R.id.action_post:
                        ivIcon.setVisibility(View.INVISIBLE);
                        fragment = new ComposeFragment();
                        break;
                    case R.id.action_home:
                        ivIcon.setVisibility(View.INVISIBLE);
                        fragment = new PostsFragment();
                        break;
                    case R.id.action_prof:
                        ivIcon.setVisibility(View.VISIBLE);
                        fragment = new ProfileFragment();
                        break;
                    default: break;
                }
                fragmentManager.beginTransaction().setCustomAnimations(
                        R.anim.slide_in,  // enter
                        R.anim.fade_out,  // exit
                        R.anim.fade_in,   // popEnter
                        R.anim.slide_out  // popExit
                ).replace(R.id.flContainer, fragment).commit();
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

    private void sendToProfileIfNeeded() {
        Intent intent = getIntent();
        if (intent.getStringExtra("userId") != null) {
            Log.e(" userId ", intent.getStringExtra("userId"));

            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.setLimit(1);
            // Retrieve the object by id
            query.getInBackground(intent.getStringExtra("userId"), new GetCallback<ParseUser>() {
                @Override
                public void done(ParseUser object, ParseException e) {
                    if (e == null) {
                        Log.e(" userId 1", object.getObjectId());
                        ivIcon.setVisibility(View.VISIBLE);
                        getSupportFragmentManager().beginTransaction().replace(R.id.flContainer,
                                new ProfileFragment(object)).commit();

                    } else {
                        Log.e(" userId error", e.toString());
                    }
                }
            });
        }
    }


}