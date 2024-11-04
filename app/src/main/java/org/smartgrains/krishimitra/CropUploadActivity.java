package org.smartgrains.krishimitra;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class CropUploadActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = "CropUploadActivity";
    private EditText editTextCropName;
    private ImageView imageViewCrop;
    private Button buttonSelectImage, buttonUploadCrop;
    private ProgressBar progressBar;

    private Uri imageUri;

    // Firebase instances
    private DatabaseReference databaseRef;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_upload);

        // Make status bar transparent
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        editTextCropName = findViewById(R.id.editTextCropName);
        imageViewCrop = findViewById(R.id.imageViewCrop);
        buttonSelectImage = findViewById(R.id.buttonSelectImage);
        buttonUploadCrop = findViewById(R.id.buttonUploadCrop);
        progressBar = findViewById(R.id.progressBar);

        // Initialize Firebase references
        databaseRef = FirebaseDatabase.getInstance().getReference("CropResource");
        storageRef = FirebaseStorage.getInstance().getReference("crop_images");

        // Set a placeholder image
        imageViewCrop.setImageResource(R.drawable.placeholder_image); // Replace with your actual placeholder image

        // Set listeners
        buttonSelectImage.setOnClickListener(v -> openImageChooser());
        buttonUploadCrop.setOnClickListener(v -> uploadCrop());
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Crop Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageViewCrop.setImageURI(imageUri);
        } else {
            // Set placeholder image if no image is selected
            imageViewCrop.setImageResource(R.drawable.admin_placeholder);
        }
    }

    private void uploadCrop() {
        String cropName = editTextCropName.getText().toString().trim();

        if (cropName.isEmpty()) {
            Toast.makeText(this, "Crop name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgress(true);

        // Define a unique reference for the image in storage
        StorageReference fileRef = storageRef.child(cropName + "_" + System.currentTimeMillis() + ".jpg");

        // Upload image to Firebase Storage
        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();

                    // Prepare data for database
                    Map<String, Object> cropData = new HashMap<>();
                    cropData.put("cropName", cropName);
                    cropData.put("imageUrl", imageUrl);

                    // Add data under CropResource node
                    databaseRef.push().setValue(cropData)
                            .addOnCompleteListener(task -> {
                                showProgress(false);

                                if (task.isSuccessful()) {
                                    Toast.makeText(CropUploadActivity.this, "Crop uploaded successfully", Toast.LENGTH_SHORT).show();
                                    resetInputFields();
                                } else {
                                    Toast.makeText(CropUploadActivity.this, "Failed to upload crop", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Database error: " + task.getException());
                                }
                            });
                }))
                .addOnFailureListener(e -> {
                    showProgress(false);
                    Log.e(TAG, "Image upload failed: " + e.getMessage());
                    Toast.makeText(CropUploadActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                });
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        buttonUploadCrop.setEnabled(!show);
    }

    private void resetInputFields() {
        editTextCropName.setText("");
        imageViewCrop.setImageResource(R.drawable.placeholder_image); // Reset to placeholder
        imageUri = null; // Clear the image URI
    }
}