package com.example.nutic.mapogram;

import android.Manifest.permission;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
    ConnectionCallbacks, OnConnectionFailedListener, OnMarkerClickListener, LocationListener,
    NavigationView.OnNavigationItemSelectedListener {

  public static final String PREFS_NAME = "MapogramPrefs";
  private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
  private static final int REQUEST_ENABLE_BT = 2;
  private static final float DEFAULT_ZOOM = 12;

  private GoogleMap mMap;
  private GoogleApiClient mGoogleApiClient;
  private Marker mCurrentMarker;
  private Location mLastLocation;
  private LocationRequest mLocationRequest;
  private BroadcastReceiver mDeviceDiscoverReceiver;
  private BluetoothAdapter mBluetoothAdapter;

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

    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);

    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

    mDeviceDiscoverReceiver = new BroadcastReceiver() {
      public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
          // Discovery has found a device. Get the BluetoothDevice
          // object and its info from the Intent.
          BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
          String deviceName = device.getName();
          Toast.makeText(getApplicationContext(), deviceName, Toast.LENGTH_SHORT).show();
        }
      }
    };

    registerReceiver(mDeviceDiscoverReceiver, filter);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    unregisterReceiver(mDeviceDiscoverReceiver);
  }

  @Override
  protected void onStart() {
    super.onStart();
    mGoogleApiClient.connect();
  }

  @Override
  protected void onStop() {
    super.onStop();
    mBluetoothAdapter.cancelDiscovery();
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
    mLocationRequest.setInterval(10000); // intervali osvezavanja
    mLocationRequest.setFastestInterval(50000);
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
  }

  private void getUsers(final Location location) {
    RequestQueue queue = Volley.newRequestQueue(this);
    // Request a string response from the provided URL.
    String apiUrl = "http://mapogram.dejan7.com/api/location/exchange";

    final StringRequest jsObjRequest = new StringRequest(Method.POST, apiUrl,
        new Response.Listener<String>() {
          @Override
          public void onResponse(String response) {
            Log.d("TAG", "onResponse: " + response);
            try {
              JSONObject array = new JSONObject(response);
            } catch (JSONException e) {
              e.printStackTrace();
            }
          }
        }, new ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
        error.printStackTrace();
      }
    }) {

      @Override
      protected Map<String, String> getParams() throws AuthFailureError {
        HashMap<String, String> params = new HashMap<>();
        params.put("location",
            "21.892018,43.318496");
        return params;
      }

      @Override
      public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        headers.put("Authorization", "Bearer 73zWKMNjndojXGlRKOM71ROjQKeOJfXLTA1k0M07rtJtJYunq6BGDCvFizVj" /*+ settings.getString("token", null)*/);
        return headers;
      }
    };

    // Add the request to the RequestQueue.
    queue.add(jsObjRequest);
  }

  @Override
  public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    int id = item.getItemId();

    switch (id) {
      case R.id.nav_add_friend: {
        Toast.makeText(this, "ADD FRIEND", Toast.LENGTH_SHORT).show();
        sendFriendRequestViaBlootooth();
        return true;
      }
      case R.id.nav_show_users: {
        getUsers(mLastLocation);
        return true;
      }
      default: {
        return false;
      }
    }
  }

  private void sendFriendRequestViaBlootooth() {
    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    if (mBluetoothAdapter.isEnabled()) {
      if (mBluetoothAdapter != null) {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
      } else {
        mBluetoothAdapter.startDiscovery();
      }
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == REQUEST_ENABLE_BT) {
      if (resultCode == RESULT_OK) {
        Intent discoverableIntent =
            new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
        mBluetoothAdapter.startDiscovery();
      }
    }
  }
}




