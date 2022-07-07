package com.example.travellens;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.ablanco.zoomy.DoubleTapListener;
import com.ablanco.zoomy.LongPressListener;
import com.ablanco.zoomy.TapListener;
import com.ablanco.zoomy.ZoomListener;
import com.ablanco.zoomy.Zoomy;
import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.example.travellens.fragments.ProfileFragment;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

public class DetailActivity extends AppCompatActivity {
    private Post thePost;
    private int likeCount;
    private TextView tvTime;
    private TextView tvLikes;
    private ImageView ivLikes;
    private BlurView blurView;
    private ImageView ivImage;
    private RatingBar rbRating;
    private TextView tvLocation;
    private boolean blurClicked;
    private TextView tvUserInDes;
    private TextView tvDescription;
    private ImageView ivProfilePicture;
    private LottieAnimationView hearts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        blurClicked = false;
        tvTime = findViewById(R.id.tvTime);
        tvLikes = findViewById(R.id.tvLikes);
        ivLikes = findViewById(R.id.ivLikes);
        ivImage = findViewById(R.id.ivImage);
        rbRating = findViewById(R.id.rbRating);
        hearts = findViewById(R.id.animHearts);
        blurView = findViewById(R.id.blurView);
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
                try {
                    goToProfile();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
        tvUserInDes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    goToProfile();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
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
        //rbRating.setRating((float) thePost.getDouble(Post.KEY_RATING));

        ObjectAnimator anim = ObjectAnimator.ofFloat(rbRating, "rating", 0f, (float) thePost.getDouble(Post.KEY_RATING));
        anim.setDuration(1000);
        anim.start();

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
        setUpBlurView();
    }

    private void setUpBlurView() {
        View decorView = getWindow().getDecorView();
        ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);
        Drawable windowBackground = decorView.getBackground();

        blurView.setupWith(rootView)
                .setFrameClearDrawable(windowBackground) // Optional
                .setBlurAlgorithm(new RenderScriptBlur(this))
                .setBlurRadius(10f)
                .setBlurAutoUpdate(true);

        final View root = findViewById(R.id.root);
        Zoomy.Builder builder = new Zoomy.Builder(this).target(blurView);
        builder.tapListener(new TapListener() {
                    @Override
                    public void onTap(View v) {
                        if (blurClicked) {
                            v.animate().translationX(0).translationY(0)
                                    .setInterpolator(new AccelerateInterpolator()).setDuration(500);
                        } else {
                            v.animate().translationX(0)
                                    .translationY(-1 * (root.getHeight() - v.getHeight()))
                                    .setInterpolator(new AccelerateInterpolator())
                                    .setDuration(500);
                        }
                        blurClicked = !blurClicked;
                    }
                })
                .longPressListener(new LongPressListener() {
                    @Override
                    public void onLongPress(View v) {
                        //View long pressed, do stuff
                    }
                }).doubleTapListener(new DoubleTapListener() {
                    @Override
                    public void onDoubleTap(View v) {
                        //View double tapped, do stuff
                        likeOrUnlike();
                    }
                })
                .zoomListener(new ZoomListener() {
                    @Override
                    public void onViewStartedZooming(View view) {
                        //View started zooming
                    }

                    @Override
                    public void onViewEndedZooming(View view) {
                        //View ended zooming

                    }
                });

        builder.register();
    }

    private void goToProfile() throws ParseException {
        // send intent to main so main knows to show user profile
        Intent intent = new Intent(this, FeedMainActivity.class);
        intent.putExtra("userId", thePost.getUser().fetchIfNeeded().getObjectId());
        startActivity(intent);
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
                        ivLikes.setImageResource(R.drawable.whiteheart);
                        // update how many likes the post has
                        likeCount = likeCount - 1;

                        tvLikes.setText(getResources().getQuantityString(R.plurals.likes, likeCount, likeCount));

                    }
                } else if (e != null){
                    Log.e("Detail Fragment: ", e.toString());
                } else {
                    // make a new like object
                    hearts.setProgress(0);
                    hearts.pauseAnimation();
                    hearts.playAnimation();
                    Likes like = new Likes();
                    like.setUser(ParseUser.getCurrentUser());
                    like.setPost(thePost);
                    // saving it
                    like.saveInBackground();
                    // set image to like heart
                    ivLikes.setImageResource(R.drawable.pinkheart);
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
                    ivLikes.setImageResource(R.drawable.whiteheart);

                } else {
                    // the post is liked by the current user
                    ivLikes.setImageResource(R.drawable.pinkheart);
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