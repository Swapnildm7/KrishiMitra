package org.smartgrains.krishimitra;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
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

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "FarmerPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private Button buttonLogout;

    // UI Components
    private Set<String> previousListings = new HashSet<>(); // To store previous listings
    private TextView textViewListingStatus, tvNoListingsFound;
    private ProgressBar progressBar;

    @SuppressLint("CutPasteId")
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
        progressBar = findViewById(R.id.progressBar);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize RecyclerView
        cropImageRecyclerView = findViewById(R.id.cropImageRecyclerView);
        cropImageList = new ArrayList<>();
        uniqueCropNames = new HashSet<>(); // Initialize the Set for unique crop names
        cropImageRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Fetch trader ID from intent
        traderId = getIntent().getStringExtra("USER_ID");
        fetchLocationData(); // This method fetches initial crop listings or location data

        Button cropFilterButton = findViewById(R.id.cropFilterButton);
        cropFilterButton.setOnClickListener(v -> showCropFilterBottomSheet());

        // Handle user profile click
        ImageView userProfileImage = findViewById(R.id.userProfile);
        userProfileImage.setOnClickListener(v -> {
            Intent intent = new Intent(FarmerDashboardActivity.this, MenuActivity.class);
            intent.putExtra("USER_ID", traderId); // Pass traderId to MenuActivity
            startActivity(intent);
        });

        // Initialize Refresh Button
        Button refreshButton = findViewById(R.id.buttonRefresh);
        refreshButton.setOnClickListener(v -> {
            // Call the method to fetch updated data
            fetchLocationData(); // You can replace this with your actual refresh logic
        });
    }

    private void fetchLocationData() {
        Log.d("FarmerDashboardActivity", "Fetching location data for traderId: " + traderId);

        databaseReference.child(traderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    selectedState = dataSnapshot.child("state").getValue(String.class);
                    selectedDistrict = dataSnapshot.child("district").getValue(String.class);
                    selectedTaluka = dataSnapshot.child("taluka").getValue(String.class);

                    if (selectedState != null && selectedDistrict != null && selectedTaluka != null) {
                        cropImageAdapter = new CropImageAdapter(FarmerDashboardActivity.this, cropImageList, selectedState, selectedDistrict, selectedTaluka);
                        cropImageRecyclerView.setAdapter(cropImageAdapter);
                        fetchCropListings();
                    } else {
                        Toast.makeText(FarmerDashboardActivity.this, "Location data is incomplete", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("FarmerDashboardActivity", "DataSnapshot for traderId is empty.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FarmerDashboardActivity", "Error fetching location data: " + databaseError.getMessage());
            }
        });
    }

    private void fetchCropListings() {
        progressBar.setVisibility(View.VISIBLE); // Show progress bar while loading

        databaseReference.orderByChild("state").equalTo(selectedState)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot stateSnapshot) {
                        cropImageList.clear();
                        uniqueCropNames.clear();
                        Set<String> currentListings = new HashSet<>(); // To store the current listings

                        for (DataSnapshot traderSnapshot : stateSnapshot.getChildren()) {
                            String traderDistrict = traderSnapshot.child("district").getValue(String.class);
                            String traderTaluka = traderSnapshot.child("taluka").getValue(String.class);

                            if (selectedDistrict.equals(traderDistrict) && selectedTaluka.equals(traderTaluka)) {
                                DataSnapshot listingsSnapshot = traderSnapshot.child("Listings");
                                for (DataSnapshot listing : listingsSnapshot.getChildren()) {
                                    CropListingModel cropListing = listing.getValue(CropListingModel.class);
                                    if (cropListing != null && uniqueCropNames.add(cropListing.getCropName())) {
                                        int imageResId = getImageResource(cropListing.getCropName());
                                        cropImageList.add(new CropImageModel(cropListing.getCropName(), imageResId));
                                        currentListings.add(cropListing.getCropName()); // Track current listings
                                    }
                                }
                            }
                        }

                        progressBar.setVisibility(View.GONE); // Hide progress bar after loading

                        // Compare the current listings with the previous listings
                        Set<String> newListings = new HashSet<>(currentListings);
                        newListings.removeAll(previousListings); // Only keep new listings
                        if (!newListings.isEmpty()) {
                            // Display the number of new listings found using a Toast
                            Toast.makeText(getApplicationContext(), newListings.size() + " new listings found", Toast.LENGTH_SHORT).show();

                            // Optionally, you can use a Handler to delay any other actions, but a Toast will automatically disappear after a short time.
                        } else {
                            // Display a message when no new listings are found
                            Toast.makeText(getApplicationContext(), "No new listings found", Toast.LENGTH_SHORT).show();
                        }

                        previousListings.clear(); // Update previous listings
                        previousListings.addAll(currentListings);

                        cropImageAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressBar.setVisibility(View.GONE); // Hide progress bar on error
                        Log.e("FarmerDashboardActivity", "Error fetching crop listings: " + databaseError.getMessage());
                    }
                });
    }

    @Override
    public void onCropsSelected(List<String> crops) {
        selectedCrops.clear();
        selectedCrops.addAll(crops);
        fetchFilteredCropListings();
    }

    private void fetchFilteredCropListings() {
        databaseReference.orderByChild("state").equalTo(selectedState)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot stateSnapshot) {
                        cropImageList.clear();
                        uniqueCropNames.clear(); // Clear the Set for unique crop names

                        for (DataSnapshot traderSnapshot : stateSnapshot.getChildren()) {
                            String traderDistrict = traderSnapshot.child("district").getValue(String.class);
                            String traderTaluka = traderSnapshot.child("taluka").getValue(String.class);

                            if (selectedDistrict != null && selectedTaluka != null &&
                                    selectedDistrict.equals(traderDistrict) && selectedTaluka.equals(traderTaluka)) {
                                DataSnapshot listingsSnapshot = traderSnapshot.child("Listings");

                                for (DataSnapshot listing : listingsSnapshot.getChildren()) {
                                    CropListingModel cropListing = listing.getValue(CropListingModel.class);

                                    if (cropListing != null) {
                                        String cropName = cropListing.getCropName();
                                        if (selectedCrops.isEmpty() || selectedCrops.contains(cropName)) {
                                            if (uniqueCropNames.add(cropName)) {
                                                int imageResId = getImageResource(cropName);
                                                cropImageList.add(new CropImageModel(cropName, imageResId));
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        cropImageAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("FarmerDashboardActivity", "Error fetching filtered crop listings: " + databaseError.getMessage());
                    }
                });
    }

    private int getImageResource(String cropName) {
        String resourceName = cropName.toLowerCase().replace(" ", "_") + "_image";
        int resId = getResources().getIdentifier(resourceName, "drawable", getPackageName());

        if (resId == 0) {
            Log.e("FarmerDashboardActivity", "Error: Image resource not found for crop: " + cropName);
        }
        return resId;
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
