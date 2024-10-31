package org.smartgrains.krishimitra;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FarmerDashboardActivity extends AppCompatActivity implements CropFilterBottomSheetFragment.OnCropsSelectedListener {
    private static final String TAG = "FarmerDashboardActivity";
    private RecyclerView cropImageRecyclerView;
    private ProgressBar progressBar;
    private CropImageAdapter cropImageAdapter;
    private List<CropListing> cropListingList;
    private Map<String, CropListing> allCropsMap;
    private DatabaseReference databaseReference;
    private String farmerState, farmerDistrict, farmerTaluka;
    private LinearLayout cropFilterButtonLayout, refreshButtonLayout;
    private ImageView cropFilterButtonIcon, refreshButtonIcon, userProfile;
    private TextView cropFilterButtonText, refreshButtonText;
    private int previousCropCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_dashboard);

        // Make status bar transparent
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        // Initialize views
        cropImageRecyclerView = findViewById(R.id.cropImageRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        cropFilterButtonLayout = findViewById(R.id.cropFilterButton); // Crop filter button layout
        refreshButtonLayout = findViewById(R.id.buttonRefresh); // Refresh button layout
        userProfile = findViewById(R.id.userProfile); // User profile ImageView

        // Initialize TextView and ImageView within crop filter button
        cropFilterButtonIcon = cropFilterButtonLayout.findViewById(R.id.icon_crop_filter);
        cropFilterButtonText = cropFilterButtonLayout.findViewById(R.id.text_crop_filter);

        // Initialize TextView and ImageView within refresh button
        refreshButtonIcon = refreshButtonLayout.findViewById(R.id.icon_refresh);
        refreshButtonText = refreshButtonLayout.findViewById(R.id.text_refresh);

        cropListingList = new ArrayList<>();
        allCropsMap = new HashMap<>();

        cropImageAdapter = new CropImageAdapter(this, cropListingList, cropListing -> {
            Intent intent = new Intent(FarmerDashboardActivity.this, TraderListActivity.class);
            intent.putExtra("CROP_NAME", cropListing.getCropName());
            intent.putExtra("FARMER_STATE", farmerState);
            intent.putExtra("FARMER_DISTRICT", farmerDistrict);
            intent.putExtra("FARMER_TALUKA", farmerTaluka);
            startActivity(intent);
        });

        cropImageRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cropImageRecyclerView.setAdapter(cropImageAdapter);

        // Set onClickListener for crop filter button
        cropFilterButtonLayout.setOnClickListener(v -> showCropFilterBottomSheet());

        // Set onClickListener for refresh button
        refreshButtonLayout.setOnClickListener(v -> fetchCropListings());

        // Set onClickListener for User Profile
        userProfile.setOnClickListener(v -> {
            String userId = getIntent().getStringExtra("USER_ID");
            Intent intent = new Intent(FarmerDashboardActivity.this, MenuActivity.class);
            intent.putExtra("USER_ID", userId); // Pass the User ID to MenuActivity
            startActivity(intent);
        });

        fetchFarmerDetails();
    }

    private void showCropFilterBottomSheet() {
        CropFilterBottomSheetFragment bottomSheetFragment = new CropFilterBottomSheetFragment(this, allCropsMap);
        bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
    }

    private void fetchFarmerDetails() {
        String userId = getIntent().getStringExtra("USER_ID");
        if (userId == null) {
            Log.e(TAG, "User ID is null");
            Toast.makeText(this, "User ID is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                farmerState = snapshot.child("state").getValue(String.class);
                farmerDistrict = snapshot.child("district").getValue(String.class);
                farmerTaluka = snapshot.child("taluka").getValue(String.class);

                if (farmerState == null || farmerDistrict == null || farmerTaluka == null) {
                    Log.e(TAG, "Farmer location details are incomplete.");
                    Toast.makeText(FarmerDashboardActivity.this, "Failed to fetch complete farmer details.", Toast.LENGTH_SHORT).show();
                    return;
                }

                fetchCropListings();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
                Toast.makeText(FarmerDashboardActivity.this, "Failed to fetch farmer details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchCropListings() {
        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> uniqueCropNames = new HashSet<>();
                allCropsMap.clear();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String role = userSnapshot.child("role").getValue(String.class);
                    if ("Trader".equals(role)) {
                        for (DataSnapshot listingSnapshot : userSnapshot.child("Listings").getChildren()) {
                            String state = listingSnapshot.child("state").getValue(String.class);
                            String district = listingSnapshot.child("district").getValue(String.class);
                            String taluka = listingSnapshot.child("taluka").getValue(String.class);
                            String cropName = listingSnapshot.child("cropName").getValue(String.class);

                            // Null checks for all location and crop fields
                            if (state == null || district == null || taluka == null || cropName == null) {
                                Log.w(TAG, "Incomplete listing data for a trader. Skipping listing.");
                                continue;
                            }

                            // Only add crop if it matches farmer's location and hasn't been added yet
                            if (farmerState.equals(state) && farmerDistrict.equals(district) && farmerTaluka.equals(taluka) && !uniqueCropNames.contains(cropName)) {
                                CropListing cropListing = new CropListing();
                                cropListing.setCropName(cropName);
                                cropListing.setImageUrl(listingSnapshot.child("imageUrl").getValue(String.class));
                                cropListing.setUserId(userSnapshot.getKey());

                                uniqueCropNames.add(cropName);
                                allCropsMap.put(cropName, cropListing);
                            }
                        }
                    }
                }

                cropListingList.clear();
                cropListingList.addAll(allCropsMap.values());
                cropImageAdapter.notifyDataSetChanged();

                int newCropCount = cropListingList.size();
                int newlyListedCrops = newCropCount - previousCropCount;

                if (newlyListedCrops > 0) {
                    Toast.makeText(FarmerDashboardActivity.this, newlyListedCrops + " new listings found!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(FarmerDashboardActivity.this, "No new listings found.", Toast.LENGTH_SHORT).show();
                }

                previousCropCount = newCropCount;
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load crop listings: " + error.getMessage());
                Toast.makeText(FarmerDashboardActivity.this, "Failed to load crop listings.", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onCropsSelected(List<String> selectedCrops) {
        progressBar.setVisibility(View.VISIBLE);

        cropListingList.clear();
        for (CropListing crop : allCropsMap.values()) {
            if (selectedCrops.contains(crop.getCropName())) {
                cropListingList.add(crop);
            }
        }

        cropImageAdapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
    }
}
