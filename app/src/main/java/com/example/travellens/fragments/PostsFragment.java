package com.example.travellens.fragments;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.airbnb.lottie.L;
import com.example.travellens.Filter;
import com.example.travellens.Post;
import com.example.travellens.PostsAdapter;
import com.example.travellens.R;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PostsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PostsFragment extends Fragment {
    private String mParam1;
    private String mParam2;
    private Dialog dialog;
    private ChipGroup cgFilter;
    private List<Post> allPosts;
    private double currLatitude;
    private RecyclerView rvPosts;
    private Place placeToQueryBy;
    private double currLongitude;
    private ImageView ivFilterIcon;
    protected PostsAdapter adapter;
    private Location mCurrentLocation;
    private LocationRequest locationRequest;
    private SwipeRefreshLayout swipeContainer;
    private ShimmerFrameLayout shimmerFrameLayout;
    private List<Post> originalAllPosts = new ArrayList<>();
    private AutocompleteSupportFragment autocompleteFragment;
    private List<String> typesToFilterBy = new ArrayList<>();

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "POSTS_FRAGMENT";
    private final static String KEY_LOCATION = "location";
    private final static List<String> TYPE_LIST = new ArrayList<>(Arrays.asList("AIRPORT",
            "AMUSEMENT_PARK","AQUARIUM", "ART_GALLERY", "BAKERY","BOOK_STORE","CAFE","CAMPGROUND",
            "CAR_RENTAL" , "CITY_HALL", "CLOTHING_STORE", "CONVENIENCE_STORE", "FLORIST", "FOOD", "LIBRARY",
            "LODGING", "MEAL_DELIVERY", "MEAL_TAKEAWAY","MUSEUM",  "PARK", "POINT_OF_INTEREST", "RESTAURANT", "SHOPPING_MALL",
            "SPA", "STORE", "SUBWAY_STATION", "TOURIST_ATTRACTION", "TRAIN_STATION", "TRANSIT_STATION", "TRAVEL_AGENCY", "ZOO"));


    public PostsFragment() {
        placeToQueryBy = null;
    }

    public PostsFragment(Place place) {
        // Required empty public constructor
        placeToQueryBy = place;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PostsFragment.
     */
    public static PostsFragment newInstance(String param1, String param2) {
        PostsFragment fragment = new PostsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_posts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // recyclerview set up
        setUpAdapter(view);
        // start shimmer before loading new data in to recyclerview
        shimmerFrameLayout = view.findViewById(R.id.shimmerLayout);
        ivFilterIcon = view.findViewById(R.id.ivFilterIcon);
        cgFilter = view.findViewById(R.id.cgFilter);
        // asks for location
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        setHasOptionsMenu(true);
        createFragmentFromAPI();

        setUpRefresh();
        createFilter();
        shimmerFrameLayout.startShimmer();
        // choosing whether user wants current or typed location
        if (placeToQueryBy == null) {
            getCurrentLocation(savedInstanceState);
        } else {
            currLatitude = placeToQueryBy.getLatLng().latitude;
            currLongitude = placeToQueryBy.getLatLng().longitude;
            queryPosts(currLatitude, currLongitude);
        }
    }

    private void reloadPostsUsingFilter() {
        dialog.dismiss();
        if (typesToFilterBy.isEmpty()) {
            allPosts = originalAllPosts;
            adapter.clear();
            adapter.addAll(allPosts);
            adapter.notifyDataSetChanged();
            return;
        }
        /* TODO : problem: if shimmer is loading, no posts are in adapter, so if you try to
         filter before adapter gets filled, it breaks */
        allPosts = Filter.getPostsByType(typesToFilterBy, originalAllPosts);
        adapter.clear();
        adapter.addAll(allPosts);
        adapter.notifyDataSetChanged();
    }

    private void createFilter() {

        ivFilterIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUpDialog();
                EditText editText = dialog.findViewById(R.id.etSearch);
                ListView listView = dialog.findViewById(R.id.listOfTypes);
                ImageView ivCloseDialog = dialog.findViewById(R.id.icCloseDialog);
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1,TYPE_LIST);

                listView.setAdapter(arrayAdapter);
                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        arrayAdapter.getFilter().filter(s);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        setUpFilterChip(arrayAdapter, position);
                    }
                });

                ivCloseDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        reloadPostsUsingFilter();
                    }
                });
            }
        });
        ivFilterIcon.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                ivFilterIcon.setVisibility(View.INVISIBLE)

                ;return true;
            }
        });
    }

    private void setUpFilterChip(ArrayAdapter<String> arrayAdapter, int position) {
        Chip chip = new Chip(getContext());
        chip.setText(arrayAdapter.getItem(position));
        /* TODO change UI on chips dynamically */
        // adding to my list so i can use it to filter later
        typesToFilterBy.add(arrayAdapter.getItem(position));

        chip.setCloseIconVisible(true);
        cgFilter.addView(chip);
        chip.setOnCloseIconClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cgFilter.removeView(v);
                typesToFilterBy.remove(chip.getText());
                if (!dialog.isShowing()) {
                    reloadPostsUsingFilter();
                }

            }
        });
    }

    private void setUpDialog() {
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_searchable_spinner);
        dialog.getWindow().setLayout(650,800);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }


    private void setUpRefresh() {
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeContainer.setRefreshing(false);
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    private void setUpAdapter(View view) {
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        rvPosts = view.findViewById(R.id.rvPosts);
        allPosts = new ArrayList<>();
        adapter = new PostsAdapter(getContext(), allPosts);
        rvPosts.setAdapter(adapter);
        StaggeredGridLayoutManager sGrid = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        sGrid.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        rvPosts.setLayoutManager(sGrid);
    }

    private void createFragmentFromAPI() {
        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(getContext());
        // link fragment to layout
        autocompleteFragment = (AutocompleteSupportFragment)
                getActivity().getSupportFragmentManager().findFragmentById(R.id.afSearchAPI);
        autocompleteFragment.getView().setEnabled(true);
        autocompleteFragment.getView().setVisibility(View.VISIBLE);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId()+ ", " + Objects.requireNonNull(place.getLatLng()).latitude+ ", " + place.getLatLng().longitude);
                autocompleteFragment.setHint(place.getName());
                autocompleteFragment.setText("");
                PostsFragment posts_with_loc = new PostsFragment(place);
                AppCompatActivity activity = (AppCompatActivity) getActivity();
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, posts_with_loc).addToBackStack(null).commit();
            }

            @Override
            public void onError(Status status) {
                Log.e(TAG, "An error occurred: " + status);
            }
        });
    }

    private void getCurrentLocation(Bundle savedInstanceState) {
        autocompleteFragment.setHint(getString(R.string.current_location));
        autocompleteFragment.setText("");

        if (savedInstanceState != null && savedInstanceState.keySet().contains(KEY_LOCATION)) {
            mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }
        // built in to android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            FragmentActivity activity = getActivity();
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                if (isGPSEnabled()) {
                    // The fused location provider is a location API in Google Play services
                    LocationServices.getFusedLocationProviderClient(activity)
                            .requestLocationUpdates(locationRequest, new LocationCallback() {
                                @Override
                                public void onLocationResult(@NonNull LocationResult locationResult) {
                                    super.onLocationResult(locationResult);

                                    LocationServices.getFusedLocationProviderClient(activity)
                                            .removeLocationUpdates(this);

                                    if (locationResult != null && locationResult.getLocations().size() >0){
                                        int index = locationResult.getLocations().size() - 1;
                                        double latitude = locationResult.getLocations().get(index).getLatitude();
                                        currLatitude = latitude;
                                        double longitude = locationResult.getLocations().get(index).getLongitude();
                                        currLongitude = longitude;
                                        queryPosts(latitude, longitude);
                                    }
                                }
                            }, Looper.getMainLooper());
                } else {
                    turnOnGPS();
                }

            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }

    }

    private void queryPosts(double latitude, double longitude) {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.addDescendingOrder("createdAt");

        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }
                List<Post> postsFiltered = new ArrayList<>();
                // for debugging purposes let's print every post description to logcat
                for (Post post : posts) {
                    double lonOfPost = post.getDouble(Post.KEY_LONGITUDE);
                    double latOfPost = post.getDouble(Post.KEY_LATITUDE);
                    double distance = distance(latOfPost, lonOfPost, latitude, longitude, "M");

                    if (distance <= 50) {
                        postsFiltered.add(post);
                    }
                }
                // save received posts to list and notify adapter of new data
                originalAllPosts.addAll(postsFiltered);
                allPosts.addAll(postsFiltered);
                adapter.notifyDataSetChanged();
                // stop shimmering when we have the new data
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
            }
        });
    }

    private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        }
        else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            if (unit.equals("K")) {
                dist = dist * 1.609344;
            } else if (unit.equals("N")) {
                dist = dist * 0.8684;
            }
            return (dist);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                if (isGPSEnabled()) {
                } else {
                    turnOnGPS();
                }
            }
        }
    }


    private boolean isGPSEnabled() {
        LocationManager locationManager = null;
        boolean isEnabled = false;
        if (locationManager == null) {
            locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        }
        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isEnabled;
    }


    private void turnOnGPS() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                } catch (ApiException e) {
                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(getActivity(), 2);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            //Device does not have location
                            break;
                    }
                }
            }
        });

    }


}
