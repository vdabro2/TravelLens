package com.example.travellens.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travellens.Post;
import com.example.travellens.ProfileAdapter;
import com.example.travellens.R;
import com.example.travellens.Post;
import com.example.travellens.ProfileAdapter;
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
    private Button bEdit;
    private String mParam1;
    private String mParam2;
    private Button bLogout;
    private TextView tvBio;
    private TextView tvUserName;
    private TextView tvRealName;
    private RecyclerView rvPosts;
    private ParseUser userProfile;
    protected List<Post> allPosts;
    protected ProfileAdapter adapter;
    private ImageView ivProfilePicture;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

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
        bEdit = view.findViewById(R.id.bEdit);
        tvBio = view.findViewById(R.id.tvBio);
        rvPosts = view.findViewById(R.id.rvGrid);
        tvRealName = view.findViewById(R.id.tvRealName);
        tvUserName = view.findViewById(R.id.tvUserName);
        ivProfilePicture = view.findViewById(R.id.ivPP2);

        allPosts = new ArrayList<>();
        adapter = new ProfileAdapter(getContext(), allPosts);
        rvPosts.setAdapter(adapter);

        // set the layout manager on the recycler view
        rvPosts.setLayoutManager(new GridLayoutManager(getContext(), 3)); // TODO make staggered
        tvUserName.setText(userProfile.getUsername());
        tvBio.setText(userProfile.getString("biography"));
        tvRealName.setText(userProfile.getString("name"));

        ParseFile profilepic = userProfile.getParseFile("profilePicture");
        if (profilepic != null) {
            Glide.with(getContext()).load(profilepic.getUrl()).circleCrop().into(ivProfilePicture);
        }

        bEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new EditProfileFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.flContainer, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
        queryPosts();
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
                    Log.e("FEED", "Issue with getting posts", e);
                    return;
                }

                // for debugging purposes let's print every post description to logcat
                for (Post post : posts) {
                    Log.i("FEED", "Post: " + post.getDescription() + ", username: " + post.getUser().getUsername());
                }

                // save received posts to list and notify adapter of new data
                allPosts.addAll(posts);
                adapter.notifyDataSetChanged();
            }
        });
    }
}