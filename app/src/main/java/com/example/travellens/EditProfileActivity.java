package com.example.travellens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.travellens.fragments.ComposeFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;

public class EditProfileActivity extends AppCompatActivity {
    private Button bSave;
    private File photoFile;
    private ImageView ivPP;
    private TextView tvChange;
    private TextView tvLogout;
    private CameraHelper camera;
    private EditText etNameOnEdit;
    private ImageView ivLeavePage;
    private EditText etUsernameOnEdit;
    private EditText etBiographyOnEdit;
    private ParseFile updatedPhoto = null;
    public String photoFileName = "photo.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        // initialize
        photoFile = null;
        camera = new CameraHelper();
        tvLogout = findViewById(R.id.tvLogOut);
        bSave = findViewById(R.id.bSaveChanges);
        ivPP = findViewById(R.id.ivChangeProfilePic);
        etNameOnEdit = findViewById(R.id.etNameOnEdit);
        tvChange = findViewById(R.id.tvChangeProfilePic);
        ivLeavePage = findViewById(R.id.ivCancel);
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
                FirebaseAuth.getInstance().signOut();
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

        bSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUser();
            }
        });

        ivLeavePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // use intent for result to leave without doing anything
                Intent intentBack = new Intent();
                setResult(RESULT_OK, intentBack);
                finish();
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
    }

    private void updateUser() {
        ParseUser user = ParseUser.getCurrentUser();
        if (photoFile == null && user.getParseFile(Post.KEY_PROFILE_PICTURE) != null) {
            // no need to update
            user.put(Post.KEY_PROFILE_PICTURE, user.getParseFile(Post.KEY_PROFILE_PICTURE));
        } else if (photoFile != null) {
            user.put(Post.KEY_PROFILE_PICTURE, new ParseFile(photoFile));
        } // otherwise do nothing bc the user has no profile pic and has not added one, there is
          // nothing to save

        // put name, username, bio, password
        user.put(Post.KEY_USERNAME, etUsernameOnEdit.getText().toString());

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