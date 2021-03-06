package com.example.travellens;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.L;
import com.bumptech.glide.Glide;
import com.example.travellens.fragments.ComposeFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.io.File;
import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {
    private String userId;
    private File photoFile;
    private EditText etPass;
    private EditText etName;
    private ImageView ivBack;
    private EditText etEmail;
    private ImageView ivIcon;
    private FirebaseAuth auth;
    private TextView tvSignup;
    private EditText etUsername;
    private CameraHelper camera;
    private EditText etBiography;
    private DatabaseReference reference;
    public String photoFileName = "photo.jpg";
    private static final String TAG = "SIGNUP_ACTIVITY";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        photoFile = null;
        camera = new CameraHelper();
        etPass = findViewById(R.id.etPass);
        etName = findViewById(R.id.etName);
        ivBack = findViewById(R.id.ivBack);
        ivIcon = findViewById(R.id.ivIcon);
        etEmail = findViewById(R.id.etEmail);
        tvSignup = findViewById(R.id.tvSignUp);
        etBiography = findViewById(R.id.etBiography);
        etUsername = findViewById(R.id.etUsername);
        auth = FirebaseAuth.getInstance();
        ivIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SignupActivity.this, CameraActivity.class);
                startActivityForResult(i, ComposeFragment.RESULT_CODE_FROM_CAMERA);
            }
        });

        tvSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etPass.getText().toString().isEmpty() || etBiography.getText().toString().isEmpty()
                        || etName.getText().toString().isEmpty() || etUsername.getText().toString().isEmpty() ||
                etEmail.getText().toString().isEmpty()) {
                    Toast.makeText(SignupActivity.this, String.valueOf(R.string.couldnt_save), Toast.LENGTH_SHORT).show();
                } else {
                    registerFirebase();
                }
            }
        });
    }

    private void registerFirebase() {
        auth.createUserWithEmailAndPassword(etEmail.getText().toString(), etPass.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    userId = firebaseUser.getUid();

                    reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("id", userId);
                    hashMap.put("username", etUsername.getText().toString());
                    hashMap.put("imageURL", "default");

                    reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()) {
                                Log.e(TAG, "exception occurred:", task.getException());
                            }
                        }
                    });
                } else {
                    Log.e(TAG, "exception occurred:", task.getException());
                }
                signUpUser();
            }
        });
    }

    private void signUpUser() {
        String password = etPass.getText().toString();
        String bio = etBiography.getText().toString();
        String name = etName.getText().toString();
        String username = etUsername.getText().toString();

        ParseUser user = new ParseUser();
        user.setUsername(username);
        if (photoFile != null)
            user.put(Post.KEY_PROFILE_PICTURE, new ParseFile(photoFile));

        user.setPassword(password);
        user.put(Post.KEY_BIOGRAPHY, bio);
        user.put(Post.KEY_NAME, name);
        user.put(Post.KEY_FIREBASE_USER_ID, userId);
        user.put(Post.KEY_USER_EMAIL, etEmail.getText().toString());


        // call the database to add the user
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "An exception occurred: ", e);
                } else {
                    loginNewUser(username, password);
                }

            }
        });
    }

    private void loginNewUser(String username, String password) {
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e!= null) {
                    Log.e(TAG, "An exception occurred: ", e);
                    Toast.makeText(SignupActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    return;
                }
                MyFirebaseMessagingService.checkUserTokenUpdate();
                Intent i = new Intent(SignupActivity.this, FeedMainActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ComposeFragment.RESULT_CODE_FROM_CAMERA) {
            if (resultCode == RESULT_OK) {
                Uri selectedImage = data.getData();
                photoFile = camera.uriToFile(SignupActivity.this, selectedImage, photoFileName, getApplicationContext());
                // scales back because icon image is scaled
                ivIcon.setScaleX((float) (1/1.5));
                ivIcon.setScaleY((float) (1/1.5));
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                Glide.with(this).load(takenImage).circleCrop()
                        .into(ivIcon);

            }
        }
    }
}