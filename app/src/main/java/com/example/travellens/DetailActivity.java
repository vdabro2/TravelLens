package com.example.travellens;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.travellens.fragments.ProfileFragment;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class DetailActivity extends AppCompatActivity {
    private Post thePost;
    private int likeCount;
    private TextView tvTime;
    private TextView tvLikes;
    private ImageView ivLikes;
    private ImageView ivImage;
    private TextView tvLocation;
    private RatingBar rbRating;
    private TextView tvUserInDes;
    private TextView tvDescription;
    private ImageView ivProfilePicture;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        tvTime = findViewById(R.id.tvTime);
        tvLikes = findViewById(R.id.tvLikes);
        ivLikes = findViewById(R.id.ivLikes);
        ivImage = findViewById(R.id.ivImage);
        rbRating = findViewById(R.id.rbRating);
        tvLocation = findViewById(R.id.tvLocation);
        tvUserInDes = findViewById(R.id.tvUsernameDetail);
        tvDescription = findViewById(R.id.tvDescription);
        ivProfilePicture = findViewById(R.id.ivProfilePicture);

        // get intent with post object
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        thePost = (Post)bundle.getSerializable("post");

        setAllDetails();
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

    private void setAllDetails() {
        // description text
        tvDescription.setText(thePost.getDescription());
        // set relative time on layout
        tvTime.setText(Post.calculateTimeAgo(thePost.getCreatedAt(), getApplicationContext()));
        // set second username text
        try {
            tvUserInDes.setText(thePost.getUser().fetchIfNeeded().getUsername());
        } catch (ParseException e) {
            Log.e("Detail Fragment: ", e.toString());
        }
        // set name of place aka location name
        tvLocation.setText(thePost.getString(Post.KEY_PLACE_NAME));
        // set rating according to database
        rbRating.setRating((float) thePost.getDouble(Post.KEY_RATING));

        // post image load into imageview using glide
        ParseFile image = thePost.getParseFile();
        if (image != null) {
            Glide.with(this).load(image.getUrl()).into(ivImage);
        }
        // post profile image into imageview using glide
        ParseFile profilepic = thePost.getUser().getParseFile(Post.KEY_PROFILE_PICTURE);
        if (profilepic != null) {
            Glide.with(this).load(profilepic.getUrl()).circleCrop().into(ivProfilePicture);
        }
    }

    private void goToProfile() {
        ProfileFragment profileFragment = new ProfileFragment(thePost.getUser());
        AppCompatActivity activity = (AppCompatActivity)this;
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

                        tvLikes.setText(getResources().getQuantityString(R.plurals.likes, likeCount, likeCount));

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
                    tvLikes.setText(getResources().getQuantityString(R.plurals.likes, likeCount, likeCount));

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
                tvLikes.setText(getResources().getQuantityString(R.plurals.likes, likeCount, likeCount));

            }
        });
    }

}