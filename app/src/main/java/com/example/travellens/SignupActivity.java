package com.example.travellens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.ArrayList;
import java.util.List;

public class SignupActivity extends AppCompatActivity {
    private EditText etBio1;
    private EditText etPass;
    private EditText etName;
    private TextView tvSignup;
    private EditText etUsername;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        etName = findViewById(R.id.etName);
        etBio1 = findViewById(R.id.etBio1);
        etPass = findViewById(R.id.etPass);
        tvSignup = findViewById(R.id.tvSignUp);
        etUsername = findViewById(R.id.etUsername);

        tvSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etPass.getText().toString().isEmpty() || etBio1.getText().toString().isEmpty()
                        || etName.getText().toString().isEmpty() || etUsername.getText().toString().isEmpty()) {
                    Toast.makeText(SignupActivity.this, String.valueOf(R.string.couldnt_save), Toast.LENGTH_SHORT).show();
                } else {
                    // TODO add profile pic from gallery and from camera
                    String password = etPass.getText().toString();
                    String bio = etBio1.getText().toString();
                    String name = etName.getText().toString();
                    String un = etUsername.getText().toString();

                    ParseUser user = new ParseUser();
                    user.setUsername(un);
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
}