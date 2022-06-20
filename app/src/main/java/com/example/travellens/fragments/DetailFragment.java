package com.example.travellens.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
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
    Post thePost;


    private TextView tvUsername;
    private TextView tvLikes;
    private ImageView ivImage;
    private TextView tvDescription;
    private TextView tvTime;
    private ImageView ivProfilePicture;
    private TextView tvUserInDes;
    private ImageView ivLikes;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    public DetailFragment() {}
    public DetailFragment(Post post_) {
        // Required empty public constructor
        thePost = post_;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DetailFragment.
     */
    // TODO: Rename and change types and number of parameters
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
        tvUsername = view.findViewById(R.id.tvUsername);
        ivImage = view.findViewById(R.id.ivImage);
        tvDescription = view.findViewById(R.id.tvDescription);
        tvTime = view.findViewById(R.id.tvTime);
        tvUserInDes = view.findViewById(R.id.tvUserInDes);
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        tvLikes = view.findViewById(R.id.tvLikes);
        ivLikes = view.findViewById(R.id.ivLikes);


        tvUsername.setText(thePost.getUser().getUsername());

        tvDescription.setText(thePost.getDescription());
        ParseFile image = thePost.getParseFile();
        if (image != null) {
            Glide.with(getContext()).load(image.getUrl()).into(ivImage);
        }
        tvTime.setText(Post.calculateTimeAgo(thePost.getCreatedAt()));

        ParseFile profilepic = thePost.getUser().getParseFile("profilePicture");
        if (profilepic != null) {
            Glide.with(getContext()).load(profilepic.getUrl()).circleCrop().into(ivProfilePicture);
        }
        tvUserInDes.setText(thePost.getUser().getUsername());

    }

}