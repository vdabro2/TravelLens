package com.example.travellens;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivity extends AppCompatActivity {
    private Button bLogin;
    private TextView signup;
    private EditText etPass;
    private EditText etUsername;
    private ImageView ivBackLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        etPass = findViewById(R.id.etPass);
        bLogin = findViewById(R.id.bLogin);
        signup = findViewById(R.id.tvSignUp);
        etUsername = findViewById(R.id.etUsername);
        ivBackLogin = findViewById(R.id.ivBackLogin);

        if (ParseUser.getCurrentUser() != null) {
            // no need for re-authentication if user is already logged in
            goMainActivity();
        }
        // login user if they clicked login
        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pass = etPass.getText().toString();
                String user = etUsername.getText().toString();
                loginUser(user, pass);
            }
        });

        // go to signup if user clicks sign up
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, SignupActivity.class);
                Pair<View, String> pairUsername = Pair.create((View)etUsername, "user");
                Pair<View, String> pairPassword = Pair.create(etPass, "pass");
                Pair<View, String> pairBackground = Pair.create((View)ivBackLogin, "background");
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(LoginActivity.this, pairUsername, pairPassword, pairBackground);
                startActivity(i, options.toBundle());
            }
        });
    }

    private void loginUser(String user, String pass) {
        ParseUser.logInInBackground(user, pass, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e!= null) {
                    Log.e("LOGIN", e.toString());
                    Toast.makeText(LoginActivity.this, getString(R.string.cant_login), Toast.LENGTH_SHORT);
                    return;
                }
                goMainActivity();
            }
        });
    }

    private void goMainActivity() {
        Intent i = new Intent(this, FeedMainActivity.class);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this);
        startActivity(i, options.toBundle());
        finish();
    }
}