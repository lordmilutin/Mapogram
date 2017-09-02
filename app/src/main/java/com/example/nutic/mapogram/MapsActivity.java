package com.example.nutic.mapogram;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

  private static final float DEFAULT_ZOOM = 9;
  private GoogleMap mMap;
  private String url = "http://mapogram.dejan7.com/api/photos/{location}/{distance}";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_maps);
    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
      .findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);
  }

  /**
   * Manipulates the map once available.
   * This callback is triggered when the map is ready to be used.
   * This is where we can add markers or lines, add listeners or move the camera
   */
  @Override
  public void onMapReady(GoogleMap googleMap) {
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      // Ask for permission
      ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
      return;
    } else {
      proceedWithPermission(googleMap);
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
    switch (requestCode) {
      case 2: {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          // permission was granted, yay!
        } else {
          // permission denied, boo! Disable the
          // functionality that depends on this permission.
          Toast.makeText(getApplicationContext(), "Too bad, we must have permission to proceed!", Toast.LENGTH_LONG).show();
        }
        return;
      }

      // other 'case' lines to check for other
      // permissions this app might request
    }
  }

  private void proceedWithPermission(GoogleMap mMap) {
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      return;
    }

    mMap.setMyLocationEnabled(true);

    LocationManager mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

    Location location = mLocationManager.getLastKnownLocation("");

    getDeviceLocation(mMap);

//    System.out.println(location.getLatitude());
//    System.out.println(location.getLongitude());

    // Add a marker in Sydney and move the camera
    LatLng sydney = new LatLng(-34, 151);
    mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));

    mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
  }

  private void getDeviceLocation(final GoogleMap mMap) {
    /*
     * Get the best and most recent location of the device, which may be null in rare
     * cases when a location is not available.
     */
    try {
      // Construct a FusedLocationProviderClient.
      FusedLocationProviderClient mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

      // Construct a FusedLocationProviderClient.
      mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
      Task locationResult = mFusedLocationProviderClient.getLastLocation();
      locationResult.addOnCompleteListener(this, new OnCompleteListener() {

        @Override
        public void onComplete(@NonNull Task task) {

          LatLng mDefaultLocation = new LatLng(43.321379, 21.895784);   // Nis Centar

          if (task.isSuccessful()) {
            // Set the map's camera position to the current location of the device.

            Location mLastKnownLocation = (Location) task.getResult();
            System.out.println(mLastKnownLocation);

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
          } else {
            Log.d("ZZ", "Current location is null. Using defaults.");
            Log.e("ZZ", "Exception: %s", task.getException());

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
          }
        }
      });
    } catch (SecurityException e) {
      Log.e("Exception: %s", e.getMessage());
    }
  }

  private void addMarkers(GoogleMap mMap) {
    RequestQueue queue = Volley.newRequestQueue(this);
    // Request a string response from the provided URL.
    JsonObjectRequest jsObjRequest = new JsonObjectRequest
      (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

        @Override
        public void onResponse(JSONObject response) {
          Toast.makeText(getApplicationContext(), "Response: " + response.toString(), Toast.LENGTH_LONG).show();
        }
      }, new Response.ErrorListener() {

        @Override
        public void onErrorResponse(VolleyError error) {
          Toast.makeText(getApplicationContext(), "Error occured", Toast.LENGTH_LONG).show();
        }
      });

    // Add the request to the RequestQueue.
    queue.add(jsObjRequest);
  }
}




