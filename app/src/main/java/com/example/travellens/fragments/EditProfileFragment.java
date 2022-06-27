package com.example.travellens.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.travellens.LoginActivity;
import com.example.travellens.R;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditProfileFragment extends Fragment {

    private String mParam1;
    private String mParam2;
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
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public final static int REQUEST_CODE_GALLERY = 43;

    public EditProfileFragment() {
        // Required empty public constructor
    }

    public static EditProfileFragment newInstance(String param1, String param2) {
        EditProfileFragment fragment = new EditProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // initiate
        tvChange = view.findViewById(R.id.tvChangePP);
        tvLogout = view.findViewById(R.id.tvLogOut);
        ivPP = view.findViewById(R.id.ivChangePP);
        etPassEdit = view.findViewById(R.id.etPassEdit);
        etNameOnEdit = view.findViewById(R.id.etNameOnEdit);
        etUsernameOnEdit = view.findViewById(R.id.etUsernameOnEdit);
        etBiographyOnEdit = view.findViewById(R.id.etBiographyOnEdit);
        tvSave = view.findViewById(R.id.tvSaveChanges);
        tvResetChanges = view.findViewById(R.id.tvResetChanges);

        populateCurrentUserInfo();


        // on logout click
        tvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // log out parse user
                ParseUser.logOutInBackground();
                ParseUser currentUser = ParseUser.getCurrentUser();
                Intent i = new Intent(getContext(), LoginActivity.class);
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
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getActivity().getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.getView().setEnabled(false);
        autocompleteFragment.getView().setVisibility(View.INVISIBLE);

        ParseUser user = ParseUser.getCurrentUser();
        ParseFile profilepic = user.getParseFile("profilePicture");
        if (profilepic != null) {
            Glide.with(getContext()).load(profilepic.getUrl()).circleCrop()
                    .into(ivPP);
        }
        etNameOnEdit.setText(user.getString("name"));
        etBiographyOnEdit.setText(user.getString("biography"));
        etUsernameOnEdit.setText(user.getString("username"));
        etPassEdit.setText("");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GALLERY && resultCode == getActivity().RESULT_OK) {
            Uri selectedImage = data.getData();
            Bitmap bitmap = null;
            ParseUser user  = ParseUser.getCurrentUser();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
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
            user.put("profilePicture", user.getParseFile("profilePicture"));
        } else {
            user.put("profilePicture", updatedPhoto);
        }

        // put name, username, bio, password
        user.put("username", etUsernameOnEdit.getText().toString());
        if (etPassEdit.getText() != null || !etPassEdit.getText().toString().isEmpty()) {
            user.put("password", etPassEdit.getText().toString());
        }
        user.put("name", etNameOnEdit.getText().toString());
        user.put("biography", etBiographyOnEdit.getText().toString());

        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(getActivity(), "Error while saving", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(getActivity(), "Changed were saved", Toast.LENGTH_SHORT).show();
                // go to profile fragment when done saving
                ProfileFragment profileFragment = new ProfileFragment(user);
                AppCompatActivity activity = (AppCompatActivity) getActivity();
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, profileFragment).addToBackStack(null).commit();
            }
        });
    }


}