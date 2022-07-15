package com.example.travellens;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.parse.ParseUser;

import java.util.HashMap;

public class MessageActivity extends AppCompatActivity {
    private Post post;
    private Intent intent;
    private FirebaseUser user;
    private ImageButton ibSend;
    private EditText etMessage;
    private ImageView ivReceiverPicture;
    private TextView tvReceiverUsername;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        ibSend = findViewById(R.id.ibSend);
        etMessage = findViewById(R.id.etMessage);
        ivReceiverPicture = findViewById(R.id.ivReceiverPicture);
        tvReceiverUsername = findViewById(R.id.tvReceiverUsername);

        // get intent with the user info of who you're sending to
        intent = getIntent();
        post = intent.getExtras().getParcelable("post");
        ibSend.setOnClickListener(this::sendMessage);

    }

    private void sendMessage(View view) {
        reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", ParseUser.getCurrentUser().getString(Post.KEY_FIREBASE_USER_ID));
        hashMap.put("receiver", post.getUser().getString(Post.KEY_FIREBASE_USER_ID));
        hashMap.put("message", etMessage.getText().toString());
        reference.child("Chats").push().setValue(hashMap);

    }
}