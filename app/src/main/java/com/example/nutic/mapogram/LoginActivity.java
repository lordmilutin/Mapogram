package com.example.nutic.mapogram;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    // Handle register button
    final Button registerButton = (Button) findViewById(R.id.registerBtn);
    registerButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        Toast.makeText(getApplicationContext(), "Please create account by filling up form!", Toast.LENGTH_LONG).show();
        Intent myIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        LoginActivity.this.startActivity(myIntent);
      }
    });

    // Handle login button click
    final Button loginButton = (Button) findViewById(R.id.loginBtn);
    loginButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        Toast.makeText(getApplicationContext(), "LOGIN ATTEMPT!", Toast.LENGTH_LONG).show();
      }
    });

  }
}
