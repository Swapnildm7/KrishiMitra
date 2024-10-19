package org.smartgrains.krishimitra;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TraderDashboardActivity extends AppCompatActivity {

    private String selectedState;
    private String selectedDistrict;
    private String selectedTaluka;

    private List<String> selectedCrops = new ArrayList<>(); // List to store selected crops
    private DatabaseReference databaseReference;
    private String traderId; // Trader ID from login session
    private CropImageAdapter cropImageAdapter;
    private List<CropImageModel> cropImageList = new ArrayList<>(); // Initialize the list to hold crop images and names

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trader_dashboard);

        // Make status bar transparent
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Fetch trader ID from intent
        traderId = getIntent().getStringExtra("USER_ID");

        // Initialize UI elements
        ImageView userProfile = findViewById(R.id.userProfile);
        TextView appName = findViewById(R.id.appName);

        Button listCropButton = findViewById(R.id.listCropButton);
        Button viewListingButton = findViewById(R.id.viewListingButton);

        // Set click listeners for buttons
        listCropButton.setOnClickListener(v -> openCropListing());
        viewListingButton.setOnClickListener(v -> openViewListings());

        // Handle user profile image click to navigate to MenuActivity
        userProfile.setOnClickListener(v -> {
            Intent intent = new Intent(TraderDashboardActivity.this, MenuActivity.class);
            intent.putExtra("USER_ID", traderId);  // Pass the trader ID to the MenuActivity
            startActivity(intent);
        });

        // Fetch location data after initializing buttons
        fetchLocationData();
    }

    private void fetchLocationData() {
        Log.d("TraderDashboardActivity", "Fetching location data for traderId: " + traderId);
        databaseReference.child(traderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    selectedState = dataSnapshot.child("state").getValue(String.class);
                    selectedDistrict = dataSnapshot.child("district").getValue(String.class);
                    selectedTaluka = dataSnapshot.child("taluka").getValue(String.class);

                    if (selectedState != null && selectedDistrict != null && selectedTaluka != null) {
                        Log.d("TraderDashboardActivity", "Location fetched: " + selectedState + ", " + selectedDistrict + ", " + selectedTaluka);
                    } else {
                        Log.e("TraderDashboardActivity", "Location data is incomplete");
                        Toast.makeText(TraderDashboardActivity.this, "Location data is incomplete", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("TraderDashboardActivity", "Failed to fetch location data");
                    Toast.makeText(TraderDashboardActivity.this, "Failed to fetch location data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("TraderDashboardActivity", "Error fetching location data: " + databaseError.getMessage());
                Toast.makeText(TraderDashboardActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openCropListing() {
        if (selectedState != null && selectedDistrict != null && selectedTaluka != null) {
            Intent intent = new Intent(TraderDashboardActivity.this, CropListing.class);
            intent.putExtra("USER_ID", traderId);  // Pass the trader ID
            intent.putExtra("State", selectedState);
            intent.putExtra("District", selectedDistrict);
            intent.putExtra("Taluka", selectedTaluka);
            intent.putStringArrayListExtra("SelectedCrops", new ArrayList<>(selectedCrops));
            startActivity(intent);
        } else {
            Toast.makeText(this, "Location data is incomplete", Toast.LENGTH_SHORT).show();
        }
    }

    private void openViewListings() {
        Intent intent = new Intent(TraderDashboardActivity.this, TraderListingsActivity.class);
        intent.putExtra("USER_ID", traderId);  // Pass the trader ID
        startActivity(intent);
    }
}
