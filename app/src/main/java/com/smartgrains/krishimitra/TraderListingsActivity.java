package com.smartgrains.krishimitra;

import android.os.Bundle;
import android.util.Log; // Import for logging
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class TraderListingsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CropAdapter cropAdapter;
    private List<CropListingModel> cropList;
    private String userId;

    private static final String TAG = "TraderListingsActivity"; // Tag for logging

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trader_listings);

        // Make status bar transparent
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.listingsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize crop list
        cropList = new ArrayList<>();

        // Get userId from intent and log it
        userId = getIntent().getStringExtra("USER_ID");
        Log.d(TAG, "User ID received: " + userId); // Log userId

        // Check if userId is null
        if (userId == null) {
            Log.e(TAG, "User ID is null!"); // Log error if userId is null
            Toast.makeText(this, "User ID is missing!", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if userId is not provided
            return;
        }

        // Fetch crop listings
        fetchCropListings();
    }

    private void fetchCropListings() {
        // Log the reference path for Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Listings");
        Log.d(TAG, "Database reference: " + databaseReference.toString()); // Log the reference path

        // Add ValueEventListener to the database reference
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                cropList.clear(); // Clear the previous list

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    CropListingModel crop = snapshot.getValue(CropListingModel.class);
                    if (crop != null) { // Ensure crop is not null
                        // Set the listing ID in the crop model
                        crop.setListingId(snapshot.getKey()); // Get the listing ID
                        cropList.add(crop);
                        Log.d(TAG, "Crop added: " + crop); // Log each added crop
                    }
                }

                // Log the number of crops fetched
                Log.d(TAG, "Number of crops fetched: " + cropList.size());

                // Set up the adapter
                cropAdapter = new CropAdapter(cropList, (crop, listingId) -> {
                    // Call method to show the Bottom Sheet Fragment
                    showUpdateCropDetails(crop, listingId);
                });
                recyclerView.setAdapter(cropAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error
                Toast.makeText(TraderListingsActivity.this, "Failed to load crops: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Database error: " + databaseError.getMessage()); // Log error details
            }
        });
    }

    private void showUpdateCropDetails(CropListingModel crop, String listingId) {
        // Pass the listing ID to the fragment
        UpdateCropDetailsFragment fragment = UpdateCropDetailsFragment.newInstance(crop, listingId, userId);
        fragment.show(getSupportFragmentManager(), "UpdateCropDetailsFragment");
        Log.d(TAG, "Showing update details for listingId: " + listingId + ", userId: " + userId); // Log showing details
    }
}
