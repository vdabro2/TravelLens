package com.example.travellens;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travellens.fragments.DetailFragment;
import com.example.travellens.fragments.ProfileFragment;
import com.example.travellens.fragments.DetailFragment;
import com.example.travellens.fragments.ProfileFragment;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {
    private Context context;
    private List<Post> posts;

    public PostsAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_profile_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);
        try {
            holder.bind(post);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    // Clean all elements of the recycler
    public void clear() {
        posts.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Post> list) {
        posts.addAll(list);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            //tvUsername = itemView.findViewById(R.id.tvUsername);
            ivImage = itemView.findViewById(R.id.ivYourPic);
            //tvDescription = itemView.findViewById(R.id.tvDescription);
            //tvTime = itemView.findViewById(R.id.tvTime);
            //tvUserInDes = itemView.findViewById(R.id.tvUserInDes);
            ///ivProfilePicture = itemView.findViewById(R.id.ivProfilePicture);
            //tvLikes = itemView.findViewById(R.id.tvLikes);
           // ivLikes = itemView.findViewById(R.id.ivLikes);
        }

        public void bind(Post post) throws JSONException {
            ParseFile image = post.getParseFile();
            if (image != null) {
                Glide.with(context).load(image.getUrl()).into(ivImage);
            }

            ivImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    DetailFragment profileFragment = new DetailFragment(post);
                    AppCompatActivity activity = (AppCompatActivity) v.getContext();
                    activity.getSupportFragmentManager().beginTransaction().replace(R.id.flContainer,
                            profileFragment).addToBackStack(null).commit();

                }
            });
        }




    }


}