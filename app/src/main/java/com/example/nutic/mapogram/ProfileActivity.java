package com.example.nutic.mapogram;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class ProfileActivity extends AppCompatActivity {

  private String url = "http://mapogram.dejan7.com/api/users";


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_profile);

    loadProfile();
  }

  private void loadProfile() {

    Intent intent = getIntent();
    String username = intent.getStringExtra("username");

//    Toast.makeText(getApplicationContext(), "Username from intent: " + username, Toast.LENGTH_LONG).show();

    url = url + "/" + username;

    JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
      @Override
      public void onResponse(JSONObject response) {

        ((TextView) findViewById(R.id.textViewUsername)).setText(response.optString("username"));
        ((TextView) findViewById(R.id.textViewEmail)).setText(response.optString("email"));
        ((TextView) findViewById(R.id.textViewFirstName)).setText(response.optString("first_name") == "null" || response.optString("first_name") == null ? "" : response.optString("first_name"));
        ((TextView) findViewById(R.id.textViewLastName)).setText(response.optString("last_name") == "null" || response.optString("last_name") == null ? "" : response.optString("last_name"));
        ((TextView) findViewById(R.id.textViewPhone)).setText(response.optString("phone_number") == "null" || response.optString("phone_number") == null ? "" : response.optString("phone_number"));

        DownloadImageTask task = new DownloadImageTask((ImageView) findViewById(R.id.imageView));

        if (response.optString("avatar") == null || response.optString("avatar") == "null")
          task.execute("https://www.android.com/static/2016/img/aife/homepage/history/2010_1x.jpg");
        else
          task.execute(response.optString("avatar"));

      }
    }, new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
        String message = "";
        try {
          JSONObject errResponse = new JSONObject(new String(error.networkResponse.data));
          message = errResponse.getString("error");
        } catch (JSONException e) {
          e.printStackTrace();
        }
        Toast.makeText(getApplicationContext(), "Error: " + message, Toast.LENGTH_LONG).show();
      }
    });

    RequestQueue queue = Volley.newRequestQueue(this);
    queue.add(jsObjRequest);
  }
}

class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
  ImageView bmImage;

  public DownloadImageTask(ImageView bmImage) {
    this.bmImage = bmImage;
  }

  protected Bitmap doInBackground(String... urls) {
    String urldisplay = urls[0];
    Bitmap mIcon11 = null;
    try {
      InputStream in = new java.net.URL(urldisplay).openStream();
      mIcon11 = BitmapFactory.decodeStream(in);
    } catch (Exception e) {
      Log.e("Error", e.getMessage());
      e.printStackTrace();
    }
    return mIcon11;
  }

  protected void onPostExecute(Bitmap result) {
    bmImage.setImageBitmap(result);
  }
}
