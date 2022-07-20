package com.example.travellens.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.example.travellens.EditProfileActivity;
import com.example.travellens.Likes;
import com.example.travellens.Post;
import com.example.travellens.ProfileAdapter;
import com.example.travellens.R;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.material.tabs.TabLayout;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    private String mParam1;
    private String mParam2;
    private TextView tvBio;
    private TextView tvUserName;
    private TextView tvRealName;
    private TabLayout tabLayout;
    private RecyclerView rvPosts;
    private ParseUser userProfile;
    protected List<Post> allPosts;
    protected ProfileAdapter adapter;
    private ImageView ivProfilePicture;
    private ShimmerFrameLayout shimmerFrameLayout;
    private static final int READY_TO_UPDATE = 12;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "PROFILE FRAGMENT";

    public ProfileFragment() {
        userProfile = ParseUser.getCurrentUser();
    }
    public ProfileFragment(ParseUser user) {
        userProfile = user;
    }

    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvBio = view.findViewById(R.id.tvBio);
        //tabSettings = view.findViewById(R.id.tabSettings);
        rvPosts = view.findViewById(R.id.rvGrid);
        tvRealName = view.findViewById(R.id.tvRealName);
        tvUserName = view.findViewById(R.id.tvUserName);
        ivProfilePicture = view.findViewById(R.id.ivProfilePic2);
        tabLayout = view.findViewById(R.id.tabOptions);

        setUpAdapter();
        attachProfileElements();
        allowEditProfile();

        // set up invisible autocomplete fragment
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getActivity().getSupportFragmentManager().findFragmentById(R.id.afSearchAPI);
        autocompleteFragment.getView().setEnabled(false);
        autocompleteFragment.getView().setVisibility(View.INVISIBLE);
        // start shimmer before loading new data in to recyclerview
        shimmerFrameLayout = view.findViewById(R.id.shimmerLayout);
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmer();

        queryPosts();


    }

    private void setUpAdapter() {
        // set up recyclerview
        allPosts = new ArrayList<>();
        adapter = new ProfileAdapter(getContext(), allPosts);
        rvPosts.setAdapter(adapter);
        StaggeredGridLayoutManager sGrid = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        sGrid.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        rvPosts.setLayoutManager(sGrid);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // set the layout manager on the recycler view
        if (requestCode == READY_TO_UPDATE) {
            attachProfileElements();
        }
    }

    private void attachProfileElements() {
        rvPosts.setLayoutManager(new GridLayoutManager(getContext(), 3));
        tvUserName.setText(userProfile.getUsername());
        tvBio.setText(userProfile.getString(Post.KEY_BIOGRAPHY));
        tvRealName.setText(userProfile.getString(Post.KEY_NAME));

        ParseFile profilepic = userProfile.getParseFile(Post.KEY_PROFILE_PICTURE);
        if (profilepic != null) {
            Glide.with(getContext()).load(profilepic.getUrl()).circleCrop().into(ivProfilePicture);
        }
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                checkTabPosition(tab);
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                checkTabPosition(tab);
            }
        });

    }

    private void checkTabPosition(TabLayout.Tab tab) {
        if (tab.getPosition() == 0) {
            setUpAdapter();
            attachProfileElements();
            queryPosts();
        }
        if (tab.getPosition() == 1) {
            adapter.clear();
            // start shimmer before loading new data in to recyclerview
            shimmerFrameLayout.startShimmer();
            shimmerFrameLayout.setVisibility(View.VISIBLE);
            querySavedPosts();
        }
        if (tab.getPosition() == 2) {
            Intent intent = new Intent(getContext(), EditProfileActivity.class);
            startActivityForResult(intent, READY_TO_UPDATE);
        }
    }

    private void querySavedPosts() {
        ParseQuery<Likes> query = ParseQuery.getQuery(Likes.class);
        query.include(Likes.KEY_POST);
        query.include(Likes.KEY_USER);
        query.whereEqualTo(Likes.KEY_USER, userProfile);

        query.addDescendingOrder("createdAt");
        query.findInBackground(new FindCallback<Likes>() {
            @Override
            public void done(List<Likes> results, ParseException e) {
                if (e == null) {
                    List<Post> posts = new ArrayList<>();
                    for (Likes like : results) {
                        posts.add((Post) like.getPost());

                    }
                    allPosts.addAll(posts);
                    adapter.notifyDataSetChanged();
                    // stop shimmering when we have the new data
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                }
            }
        });
    }


    private void allowEditProfile() {
        if (!userProfile.getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
            // remove tab for viewing settings
            tabLayout.removeTabAt(2);
        }
    }


    protected void queryPosts() {
        // specify what type of data we want to query - Post.class
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        // include data referred by user key
        query.include(Post.KEY_USER);
        query.whereEqualTo(Post.KEY_USER, userProfile);
        // limit query to latest 20 items
        query.setLimit(20);
        // order posts by creation date (newest first)
        query.addDescendingOrder("createdAt");
        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }
                // save received posts to list and notify adapter of new data
                allPosts.addAll(posts);
                adapter.notifyDataSetChanged();
                // stop shimmering when we have the new data
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
            }
        });
    }
}