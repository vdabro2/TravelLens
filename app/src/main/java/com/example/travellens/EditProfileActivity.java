package com.example.travellens;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.travellens.fragments.ComposeFragment;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;

public class EditProfileActivity extends AppCompatActivity {
    private File photoFile;
    private ImageView ivPP;
    private TextView tvSave;
    private TextView tvChange;
    private TextView tvLogout;
    private EditText etPassEdit;
    private CameraHelper camera;
    private EditText etNameOnEdit;
    private TextView tvResetChanges;
    private EditText etUsernameOnEdit;
    private EditText etBiographyOnEdit;
    private ParseFile updatedPhoto = null;
    public String photoFileName = "photo.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        // initiate
        photoFile = null;
        camera = new CameraHelper();
        ivPP = findViewById(R.id.ivChangeProfilePic);
        tvLogout = findViewById(R.id.tvLogOut);
        tvChange = findViewById(R.id.tvChangeProfilePic);
        tvSave = findViewById(R.id.tvSaveChanges);
        etPassEdit = findViewById(R.id.etPassEdit);
        etNameOnEdit = findViewById(R.id.etNameOnEdit);
        tvResetChanges = findViewById(R.id.tvResetChanges);
        etUsernameOnEdit = findViewById(R.id.etUsernameOnEdit);
        etBiographyOnEdit = findViewById(R.id.etBiographyOnEdit);

        populateCurrentUserInfo();
        // on logout click
        tvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // log out parse user
                ParseUser.logOutInBackground();
                ParseUser currentUser = ParseUser.getCurrentUser();
                Intent i = new Intent(EditProfileActivity.this, LoginActivity.class);
                startActivity(i);
            }
        });
        // on change profile picture
        tvChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(EditProfileActivity.this, CameraActivity.class);
                startActivityForResult(i, ComposeFragment.RESULT_CODE_FROM_CAMERA);
            }
        });

        tvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUser();
            }
        });

        tvResetChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                populateCurrentUserInfo();
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ComposeFragment.RESULT_CODE_FROM_CAMERA) {
            if (resultCode == RESULT_OK) {
                Uri selectedImage = data.getData();
                photoFile = camera.uriToFile(EditProfileActivity.this, selectedImage, photoFileName, getApplicationContext());
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                Glide.with(this).load(takenImage).circleCrop()
                        .into(ivPP);
            }
        }
    }

    private void populateCurrentUserInfo() {
        ParseUser user = ParseUser.getCurrentUser();
        ParseFile profilepic = user.getParseFile(Post.KEY_PROFILE_PICTURE);
        if (profilepic != null) {
            Glide.with(this).load(profilepic.getUrl()).circleCrop()
                    .into(ivPP);
        }
        etNameOnEdit.setText(user.getString(Post.KEY_NAME));
        etBiographyOnEdit.setText(user.getString(Post.KEY_BIOGRAPHY));
        etUsernameOnEdit.setText(user.getString(Post.KEY_USERNAME));
        etPassEdit.setText("");
    }

    private void updateUser() {
        ParseUser user = ParseUser.getCurrentUser();
        if (photoFile == null) {
            // no need to update
            user.put(Post.KEY_PROFILE_PICTURE, user.getParseFile(Post.KEY_PROFILE_PICTURE));
        } else {
            user.put(Post.KEY_PROFILE_PICTURE, new ParseFile(photoFile));
        }

        // put name, username, bio, password
        user.put(Post.KEY_USERNAME, etUsernameOnEdit.getText().toString());
        if (etPassEdit.getText() != null || !etPassEdit.getText().toString().isEmpty()) {
            user.put(Post.KEY_PASSWORD, etPassEdit.getText().toString());
        }
        user.put(Post.KEY_NAME, etNameOnEdit.getText().toString());
        user.put(Post.KEY_BIOGRAPHY, etBiographyOnEdit.getText().toString());

        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(EditProfileActivity.this, String.valueOf(R.string.couldnt_save), Toast.LENGTH_SHORT).show();
                    return;
                }
                // ask profile adapter to refresh before returning
                // use intent for result
                Intent intentBack = new Intent();
                setResult(RESULT_OK, intentBack);
                finish();
            }
        });
    }

}