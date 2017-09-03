package com.example.nutic.mapogram;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PhotoActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

  private String url = "http://mapogram.dejan7.com/api/photos/";
  private int likes = 0;
  private String id; //photoID
  private String photoLngLatString = "";
  public static final String PREFS_NAME = "MapogramPrefs";
  Double latitude;
  Double longitude;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_photo);

    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);

    Intent intent = getIntent();
    latitude = intent.getDoubleExtra("latitude", 43.321349);
    longitude = intent.getDoubleExtra("longitude", 21.895758);
    /**
     * COMMENT CLICK
     */
    Button submitCommentBTN = (Button) findViewById(R.id.submitComment);
    submitCommentBTN.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        EditText commentText = (EditText) findViewById(R.id.commentTextarea);

        if (commentText.getText().toString().length() < 5) {
          Toast.makeText(getApplicationContext(), "Comment must be at least 5 characters long!", Toast.LENGTH_LONG).show();
        } else {
          sendComment(commentText.getText().toString());
        }
      }
    });

    /**
     * LIKE CLICK
     */
    Button likeBTN = (Button) findViewById(R.id.likebtn);
    likeBTN.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Log.e("nes", photoLngLatString);
        String[] lnglat = photoLngLatString.split(",");
        double lng = Double.parseDouble(lnglat[0]);
        double lat = Double.parseDouble(lnglat[1]);

        LatLng photoLocation = new LatLng(lat, lng);

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Log.e("nes2", String.valueOf(location.getLatitude()));
        Log.e("nes2", String.valueOf(location.getLatitude()));
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
          // TODO: Consider calling
          Log.e("nes2", "nema nista");
          //    ActivityCompat#requestPermissions
          // here to request the missing permissions, and then overriding
          //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
          //                                          int[] grantResults)
          // to handle the case where the user grants the permission. See the documentation
          // for ActivityCompat#requestPermissions for more details.
          return;
        }
      }
    });

    loadPhoto();
  }

  /**
   * LOAD PHOTO
   */
  private void loadPhoto() {
    Intent intent = getIntent();
    this.id = intent.getStringExtra("photoId");

    final TextView author = (TextView) findViewById(R.id.author);
    final TextView likes = (TextView) findViewById(R.id.likes);

    JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url + this.id, null, new Response.Listener<JSONObject>() {
      @Override
      public void onResponse(JSONObject response) {

        DownloadImageTask task = new DownloadImageTask((ImageView) findViewById(R.id.imageView));

        task.execute(response.optString("url"));

        try {
          author.setText(response.getJSONObject("user").getString("username"));
          int likesNr = Integer.parseInt(response.getString("likes"));
          likes.setText(likesNr == 1 ? "1 Like" : likesNr + " Likes");
          JSONArray comments = response.getJSONArray("comments");

          photoLngLatString = response.getString("location");

          loadComments(comments);
        } catch (JSONException e) {
          e.printStackTrace();
        }
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
    }) {
      @Override
      public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", "application/json");
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        headers.put("Authorization", "Bearer " + settings.getString("token", null));
        return headers;
      }
    };

    RequestQueue queue = Volley.newRequestQueue(this);
    queue.add(jsObjRequest);

  }

  private void loadComments(JSONArray comments) throws JSONException {
    final LinearLayout layout = (LinearLayout) findViewById(R.id.commentsLayout);

    if (comments.length() == 0 ) {
      //no comments yet
    } else {
      for (int i = 0; i < comments.length(); i++) {
        JSONObject comment = comments.getJSONObject(i);
        JSONObject commentAuthor = comment.getJSONObject("user");
        View commentView = getLayoutInflater().inflate(R.layout.comment, null);
        layout.addView(commentView);

        final ImageView commentPhoto = (ImageView) commentView.findViewById(R.id.commentPhoto);
        final TextView commentText = (TextView) commentView.findViewById(R.id.commentText);
        final TextView commentAuthorText = (TextView) commentView.findViewById(R.id.commentAuthor);

        if (commentAuthor.optString("avatar") != null ||  commentAuthor.optString("avatar") != "null") {
          DownloadImageTask task = new DownloadImageTask(commentPhoto);
          task.execute(commentAuthor.optString("avatar"));
        }

        commentText.setText(comment.getString("text"));
        commentAuthorText.setText(commentAuthor.getString("username") + ":");
      }
    }
  }

  private void sendComment(String commentText) {

    Map<String, String> params = new HashMap();

    params.put("text", commentText);
    JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url + this.id + "/comments", new JSONObject(params), new Response.Listener<JSONObject>() {
      @Override
      public void onResponse(JSONObject response) {
        EditText commentTextArea = (EditText) findViewById(R.id.commentTextarea);
        myRecreate();
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
    }) {
      @Override
      public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", "application/json");
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        headers.put("Authorization", "Bearer " + settings.getString("token", null));
        return headers;
      }
    };

    RequestQueue queue = Volley.newRequestQueue(this);
    queue.add(jsObjRequest);
  }

  private void myRecreate() {
    this.recreate();
  }

  @Override
  public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    int id = item.getItemId();

    switch (id) {
      case R.id.nav_add_friend: {
        Intent intent = new Intent(PhotoActivity.this, MapsActivity.class);
        PhotoActivity.this.startActivity(intent);
        return true;
      }
      case R.id.nav_show_users: {
        Intent alreadyLoggedInIntent = new Intent(PhotoActivity.this, MapsActivity.class);
        PhotoActivity.this.startActivity(alreadyLoggedInIntent);
        return true;
      }
      case R.id.nav_add_photo: {
        Intent intent = new Intent(PhotoActivity.this, AddPhotoActivity.class);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        PhotoActivity.this.startActivity(intent);
        return true;
      }
      case R.id.nav_top_list: {
        Intent intent = new Intent(PhotoActivity.this, TopListActivity.class);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        PhotoActivity.this.startActivity(intent);
        return true;
      }
      default: {
        return false;
      }
    }
  }
}

