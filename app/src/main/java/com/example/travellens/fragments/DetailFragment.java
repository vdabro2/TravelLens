package com.example.travellens.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.travellens.FeedMainActivity;
import com.example.travellens.Likes;
import com.example.travellens.Post;
import com.example.travellens.R;
import com.example.travellens.Post;
import com.example.travellens.R;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.maps.errors.ApiException;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailFragment extends Fragment {
    private Post thePost;
    private int likeCount;
    private String mParam1;
    private String mParam2;
    private TextView tvTime;
    private TextView tvLikes;
    private ImageView ivLikes;
    private ImageView ivImage;
    private TextView tvLocation;
    private RatingBar rbRating;
    private TextView tvUserInDes;
    private TextView tvDescription;
    private ImageView ivProfilePicture;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    public DetailFragment() {}
    public DetailFragment(Post post_) {
        setHasOptionsMenu(true);
        thePost = post_;
    }

    public static DetailFragment newInstance(String param1, String param2) {
        DetailFragment fragment = new DetailFragment();
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
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvTime = view.findViewById(R.id.tvTime);
        tvLikes = view.findViewById(R.id.tvLikes);
        ivLikes = view.findViewById(R.id.ivLikes);
        ivImage = view.findViewById(R.id.ivImage);
        rbRating = view.findViewById(R.id.rbRating);
        tvLocation = view.findViewById(R.id.tvLocation);
        tvUserInDes = view.findViewById(R.id.tvUserInDes);
        tvDescription = view.findViewById(R.id.tvDescription);
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        // turn off autocomplete fragment on this page
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getActivity().getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.getView().setEnabled(false);
        autocompleteFragment.getView().setVisibility(View.INVISIBLE);

        // description text
        tvDescription.setText(thePost.getDescription());
        // set relative time on layout
        tvTime.setText(Post.calculateTimeAgo(thePost.getCreatedAt()));
        // set second username text
        try {
            tvUserInDes.setText(thePost.getUser().fetchIfNeeded().getUsername());
        } catch (ParseException e) {
            Log.e("Detail Fragment: ", e.toString());
        }
        // set name of place aka location name
        tvLocation.setText(thePost.getString("placeName"));
        // set rating according to database
        rbRating.setRating((float) thePost.getDouble("rating"));

        // post image load into imageview using glide
        ParseFile image = thePost.getParseFile();
        if (image != null) {
            Glide.with(getContext()).load(image.getUrl()).into(ivImage);
        }
        // post profile image into imageview using glide
        ParseFile profilepic = thePost.getUser().getParseFile("profilePicture");
        if (profilepic != null) {
            Glide.with(getContext()).load(profilepic.getUrl()).circleCrop().into(ivProfilePicture);
        }

        // if you click the profile image or username, you get sent to the users profile
        ivProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToProfile();
            }
        });
        tvUserInDes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToProfile();
            }
        });

        // checks whether user has previously liked this post
        queryIfLiked();
        queryHowManyLikes();
        ivLikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likeOrUnlike();
            }
        });
    }

    private void goToProfile() {
        ProfileFragment profileFragment = new ProfileFragment(thePost.getUser());
        AppCompatActivity activity = (AppCompatActivity)getActivity();
        activity.getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, profileFragment).addToBackStack(null).commit();
    }

    private void likeOrUnlike() {
        ParseQuery<Likes> query = ParseQuery.getQuery(Likes.class);
        query.whereEqualTo(Likes.KEY_POST, thePost);
        query.whereEqualTo(Likes.KEY_USER, ParseUser.getCurrentUser());
        query.setLimit(1);
        query.findInBackground(new FindCallback<Likes>() {
            @Override
            public void done(List<Likes> results, ParseException e) {
                if (e == null && !results.isEmpty()) {
                    for (Likes like : results) {
                        // deleted from likes
                        like.deleteInBackground();
                        // set image to unlike heart
                        ivLikes.setImageResource(R.drawable.ufi_heart);
                        // update how many likes the post has
                        likeCount = likeCount - 1;

                        if (likeCount == 0) {
                            // no likes, shouldnt have number
                        } else if (likeCount == 1) {
                            tvLikes.setText(likeCount + " like");
                        } else {
                            tvLikes.setText(likeCount + " likes");}
                    }
                } else if (e != null){
                    Log.e("Detail Fragment: ", e.toString());
                } else {
                    // make a new like object
                    Likes like = new Likes();
                    like.setUser(ParseUser.getCurrentUser());
                    like.setPost(thePost);
                    // saving it
                    like.saveInBackground();
                    // set image to like heart
                    ivLikes.setImageResource(R.drawable.img_2);
                    // update how many likes the post has
                    likeCount = likeCount + 1;
                    if (likeCount == 0) {
                        // no likes, shouldnt have number
                    } else if (likeCount == 1) {
                        tvLikes.setText(likeCount + " like");
                    } else {
                        tvLikes.setText(likeCount + " likes");
                    }
                }
            }
        });
    }


    private void queryIfLiked() {
        ParseQuery<Likes> query = ParseQuery.getQuery(Likes.class);
        query.whereEqualTo(Likes.KEY_POST, thePost);
        query.whereEqualTo(Likes.KEY_USER, ParseUser.getCurrentUser());

        query.findInBackground(new FindCallback<Likes>() {
            @Override
            public void done(List<Likes> likesList, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e("FEED", "Issue with getting likes", e);
                    return;
                }
                if (likesList.isEmpty()) {
                    // the post is not liked by the current user
                    ivLikes.setImageResource(R.drawable.ufi_heart);

                } else {
                    // the post is liked by the current user
                    ivLikes.setImageResource(R.drawable.img_2);
                }
            }
        });
    }


    private void queryHowManyLikes() {
        ParseQuery<Likes> query = ParseQuery.getQuery(Likes.class);
        query.whereEqualTo(Likes.KEY_POST, thePost);

        query.countInBackground(new CountCallback() {
            @Override
            public void done(int count, ParseException e) {
                if (e != null) {
                    Log.e("FEED", "Issue with getting likes", e);
                    return;
                }
                likeCount = count;
                if (count == 0) {
                    // no likes, shouldnt have number
                } else if (count == 1) {
                    tvLikes.setText(count + " like");
                } else {
                    tvLikes.setText(count + " likes");
                }
            }
        });
    }

}