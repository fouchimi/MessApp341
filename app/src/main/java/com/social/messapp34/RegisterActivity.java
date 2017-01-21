package com.social.messapp34;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = RegisterActivity.class.getSimpleName();
    private EditText usernameText;
    private EditText emailText;
    private EditText passwordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameText = (EditText) findViewById(R.id.displayName);
        emailText = (EditText) findViewById(R.id.email);
        passwordText = (EditText) findViewById(R.id.pwd);
    }

    public void createAccount(View view){
        String username = usernameText.getText().toString();
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if(username.length() == 0 || email.length() == 0 || password.length() == 0){
            Toast.makeText(this, "Please fill all the required fields", Toast.LENGTH_LONG).show();
        }else {
            ParseUser parseUser = new ParseUser();
            parseUser.setUsername(username);
            parseUser.setEmail(email);
            parseUser.setPassword(password);

            parseUser.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(ParseException e) {
                    if(e == null){
                        Log.d(TAG, "Account created !!!");
                        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        RegisterActivity.this.startActivity(intent);
                    }else {
                        Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}
