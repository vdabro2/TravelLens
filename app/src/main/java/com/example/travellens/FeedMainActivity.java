package com.example.travellens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.travellens.fragments.ComposeFragment;
import com.example.travellens.fragments.DetailFragment;
import com.example.travellens.fragments.ProfileFragment;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;

import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toolbar;

import com.example.travellens.fragments.PostsFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FeedMainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    Toolbar toolbar;
    SearchView svSearchLocation;
    ImageView ivSearch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_main);

        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        setActionBar(tb);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final FragmentManager fragmentManager = getSupportFragmentManager();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        toolbar = findViewById(R.id.toolbar);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = new Fragment();
                String tag = "";
                switch (item.getItemId()) {
                    case R.id.action_post:
                        tag = "post";
                        fragment = new ComposeFragment();
                        break;

                    case R.id.action_home:
                        tag = "home";
                        fragment = new PostsFragment();
                        // do something here
                        break;
                    case R.id.action_prof:
                        tag = "profile";
                        // do something here

                        fragment = new ProfileFragment();
                        break;
                    default: break;
                }
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment, tag).commit();
                return true;
            }
        });
        bottomNavigationView.setSelectedItemId(R.id.action_home);
/*
        if (!Places.isInitialized()) {
            // initialize the api with key
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_api_key));
        }
        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(getApplicationContext());
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                    Log.i("TAG", "Place: " + place.getName() + ", " + place.getId()+ ", " + Objects.requireNonNull(place.getLatLng()).latitude+ ", " + place.getLatLng().longitude);
                    PostsFragment posts_with_loc = new PostsFragment(place);
                    AppCompatActivity activity = (AppCompatActivity) FeedMainActivity.this;
                    activity.getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, posts_with_loc).addToBackStack(null).commit();
            }

            @Override
            public void onError(Status status) {
                Log.i("TAG", "An error occurred: " + status);
            }
        });*/
    }








}