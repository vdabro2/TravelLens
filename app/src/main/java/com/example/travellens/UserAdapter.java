package com.example.travellens;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private Context context;
    private List<MyFirebaseUser> firebaseUsers;

    public UserAdapter(Context context, List<MyFirebaseUser> firebaseUsers) {
        this.context = context;
        this.firebaseUsers = firebaseUsers;
    }

    @Override
    public int getItemCount() {
        return firebaseUsers.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message_preview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.ViewHolder holder, int position) {
        MyFirebaseUser user = firebaseUsers.get(position);
        try {
            holder.bind(user);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    // Clean all elements of the recycler
    public void clear() {
        firebaseUsers.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<MyFirebaseUser> list) {
        firebaseUsers.addAll(list);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvUserMessage;
        private ImageView ivPreviewProfileImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserMessage = itemView.findViewById(R.id.tvUserMessage);
            ivPreviewProfileImage = itemView.findViewById(R.id.ivPreviewProfileImage);

        }

        public void bind(MyFirebaseUser user) throws JSONException {
            tvUserMessage.setText(user.username);
            // todo query for user with the userid in parse database
            //query through users in parse that have the id of the firebase user, should be one
            // then this is a ParseUser so you can post.setUser() below and set the image profile
            final ParseUser[] tempUser = new ParseUser[1];
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo(Post.KEY_FIREBASE_USER_ID, user.id);
            query.findInBackground(new FindCallback<ParseUser>() {
                public void done(List<ParseUser> objects, ParseException e) {
                    if (e == null) {
                        // The query was successful.
                        tempUser[0] = objects.get(0);
                        Glide.with(context).load(tempUser[0].getParseFile(Post.KEY_PROFILE_PICTURE)
                                .getUrl()).circleCrop().into(ivPreviewProfileImage);

                    }
                }
            });
            tvUserMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Post tempPost = new Post();
                    tempPost.setUser(tempUser[0]);
                    Intent intent = new Intent(context, MessageActivity.class);
                    intent.putExtra("post", (Parcelable) tempPost);
                    context.startActivity(intent);
                }
            });

        }




    }

}
