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

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.travellens.fragments.ComposeFragment;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.io.File;

public class SignupActivity extends AppCompatActivity {
    private EditText etBio1;
    private EditText etPass;
    private EditText etName;
    private TextView tvSignup;
    private EditText etUsername;
    private ImageView ivBack;

    private ImageView ivIcon;
    private File photoFile;
    private CameraHelper camera;
    public String photoFileName = "photo.jpg";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        etName = findViewById(R.id.etName);
        ivBack = findViewById(R.id.ivBack);
        etBio1 = findViewById(R.id.etBiography);
        etPass = findViewById(R.id.etPass);
        tvSignup = findViewById(R.id.tvSignUp);
        etUsername = findViewById(R.id.etUsername);
        ivIcon = findViewById(R.id.ivIcon);

        photoFile = null;
        camera = new CameraHelper();
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
                if (etPass.getText().toString().isEmpty() || etBio1.getText().toString().isEmpty()
                        || etName.getText().toString().isEmpty() || etUsername.getText().toString().isEmpty()) {
                    Toast.makeText(SignupActivity.this, String.valueOf(R.string.couldnt_save), Toast.LENGTH_SHORT).show();
                } else {
                    String password = etPass.getText().toString();
                    String bio = etBio1.getText().toString();
                    String name = etName.getText().toString();
                    String un = etUsername.getText().toString();

                    ParseUser user = new ParseUser();
                    user.setUsername(un);
                    if (photoFile != null)
                        user.put(Post.KEY_PROFILE_PICTURE, new ParseFile(photoFile));
                    user.setPassword(password);
                    user.put(Post.KEY_BIOGRAPHY, bio);
                    user.put(Post.KEY_NAME, name);

                    // call the database to add the user
                    user.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e("Signup Problem", e.toString());
                            } else {
                                ParseUser.logInInBackground(un, password, new LogInCallback() {
                                    @Override
                                    public void done(ParseUser user, ParseException e) {
                                        if (e!= null) {
                                            Log.e("LOGIN ISSUE after signup", e.toString());
                                            Toast.makeText(SignupActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        Intent i = new Intent(SignupActivity.this, FeedMainActivity.class);
                                        startActivity(i);
                                        finish();
                                    }
                                });
                            }

                        }
                    });
                }
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ComposeFragment.RESULT_CODE_FROM_CAMERA) {
            if (resultCode == RESULT_OK) {
                Uri selectedImage = data.getData();
                photoFile = camera.uriToFile(SignupActivity.this, selectedImage, photoFileName, getApplicationContext());
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                Glide.with(this).load(takenImage).circleCrop()
                        .into(ivIcon);
            }
        }
    }
}