package com.example.travellens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parse.ParseUser;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessageActivity extends AppCompatActivity {
    private Post post;
    private Intent intent;
    private FirebaseUser user;
    private ImageButton ibSend;
    private EditText etMessage;
    private MessageAdapter adapter;
    private TextView tvReceiverName;
    private List<Message> allMessages;
    private RecyclerView recyclerView;
    private ImageView ivReceiverPicture;
    private TextView tvReceiverUsername;
    private DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        ibSend = findViewById(R.id.ibSend);
        etMessage = findViewById(R.id.etMessage);
        tvReceiverName = findViewById(R.id.tvReceiverName);
        ivReceiverPicture = findViewById(R.id.ivReceiverPicture);
        tvReceiverUsername = findViewById(R.id.tvReceiverUsername);
        recyclerView = findViewById(R.id.rvChat);

        // set up adapter
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        // get intent with the user info of who you're sending to
        intent = getIntent();
        post = intent.getExtras().getParcelable("post");
        ibSend.setOnClickListener(this::sendMessage);
        adaptMessages(ParseUser.getCurrentUser().getString(Post.KEY_FIREBASE_USER_ID), post.getUser().getString(Post.KEY_FIREBASE_USER_ID));

        Glide.with(getApplicationContext())
                .load(post.getUser().getParseFile(Post.KEY_PROFILE_PICTURE).getUrl())
                .circleCrop()
                .into(ivReceiverPicture);
        tvReceiverUsername.setText(post.getUser().getUsername());
        tvReceiverName.setText(post.getUser().getString(Post.KEY_NAME));
    }

    private void sendMessage(View view) {
        reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", ParseUser.getCurrentUser().getString(Post.KEY_FIREBASE_USER_ID));
        hashMap.put("receiver", post.getUser().getString(Post.KEY_FIREBASE_USER_ID));
        hashMap.put("message", etMessage.getText().toString());
        reference.child("Chats").push().setValue(hashMap);
    }

    private void adaptMessages(String currentUserId, String otherUserId) {
        allMessages = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allMessages.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Message message = dataSnapshot.getValue(Message.class);
                    if ((message.getReceiver().equals(currentUserId) && message.getSender().equals(otherUserId))
                    || (message.getReceiver().equals(otherUserId) && message.getSender().equals(currentUserId))) {
                        allMessages.add(message);
                    }

                    // TODO if user is only looking at messages make post null
                    adapter = new MessageAdapter(MessageActivity.this, allMessages, post);
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}