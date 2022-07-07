package com.example.travellens.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.travellens.CameraHelper;
import com.example.travellens.CameraActivity;
import com.example.travellens.Post;
import com.example.travellens.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.errors.ApiException;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ComposeFragment extends Fragment {
    private String mParam1;
    private String mParam2;
    private File photoFile;
    private Button bSubmit;
    private Button bCamera;
    private ImageView ivPic;
    private Place placeInPost;
    private RatingBar rbRating;
    private CameraHelper camera;
    private EditText etDescription;
    private ChipGroup cgRecommended;
    private ProgressBar progressBar;
    private PlacesClient placesClient;
    public String photoFileName = "photo.jpg";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "COMPOSE_FRAGMENT";
    public static final int RESULT_CODE_FROM_CAMERA = 10;
    // list of types that I want location recommendations for the user
    private List<String> typeList = new ArrayList<>(Arrays.asList("POINT_OF_INTEREST", "FOOD",
            "CAFE","TRANSIT_STATION", "TOURIST_ATTRACTION", "PARK", "MUSEUM"));

    public ComposeFragment() {}


    public static ComposeFragment newInstance(String param1, String param2) {
        ComposeFragment fragment = new ComposeFragment();
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
        return inflater.inflate(R.layout.fragment_compose, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // all var declarations
        placeInPost = null;
        camera = new CameraHelper();
        ivPic = view.findViewById(R.id.ivPic);
        bSubmit = view.findViewById(R.id.bSubmit);
        bCamera = view.findViewById(R.id.bCamera);
        placesClient = Places.createClient(getContext());
        etDescription = view.findViewById(R.id.etDescription);
        rbRating = view.findViewById(R.id.rbRatingCompose);
        cgRecommended = view.findViewById(R.id.cgRecomended);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        // call method that creates recommended places based on user location
        populateChipsWithLocation();

        // checking conditions and then sharing post
        bSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkConditionsOfPost();
                ParseUser user  = ParseUser.getCurrentUser();
                savePost(etDescription.getText().toString(), user);
                goLocationTimeline();
            }
        });

        // launches camera on click
        bCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), CameraActivity.class);
                startActivityForResult(i, RESULT_CODE_FROM_CAMERA);
            }
        });

        // allows this fragments menu to behave differently than in main
        setHasOptionsMenu(true);
        // method creates the autocomplete fragment
        createFragmentFromAPI();

    }

    private void checkConditionsOfPost() {
        String description = etDescription.getText().toString();
        if (description.isEmpty() || photoFile == null || ivPic.getDrawable() == null
                || placeInPost == null || rbRating.getRating() <= 0) {
            Toast.makeText(getContext(), R.string.fill_everything_out_warning, Toast.LENGTH_SHORT).show();
            return;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_CODE_FROM_CAMERA) {
            if (resultCode == getActivity().RESULT_OK) {
                Uri selectedImage = data.getData();
                photoFile = camera.uriToFile(getActivity(), selectedImage, photoFileName, getContext());
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                ivPic.setImageBitmap(takenImage);
            }
        }
    }
    private void populateChipsWithLocation() {

        List<Place.Field> placeFields = (Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.TYPES));

        // Use the builder to create a FindCurrentPlaceRequest.
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);

        // Call findCurrentPlace and handle the response (first check that the user has granted permission).
        Context context = getContext();
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);
            placeResponse.addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    FindCurrentPlaceResponse response = task.getResult();
                    for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                        boolean var = placeLikelihood.getPlace().getTypes().stream().anyMatch(element -> typeList.contains(element));
                        if (var == false) {
                            Chip chip = new Chip(context);
                            chip.setText(placeLikelihood.getPlace().getName());
                            chip.setCloseIconVisible(false);
                            cgRecommended.addView(chip);
                            chip.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    placeInPost = placeLikelihood.getPlace();
                                    AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                                            getActivity().getSupportFragmentManager().findFragmentById(R.id.afSearchAPI);
                                    autocompleteFragment.setText(placeInPost.getName());
                                }
                            });
                        }
                    }
                } else {
                    Exception exception = task.getException();
                    if (exception instanceof ApiException) {
                        ApiException apiException = (ApiException) exception;
                        Log.e(TAG, "Place not found: " + apiException);
                    }
                }
            });
        }

    }

    private void goLocationTimeline() {
        PostsFragment postsFragment = new PostsFragment(placeInPost);
        AppCompatActivity activity = (AppCompatActivity) getContext();
        activity.getSupportFragmentManager().beginTransaction().replace(R.id.flContainer,
                postsFragment).addToBackStack(null).commit();
    }

    private void createFragmentFromAPI() {
        // link fragment to layout
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getActivity().getSupportFragmentManager().findFragmentById(R.id.afSearchAPI);
        autocompleteFragment.getView().setEnabled(true);
        autocompleteFragment.getView().setVisibility(View.VISIBLE);
        autocompleteFragment.setHint(getString(R.string.set_post_location));

        // asks for the info I need to store in database
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // save this place to the post info
                placeInPost = place;
            }
            @Override
            public void onError(Status status) {
                Log.e(TAG, "An error occurred: " + status);
            }
        });
    }


    private void savePost(String description, ParseUser user) {
        Post post = new Post();
        post.setDescription(description);
        post.setImage(new ParseFile(photoFile));
        post.put(Post.KEY_LATITUDE, placeInPost.getLatLng().latitude);
        post.put(Post.KEY_RATING, rbRating.getRating());
        post.put(Post.KEY_PLACE_NAME, placeInPost.getName());
        post.put(Post.KEY_LONGITUDE, placeInPost.getLatLng().longitude);
        post.put(Post.KEY_PLACE_ID, placeInPost.getId());
        post.setUser(user);
        progressBar.setVisibility(View.VISIBLE);
        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null){
                    return;
                }
                progressBar.setVisibility(View.GONE);
                etDescription.setText("");
                ivPic.setImageResource(0);
            }
        });
    }

}