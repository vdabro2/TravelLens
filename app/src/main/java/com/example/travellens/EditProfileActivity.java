package com.example.travellens;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.travellens.fragments.ProfileFragment;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class EditProfileActivity extends AppCompatActivity {
    private ImageView ivPP;
    private TextView tvSave;
    private TextView tvChange;
    private TextView tvLogout;
    private EditText etPassEdit;
    private EditText etNameOnEdit;
    private TextView tvResetChanges;
    private EditText etUsernameOnEdit;
    private EditText etBiographyOnEdit;
    private ParseFile updatedPhoto = null;
    public final static int REQUEST_CODE_GALLERY = 43;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // initiate
        tvChange = findViewById(R.id.tvChangePP);
        tvLogout = findViewById(R.id.tvLogOut);
        ivPP = findViewById(R.id.ivChangePP);
        etPassEdit = findViewById(R.id.etPassEdit);
        etNameOnEdit = findViewById(R.id.etNameOnEdit);
        etUsernameOnEdit = findViewById(R.id.etUsernameOnEdit);
        etBiographyOnEdit = findViewById(R.id.etBiographyOnEdit);
        tvSave = findViewById(R.id.tvSaveChanges);
        tvResetChanges = findViewById(R.id.tvResetChanges);

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
                // request getting content from photo gallery
                // make chip group visible
                Intent intent = new Intent();
                intent.setType("image/'");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Pick an image"), REQUEST_CODE_GALLERY);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            Bitmap bitmap = null;
            ParseUser user  = ParseUser.getCurrentUser();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                Glide.with(this).load(selectedImage).apply(RequestOptions.circleCropTransform()).into(ivPP);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            updatedPhoto = conversionBitmapParseFile(bitmap);

        }
    }

    public ParseFile conversionBitmapParseFile(Bitmap imageBitmap){
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
        byte[] imageByte = byteArrayOutputStream.toByteArray();
        ParseFile parseFile = new ParseFile("image_file.png",imageByte);
        return parseFile;
    }

    private void updateUser() {
        ParseUser user = ParseUser.getCurrentUser();
        if (updatedPhoto == null) {
            // no need to update
            user.put(Post.KEY_PROFILE_PICTURE, user.getParseFile(Post.KEY_PROFILE_PICTURE));
        } else {
            user.put(Post.KEY_PROFILE_PICTURE, updatedPhoto);
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
                // go to profile fragment when done saving
                ProfileFragment profileFragment = new ProfileFragment(user);
                AppCompatActivity activity = (AppCompatActivity) EditProfileActivity.this;
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, profileFragment).addToBackStack(null).commit();
            }
        });
    }

}