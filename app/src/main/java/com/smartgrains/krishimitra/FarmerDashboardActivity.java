package com.smartgrains.krishimitra;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FarmerDashboardActivity extends AppCompatActivity implements CropFilterBottomSheetFragment.OnCropsSelectedListener {

    private String selectedState;
    private String selectedDistrict;
    private String selectedTaluka;

    private List<String> selectedCrops = new ArrayList<>(); // List to store selected crops
    private DatabaseReference databaseReference;
    private String traderId; // Trader ID from login session
    private RecyclerView cropImageRecyclerView;
    private CropImageAdapter cropImageAdapter;
    private List<CropImageModel> cropImageList; // List to hold crop images and names
    private Set<String> uniqueCropNames; // Set to store unique crop names

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_dashboard);

        // Make status bar transparent
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize RecyclerView
        cropImageRecyclerView = findViewById(R.id.cropImageRecyclerView);
        cropImageList = new ArrayList<>();
        uniqueCropNames = new HashSet<>(); // Initialize the Set for unique crop names
        cropImageRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Fetch trader ID from intent
        traderId = getIntent().getStringExtra("USER_ID");
        fetchLocationData();

        // Initialize buttons and listeners
        Button selectLocationButton = findViewById(R.id.locationFilterButton);
        selectLocationButton.setOnClickListener(v -> showLocationBottomSheet());

        Button cropFilterButton = findViewById(R.id.cropFilterButton);
        cropFilterButton.setOnClickListener(v -> showCropFilterBottomSheet());
    }

    private void fetchLocationData() {
        Log.d("farmerDashboardActivity", "Fetching location data for traderId: " + traderId);
        databaseReference.child(traderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    selectedState = dataSnapshot.child("state").getValue(String.class);
                    selectedDistrict = dataSnapshot.child("district").getValue(String.class);
                    selectedTaluka = dataSnapshot.child("taluka").getValue(String.class);

                    if (selectedState != null && selectedDistrict != null && selectedTaluka != null) {
                        Log.d("FarmerDashboardActivity", "Location fetched: " + selectedState + ", " + selectedDistrict + ", " + selectedTaluka);

                        cropImageAdapter = new CropImageAdapter(FarmerDashboardActivity.this, cropImageList, selectedState, selectedDistrict, selectedTaluka);
                        cropImageRecyclerView.setAdapter(cropImageAdapter);
                        fetchCropListings();
                    } else {
                        Log.e("FarmerDashboardActivity", "Location data is incomplete");
                        Toast.makeText(FarmerDashboardActivity.this, "Location data is incomplete", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("FarmerDashboardActivity", "Failed to fetch location data");
                    Toast.makeText(FarmerDashboardActivity.this, "Failed to fetch location data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FarmerDashboardActivity", "Error fetching location data: " + databaseError.getMessage());
                Toast.makeText(FarmerDashboardActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchCropListings() {
        // Fetch crop listings based on selected state, district, and taluka
        Log.d("FarmerDashboardActivity", "Fetching crop listings for State: " + selectedState +
                ", District: " + selectedDistrict + ", Taluka: " + selectedTaluka);

        // Query the database for traders in the selected state
        databaseReference.orderByChild("state").equalTo(selectedState)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot stateSnapshot) {
                        Log.d("FarmerDashboardActivity", "State snapshot retrieved: " + stateSnapshot.getChildrenCount() + " traders found.");

                        cropImageList.clear(); // Clear the previous listings
                        uniqueCropNames.clear(); // Clear the Set for unique crop names
                        for (DataSnapshot traderSnapshot : stateSnapshot.getChildren()) {
                            String traderDistrict = traderSnapshot.child("district").getValue(String.class);
                            String traderTaluka = traderSnapshot.child("taluka").getValue(String.class);

                            // Check if district and taluka match the selected values
                            if (selectedDistrict != null && selectedTaluka != null &&
                                    selectedDistrict.equals(traderDistrict) && selectedTaluka.equals(traderTaluka)) {
                                DataSnapshot listingsSnapshot = traderSnapshot.child("Listings");

                                for (DataSnapshot listing : listingsSnapshot.getChildren()) {
                                    CropListingModel cropListing = listing.getValue(CropListingModel.class);
                                    if (cropListing != null) {
                                        // Log the crop details
                                        String cropName = cropListing.getCropName();
                                        String minPrice = cropListing.getMinPrice();
                                        String maxPrice = cropListing.getMaxPrice();
                                        String quantity = cropListing.getQuantity();
                                        String unit = cropListing.getUnit();
                                        Log.d("FarmerDashboardActivity", "Fetched Crop: " + cropName + ", Min Price: " + minPrice + ", Max Price: " + maxPrice + ", Quantity: " + quantity + ", Unit: " + unit);

                                        // Add crop image and name to the list only if it's not already added
                                        if (uniqueCropNames.add(cropName)) { // Add crop name to the Set
                                            int imageResId = getImageResource(cropName); // Get image resource ID based on crop name
                                            cropImageList.add(new CropImageModel(cropName, imageResId));
                                        }
                                    } else {
                                        Log.e("FarmerDashboardActivity", "Error: CropListing object is null for listing: " + listing.getKey());
                                    }
                                }
                            } else {
                                Log.d("FarmerDashboardActivity", "Skipping listing - District/Taluka mismatch: " + traderDistrict + "/" + traderTaluka);
                            }
                        }

                        cropImageAdapter.notifyDataSetChanged(); // Notify the adapter about the updated list
                        Log.d("FarmerDashboardActivity", "Finished fetching crop listings");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("FarmerDashboardActivity", "Error fetching crop listings: " + databaseError.getMessage());
                    }
                });
    }

    private int getImageResource(String cropName) {
        String resourceName = cropName.toLowerCase().replace(" ", "_") + "_image";
        return getResources().getIdentifier(resourceName, "drawable", getPackageName());
    }

    @Override
    public void onCropsSelected(List<String> crops) {
        selectedCrops.clear();
        selectedCrops.addAll(crops);
        fetchFilteredCropListings();
    }

    private void fetchFilteredCropListings() {
        Log.d("FarmerDashboardActivity", "Fetching filtered crop listings for State: " + selectedState);
        databaseReference.orderByChild("state").equalTo(selectedState)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot stateSnapshot) {
                        cropImageList.clear();
                        uniqueCropNames.clear(); // Clear the Set for unique crop names
                        for (DataSnapshot traderSnapshot : stateSnapshot.getChildren()) {
                            String traderDistrict = traderSnapshot.child("district").getValue(String.class);
                            String traderTaluka = traderSnapshot.child("taluka").getValue(String.class);

                            if (selectedDistrict.equals(traderDistrict) && selectedTaluka.equals(traderTaluka)) {
                                DataSnapshot listingsSnapshot = traderSnapshot.child("Listings");
                                for (DataSnapshot listing : listingsSnapshot.getChildren()) {
                                    CropListingModel cropListing = listing.getValue(CropListingModel.class);

                                    if (cropListing != null && (selectedCrops.isEmpty() || selectedCrops.contains(cropListing.getCropName()))) {
                                        if (uniqueCropNames.add(cropListing.getCropName())) { // Add crop name to the Set
                                            int imageResId = getImageResource(cropListing.getCropName());
                                            cropImageList.add(new CropImageModel(cropListing.getCropName(), imageResId));
                                        }
                                    } else {
                                        Log.e("FarmerDashboardActivity", "Error: CropListing object is null for listing: " + listing.getKey());
                                    }
                                }
                            } else {
                                Log.d("FarmerDashboardActivity", "Skipping listing - District/Taluka mismatch: " + traderDistrict + "/" + traderTaluka);
                            }
                        }

                        cropImageAdapter.notifyDataSetChanged(); // Notify the adapter about the updated list
                        Log.d("FarmerDashboardActivity", "Finished fetching filtered crop listings");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("FarmerDashboardActivity", "Error fetching filtered crop listings: " + databaseError.getMessage());
                    }
                });
    }
    private void showCropFilterBottomSheet() {
        CropFilterBottomSheetFragment filterBottomSheet = new CropFilterBottomSheetFragment();
        filterBottomSheet.setOnCropsSelectedListener(this);
        filterBottomSheet.show(getSupportFragmentManager(), "CropFilterBottomSheet");
    }

    private void showLocationBottomSheet() {
        LocationBottomSheetFragment locationBottomSheet = new LocationBottomSheetFragment();
        locationBottomSheet.show(getSupportFragmentManager(), "LocationBottomSheet");
    }

    public void onLocationSelected(String state, String district, String taluka) {
        this.selectedState = state;
        this.selectedDistrict = district;
        this.selectedTaluka = taluka;
        fetchCropListings();
    }
}


