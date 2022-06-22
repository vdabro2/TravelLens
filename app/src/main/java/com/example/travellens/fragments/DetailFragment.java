package com.example.travellens.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.travellens.FeedMainActivity;
import com.example.travellens.Post;
import com.example.travellens.R;
import com.example.travellens.Post;
import com.example.travellens.R;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailFragment extends Fragment {
    private Post thePost;
    private String mParam1;
    private String mParam2;
    private TextView tvTime;
    private TextView tvLikes;
    private ImageView ivLikes;
    private ImageView ivImage;
    private TextView tvUsername;
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
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.autocomplete_fragment:
                // do something
                return true;
            default:
                return false;
        }
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
        tvUsername = view.findViewById(R.id.tvUsername);
        tvUserInDes = view.findViewById(R.id.tvUserInDes);
        tvDescription = view.findViewById(R.id.tvDescription);
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);

        // username text
        tvUsername.setText(thePost.getUser().getUsername());
        // description text
        tvDescription.setText(thePost.getDescription());
        // set relative time on layout
        tvTime.setText(Post.calculateTimeAgo(thePost.getCreatedAt()));
        // set second username text
        tvUserInDes.setText(thePost.getUser().getUsername());

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
                ProfileFragment profileFragment = new ProfileFragment(thePost.getUser());
                AppCompatActivity activity = (AppCompatActivity)getActivity();
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, profileFragment).addToBackStack(null).commit();
            }
        });
        tvUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileFragment profileFragment = new ProfileFragment(thePost.getUser());
                AppCompatActivity activity = (AppCompatActivity)getActivity();
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, profileFragment).addToBackStack(null).commit();
            }
        });
    }

}