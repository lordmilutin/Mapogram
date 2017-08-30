package com.example.nutic.mapogram;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {

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
}
