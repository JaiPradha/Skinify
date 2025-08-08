package com.example.skinify;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SkinAnalysisActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 101;
    private static final int GALLERY_REQUEST_CODE = 102;
    private static final int PERMISSION_REQUEST_CODE = 103;

    private ImageView skinPhotoImageView;
    private Button takePhotoButton;
    private Button uploadPhotoButton;
    private Button analyzeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skin_analysis);

        skinPhotoImageView = findViewById(R.id.iv_skin_photo);
        takePhotoButton = findViewById(R.id.btn_take_photo);
        uploadPhotoButton = findViewById(R.id.btn_upload_photo);
        analyzeButton = findViewById(R.id.btn_analyze);

        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(SkinAnalysisActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(SkinAnalysisActivity.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
                } else {
                    launchCamera();
                }
            }
        });

        uploadPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(SkinAnalysisActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(SkinAnalysisActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                } else {
                    launchGallery();
                }
            }
        });

        analyzeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the bitmap from the ImageView
                skinPhotoImageView.setDrawingCacheEnabled(true);
                skinPhotoImageView.buildDrawingCache();
                Bitmap bitmap = skinPhotoImageView.getDrawingCache();

                if (bitmap == null) {
                    Toast.makeText(SkinAnalysisActivity.this, "Please take or upload a photo first.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Send the image to the backend on a new thread
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // IMPORTANT: Use your computer's IP address, not "localhost" or "127.0.0.1".
                        // Replace 'YOUR_IP_ADDRESS' with your actual IP address.
                        String url = "http://YOUR_IP_ADDRESS:5000/analyze_skin";

                        try {
                            // Convert Bitmap to byte array
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            byte[] imageBytes = stream.toByteArray();

                            OkHttpClient client = new OkHttpClient();

                            RequestBody requestBody = new MultipartBody.Builder()
                                    .setType(MultipartBody.FORM)
                                    .addFormDataPart("image", "skin_photo.png", RequestBody.create(imageBytes, MediaType.parse("image/png")))
                                    .build();

                            Request request = new Request.Builder()
                                    .url(url)
                                    .post(requestBody)
                                    .build();

                            Response response = client.newCall(request).execute();

                            String responseBody = response.body().string();

                            // Display the response on the UI thread
                            runOnUiThread(() -> {
                                Toast.makeText(SkinAnalysisActivity.this, "Analysis Result: " + responseBody, Toast.LENGTH_LONG).show();
                            });

                        } catch (IOException e) {
                            e.printStackTrace();
                            runOnUiThread(() -> {
                                Toast.makeText(SkinAnalysisActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                        } finally {
                            // Clear the drawing cache
                            skinPhotoImageView.setDrawingCacheEnabled(false);
                        }
                    }
                }).start();
            }
        });
    }

    private void launchCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }

    private void launchGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == CAMERA_REQUEST_CODE) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                skinPhotoImageView.setImageBitmap(photo);
            } else if (requestCode == GALLERY_REQUEST_CODE) {
                Uri selectedImage = data.getData();
                try {
                    Bitmap photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                    skinPhotoImageView.setImageBitmap(photo);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (permissions[0].equals(Manifest.permission.CAMERA)) {
                    launchCamera();
                } else if (permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    launchGallery();
                }
            } else {
                Toast.makeText(this, "Permission denied. Cannot access camera or gallery.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}