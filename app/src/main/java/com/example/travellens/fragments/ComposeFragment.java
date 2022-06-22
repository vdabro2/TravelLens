package com.example.travellens.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.travellens.FeedMainActivity;
import com.example.travellens.Post;
import com.example.travellens.R;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ComposeFragment extends Fragment {
    File photoFile;
    Button bSubmit;
    ImageView ivPic;
    Place placeInPost;
    private String mParam1;
    private String mParam2;
    EditText etDescription;
    ProgressBar progressBar;
    boolean fromGal = false;
    ParseFile photoFileFromGal;
    FloatingActionButton bCamera;
    FloatingActionButton bGallery;
    public String photoFileName = "photo.jpg";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public final static int REQUEST_CODE_GALLERY = 43;
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;


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
        // all var declarations
        ivPic = view.findViewById(R.id.ivPic);
        bSubmit = view.findViewById(R.id.bSubmit);
        bCamera = view.findViewById(R.id.bCamera);
        bGallery = view.findViewById(R.id.bGallery);
        etDescription = view.findViewById(R.id.etDescription);
        // this bar is shown while post is posting to database
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        placeInPost = null;
        bSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String des = etDescription.getText().toString();
                if (des.isEmpty()) {
                    Toast.makeText(getContext(), "Cant be empty", Toast.LENGTH_SHORT).show();
                }
                if (photoFile == null || ivPic.getDrawable() == null) {
                    Toast.makeText(getContext(), "Theres no photo", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (placeInPost == null) {
                    Toast.makeText(getContext(), "Location can't be empty", Toast.LENGTH_SHORT).show();
                }
                ParseUser user  = ParseUser.getCurrentUser();
                savePost(des, user);
                Intent i = new Intent(getContext(), FeedMainActivity.class); // TODO maybe send to the posts location PostsFrag(postlocation)
                startActivity(i);
            }
        });

        bCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCamera();
            }
        });

        bGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                photoFile = getPhotoFileUri(photoFileName);
                Uri fileProvider = FileProvider.getUriForFile(getContext(), "com.codepath.fileprovider", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
                intent.setType("image/'");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                fromGal = true;
                startActivityForResult(Intent.createChooser(intent, "Pick an image"), REQUEST_CODE_GALLERY);
            }
        });

        setHasOptionsMenu(true);
        if (!Places.isInitialized()) {
            // initialize the api with key
            Places.initialize(getContext(), getString(R.string.google_maps_api_key));
        }
        PlacesClient placesClient = Places.createClient(getContext());
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getActivity().getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.i("TAG", "SELECT IN COMPOSE FRAGMENT");
                // save this place to the post info
                placeInPost = place;
            }

            @Override
            public void onError(Status status) {
                Log.i("TAG", "An error occurred: " + status);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == getActivity().RESULT_OK) {
                // by this point we have the camera photo on disk
                fromGal = false;
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                // RESIZE BITMAP, see section below
                // Load the taken image into a preview

                ivPic.setImageBitmap(takenImage);
            } else { // Result was a failure
                Toast.makeText(getContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CODE_GALLERY && resultCode == getActivity().RESULT_OK) {
            fromGal = true;
            Uri image = data.getData();

            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), image);
                Glide.with(this).load(image).into(ivPic);
                photoFileFromGal = conversionBitmapParseFile(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public ParseFile conversionBitmapParseFile(Bitmap imageBitmap){
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
        byte[] imageByte = byteArrayOutputStream.toByteArray();
        ParseFile parseFile = new ParseFile("image_file.png",imageByte);
        return parseFile;
    }

    private void launchCamera() {
        fromGal = false;
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(getContext(), "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    private File getPhotoFileUri(String photoFileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d("compose", "failed to create directory");
        }

        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + photoFileName);

        return file;
    }

    private void savePost(String des, ParseUser user) {
        Post post = new Post();
        post.setDescription(des);
        if (fromGal == true) {
            post.setImage(photoFileFromGal);
        } else {
            post.setImage(new ParseFile(photoFile));
        }
        post.put(Post.KEY_LATITUDE, placeInPost.getLatLng().latitude);
        post.put(Post.KEY_LONGITUDE, placeInPost.getLatLng().longitude);
        post.put("placeId", placeInPost.getId());
        // ask for rating
        post.setUser(user);
        progressBar.setVisibility(View.VISIBLE);
        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null){
                    Log.e("EROOOR", e.toString());
                    return;
                }
                progressBar.setVisibility(View.GONE);
                etDescription.setText("");
                ivPic.setImageResource(0);
            }
        });
    }

}