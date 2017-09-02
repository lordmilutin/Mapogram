package com.example.nutic.mapogram;

import android.Manifest.permission;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
    ConnectionCallbacks, OnConnectionFailedListener, OnMarkerClickListener, LocationListener {

  public static final String PREFS_NAME = "MapogramPrefs";
  private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
  private static final float DEFAULT_ZOOM = 12;

  private GoogleMap mMap;
  private GoogleApiClient mGoogleApiClient;
  private Marker mCurrentMarker;
  private Location mLastLocation;
  private LocationRequest mLocationRequest;

  private String url = "http://mapogram.dejan7.com/api/photos/{location}/{distance}";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_maps);

    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
        .findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);

    if (mGoogleApiClient == null) {
      mGoogleApiClient = new GoogleApiClient.Builder(this)
          .addConnectionCallbacks(this)
          .addOnConnectionFailedListener(this)
          .addApi(LocationServices.API)
          .build();
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    mGoogleApiClient.connect();
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
      mGoogleApiClient.disconnect();
    }
  }

  @Override
  public void onMapReady(GoogleMap googleMap) {
    mMap = googleMap;
    mMap.getUiSettings().setZoomControlsEnabled(true);
    mMap.setOnMarkerClickListener(this);

    setUpMap();
  }

  private void setUpMap() {
    if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {

      ActivityCompat.requestPermissions(this,
          new String[]{permission.ACCESS_FINE_LOCATION},
          LOCATION_PERMISSION_REQUEST_CODE);
    } else {
      initMap();
    }
  }

  private void initMap() {
    if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
      return;
    }
    mMap.setMyLocationEnabled(true);
    mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    switch (requestCode) {
      case LOCATION_PERMISSION_REQUEST_CODE: {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          initMap();
        } else {
          return;
        }
        break;
      }
    }
  }

  @Override
  public void onConnected(@Nullable Bundle bundle) {
    mLocationRequest = new LocationRequest();
    mLocationRequest.setInterval(1000);
    mLocationRequest.setFastestInterval(1000);
    mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    if (ContextCompat.checkSelfPermission(this,
        permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

      LocationServices.FusedLocationApi
          .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }
  }

  @Override
  public void onConnectionSuspended(int i) {

  }

  @Override
  public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

  }

  @Override
  public boolean onMarkerClick(Marker marker) {
    return false;
  }

  @Override
  public void onLocationChanged(Location location) {
    mLastLocation = location;
    updateCurrentLocation(location);
  }

  private void updateCurrentLocation(Location location) {
    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

    if (mCurrentMarker != null) {
      mCurrentMarker.remove();
    }

    MarkerOptions markerOptions = new MarkerOptions();
    markerOptions.position(latLng);
    markerOptions.title("Current Position");
    mCurrentMarker = mMap.addMarker(markerOptions);
    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

    getUsers(location);
  }

  private void getUsers(final Location location){
    RequestQueue queue = Volley.newRequestQueue(this);
    // Request a string response from the provided URL.
    String apiUrl = "http://mapogram.dejan7.com/api/location/exchange";

    HashMap<String, String> params = new HashMap<>();
    params.put("location", String.valueOf(location.getLongitude()) + ", " + String.valueOf(location.getLatitude()));

    final JsonObjectRequest jsObjRequest = new JsonObjectRequest
        (Method.POST, apiUrl, new JSONObject(params), new Response.Listener<JSONObject>() {

          @Override
          public void onResponse(JSONObject response) {
            Toast.makeText(getApplicationContext(), "Response: " + response.toString(),
                Toast.LENGTH_LONG).show();
          }
        }, new Response.ErrorListener() {

          @Override
          public void onErrorResponse(VolleyError error) {
            error.printStackTrace();
            Toast.makeText(getApplicationContext(), "Error occured", Toast.LENGTH_LONG).show();
          }
        }) {
      @Override
      public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String, String> header = new HashMap<>();
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        header.put("Authorization", settings.getString("token", null));
        return header;
      }
    };

    // Add the request to the RequestQueue.
    queue.add(jsObjRequest);
  }
}




