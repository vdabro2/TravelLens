package com.example.travellens;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.parse.ParseUser;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
    private ImageView ivPictureFromPost;
    private TextView tvReceiverUsername;
    private DatabaseReference reference;
    private FloatingActionButton bDeleteAttachment;
    private boolean attachPhotoToMessage = false;

    public static String API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        ibSend = findViewById(R.id.ibSend);
        recyclerView = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        API_KEY = getString(R.string.firebase_server_key);
        tvReceiverName = findViewById(R.id.tvReceiverName);
        ivReceiverPicture = findViewById(R.id.ivReceiverPicture);
        bDeleteAttachment = findViewById(R.id.bDeleteAttachment);
        ivPictureFromPost = findViewById(R.id.ivAttachedToMessage);
        tvReceiverUsername = findViewById(R.id.tvReceiverUsername);

        // set up adapter
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        // get intent with the user info of who you're sending to
        // todo may be null if looking at list of messages
        intent = getIntent();
        post = intent.getExtras().getParcelable("post");

        ibSend.setOnClickListener(this::sendMessage);
        bDeleteAttachment.setOnClickListener(this::deleteAttachmentFromPost);

        adaptMessages(ParseUser.getCurrentUser().getString(Post.KEY_FIREBASE_USER_ID), post.getUser().getString(Post.KEY_FIREBASE_USER_ID));
        attachWidgets();
    }

    private void attachWidgets() {
        Glide.with(getApplicationContext())
                .load(post.getUser().getParseFile(Post.KEY_PROFILE_PICTURE).getUrl())
                .circleCrop()
                .into(ivReceiverPicture);
        tvReceiverUsername.setText(post.getUser().getUsername());
        tvReceiverName.setText(post.getUser().getString(Post.KEY_NAME));
        if (post == null) {
            ivPictureFromPost.setVisibility(View.GONE);
        } else {
            Glide.with(getApplicationContext()).load(post.getParseFile(Post.KEY_IMAGE)
                    .getUrl()).centerCrop().transform(new RoundedCorners(16)).into(ivPictureFromPost);
            attachPhotoToMessage = true;
        }
    }

    private void deleteAttachmentFromPost(View view) {
        ivPictureFromPost.setVisibility(View.GONE);
        attachPhotoToMessage = false;
    }

    private void sendMessage(View view) {
        reference = FirebaseDatabase.getInstance().getReference();
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd hh:mm a");
        String dateAndTime = formatter.format(date);

        HashMap<String, Object> keyValueMessagePairs = new HashMap<>();
        keyValueMessagePairs.put("sender", ParseUser.getCurrentUser().getString(Post.KEY_FIREBASE_USER_ID));
        keyValueMessagePairs.put("receiver", post.getUser().getString(Post.KEY_FIREBASE_USER_ID));
        keyValueMessagePairs.put("message", etMessage.getText().toString());
        keyValueMessagePairs.put("dateAndTime", dateAndTime);
        if (post != null && attachPhotoToMessage == true) {
            // attach image to message
            keyValueMessagePairs.put("photo", post.getParseFile(Post.KEY_IMAGE).getUrl());
        }

        reference.child("Chats").push().setValue(keyValueMessagePairs);

        // send a notification
        MyFirebaseMessagingService.sendNotification(post.getUser().getString(Post.KEY_DEVICE_TOKEN),
                getString(R.string.you_have_a_message) + post.getUser().getUsername(), etMessage.getText().toString());

        etMessage.setText("");
        ivPictureFromPost.setVisibility(View.GONE);
        
        //updateListOfMessagedUsers();
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

                    if ((message.getReceiver().equals(currentUserId)
                            && message.getSender().equals(otherUserId))
                    || (message.getReceiver().equals(otherUserId) && message.getSender().equals(currentUserId))) {
                        allMessages.add(message);
                    }

                    // TODO if user is only looking at listed messages make post null
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