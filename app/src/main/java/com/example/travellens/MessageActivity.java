package com.example.travellens;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class MessageActivity extends AppCompatActivity {
    private Post post;
    private Intent intent;
    private FirebaseUser user;
    private ImageView ivReceiverPicture;
    private TextView tvReceiverUsername;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        ivReceiverPicture = findViewById(R.id.ivReceiverPicture);
        tvReceiverUsername = findViewById(R.id.tvReceiverUsername);

        // get intent with the user info of who youre sending to
        intent = getIntent();
        intent.getExtras().getParcelable("post");
        //post.getUser().getUsername()


    }
}