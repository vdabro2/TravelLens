package com.example.travellens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivity extends AppCompatActivity {
    EditText etPass;
    EditText etUsername;
    Button bLogin;
    TextView signup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        etPass = findViewById(R.id.etPass);
        etUsername = findViewById(R.id.etUsername);
        bLogin = findViewById(R.id.bLogin);
        signup = findViewById(R.id.tvSignUp);
        if (ParseUser.getCurrentUser() != null) {
            goMainActivity();
        }
        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pass = etPass.getText().toString();
                String user = etUsername.getText().toString();
                loginUser(user, pass);
            }
        });
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(i);
            }
        });
    }

    private void loginUser(String user, String pass) {
        ParseUser.logInInBackground(user, pass, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e!= null) {
                    Log.e("LOGIN", e.toString());

                    return;
                }
                //Toast.makeText(LoginActivity.this, "SUCCESS", Toast.LENGTH_SHORT);
                goMainActivity();
                Toast.makeText(LoginActivity.this, "SUCCESS", Toast.LENGTH_SHORT);
            }
        });
    }

    private void goMainActivity() {
        Intent i = new Intent(this, FeedMainActivity.class);

        startActivity(i);
        //Log.i("LOGIN SUCC")
        finish();
    }
}