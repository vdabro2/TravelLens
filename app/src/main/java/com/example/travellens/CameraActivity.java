package com.example.travellens;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CameraActivity extends AppCompatActivity {
    private CameraHelper camera;
    private File photoFile;
    private Button bFromGallery;
    private Bitmap bitmap;
    private Button bFromCamera;

    public final static int REQUEST_CODE_GALLERY = 43;
    private final static String TAG = "CAMERA_ACTIVITY";
    private final static String PHOTO_FILE_NAME = "photo.jpg";
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        bFromCamera = findViewById(R.id.bFromCamera);
        bFromGallery = findViewById(R.id.bFromGallery);
        camera = new CameraHelper();

        bFromCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCamera();
            }
        });
        
        bFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchGallery();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
        } else if (requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK) {

            Uri selectedImage = data.getData();
            try {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), selectedImage);
                bitmap = ImageDecoder.decodeBitmap(source);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
                File resizedFile = camera.getPhotoFileUri(PHOTO_FILE_NAME + "_resized", getApplicationContext());
                try {
                    resizedFile.createNewFile();
                    try (FileOutputStream fos = new FileOutputStream(resizedFile)) {
                        fos.write(bytes.toByteArray());
                    } catch (FileNotFoundException e) {
                        Log.e(TAG, "An exception occurred: ", e);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "An exception occurred: ", e);
                }
                photoFile = resizedFile;
            } catch (FileNotFoundException e) {
                Log.e(TAG, "An exception occurred: ", e);
            } catch (IOException e) {
                Log.e(TAG, "An exception occurred: ", e);
            }
        }
        Intent intentBack = new Intent();
        intentBack.setData(Uri.fromFile(photoFile));
        setResult(RESULT_OK, intentBack);
        finish();
    }

    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFile = camera.getPhotoFileUri(PHOTO_FILE_NAME, getApplicationContext());
        Uri fileProvider = FileProvider.getUriForFile(CameraActivity.this, "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    private void launchGallery() {
        Intent intent = new Intent();
        photoFile = camera.getPhotoFileUri(PHOTO_FILE_NAME, getApplicationContext());
        Uri fileProvider = FileProvider.getUriForFile(CameraActivity.this, "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
        intent.setType("image/'");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Pick an image"), REQUEST_CODE_GALLERY);
    }

}