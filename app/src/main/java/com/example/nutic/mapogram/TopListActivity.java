package com.example.nutic.mapogram;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TopListActivity extends AppCompatActivity {

  private String url = "http://mapogram.dejan7.com/api/users/toplist";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_top_list);

    loadToplist();
  }


  private void loadToplist() {
    JsonArrayRequest jsObjRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
      @Override
      public void onResponse(JSONArray response) {
        ListView list = (ListView) findViewById(R.id.mListView);
        ArrayList<String> arrayList = new ArrayList<String>();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, arrayList);

        // Here, you set the data in your ListView
        list.setAdapter(adapter);

        for (int i = 0; i < response.length(); i++) {
          try {
            JSONObject user = new JSONObject(response.get(i).toString());

            String listItem = user.getString("username") + " (";
            listItem += user.getString("first_name") != "null" ? user.getString("first_name") : "-";
            listItem += user.getString("last_name") != "null" ? user.getString("last_name") : "-";
            listItem += ") \t\t\t\t\t";
            listItem += user.getString("total_likes") + " likes";
            arrayList.add(listItem);

          } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error - Parsing", Toast.LENGTH_LONG).show();
          }
        }
        adapter.notifyDataSetChanged();
      }
    }, new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
        String message = "";
        try {
          JSONObject errResponse = new JSONObject(new String(error.networkResponse.data));
          message = errResponse.getString("error");
        } catch (Exception e) {
          e.printStackTrace();
        }
        Toast.makeText(getApplicationContext(), "Error: " + error.toString(), Toast.LENGTH_LONG).show();
      }
    });

    RequestQueue queue = Volley.newRequestQueue(this);
    queue.add(jsObjRequest);
  }
}
