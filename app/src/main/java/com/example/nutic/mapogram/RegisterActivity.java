package com.example.nutic.mapogram;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;


public class RegisterActivity extends AppCompatActivity {

//  private String url = "http://192.168.1.6:3000/test";
  private String url = "http://mapogram.dejan7.com/api/users/dejan";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_register);

    // Handle back button
    final Button backToLoginBtn = (Button) findViewById(R.id.backToLoginBtn);
    backToLoginBtn.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        Intent myIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        RegisterActivity.this.startActivity(myIntent);
      }
    });

    // Handle photo button
    final Button uploadPhotoBtn = (Button) findViewById(R.id.uploadPhotoBtn);
    uploadPhotoBtn.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivityForResult(intent, 0);
      }
    });

  }

  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == 0 && resultCode == RESULT_OK) {
      Bundle extras = data.getExtras();
      Bitmap imageBitmap = (Bitmap) extras.get("data");
      final ImageView photoView = (ImageView) findViewById(R.id.photoView);
      photoView.setImageBitmap(imageBitmap);
    }
  }

  public void submitForm(View v) {

    String jsonData = "{" +
      "username:\"" + ((EditText) findViewById(R.id.usernameTf)).getText() + "\"," +
      "first_name:\"" + ((EditText) findViewById(R.id.firstNameTf)).getText() + "\"," +
      "last_name:\"" + ((EditText) findViewById(R.id.lastNameTf)).getText() + "\"," +
      "email:\"" + ((EditText) findViewById(R.id.emailTf)).getText() + "\"," +
      "phone:\"" + ((EditText) findViewById(R.id.phoneNumberTf)).getText() + "\"," +
      "password:\"" + ((EditText) findViewById(R.id.passwordTf)).getText() + "\"," +
      "password_confirmation:\"" + ((EditText) findViewById(R.id.passwordConfTf)).getText() + "\"" +
      "}";


    // Instantiate the RequestQueue.
    RequestQueue queue = Volley.newRequestQueue(this);

    // Request a string response from the provided URL.
    JsonObjectRequest jsObjRequest = new JsonObjectRequest
      (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

        @Override
        public void onResponse(JSONObject response) {
          System.out.println(response);
          // Toast.makeText(getApplicationContext(), "Response is: " + response.substring(0, 500) , Toast.LENGTH_LONG).show();
        }
      }, new Response.ErrorListener() {

        @Override
        public void onErrorResponse(VolleyError error) {
          // TODO Auto-generated method stub
          Toast.makeText(getApplicationContext(), "That didn't work!", Toast.LENGTH_LONG).show();
        }
      });

    // Add the request to the RequestQueue.
    queue.add(jsObjRequest);


    Toast.makeText(getApplicationContext(), "Submiting form!", Toast.LENGTH_LONG).show();


  }
}
