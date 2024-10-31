package org.smartgrains.krishimitra;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TraderDashboardActivity extends AppCompatActivity {

    private ImageView userProfile;
    private TextView appName;
    private LinearLayout listCropLayout, viewListingsLayout; // Change to LinearLayout

    private DatabaseReference databaseReference;
    private String userId;

    // Variables to store user details
    private String state, district, taluka;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trader_dashboard);

        // Make status bar transparent
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        try {
            // Initialize Views
            userProfile = findViewById(R.id.userProfile);
            appName = findViewById(R.id.appName);
            listCropLayout = findViewById(R.id.listCropLayout); // Update to reference LinearLayout
            viewListingsLayout = findViewById(R.id.viewListingsLayout); // Update to reference LinearLayout

            // Get User ID from Intent
            userId = getIntent().getStringExtra("USER_ID");

            if (userId == null) {
                throw new NullPointerException("User ID is missing in the intent extras.");
            }

            // Initialize Firebase Database reference
            databaseReference = FirebaseDatabase.getInstance().getReference("Users");

            // Fetch user details from the database
            fetchUserDetails(userId);

            // Set onClickListener for List Crop Layout
            listCropLayout.setOnClickListener(v -> openListCropActivity());

            // Set onClickListener for View Listings Layout
            viewListingsLayout.setOnClickListener(v -> openEditCropListingActivity());

            // Set onClickListener for User Profile
            userProfile.setOnClickListener(v -> openMenuActivity());

        } catch (Exception e) {
            Log.e("TraderDashboard", "Initialization error: " + e.getMessage());
            Toast.makeText(this, "Error initializing dashboard. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
    private void fetchUserDetails(String userId) {
        databaseReference.child(userId).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.exists()) {
                        state = dataSnapshot.child("state").getValue(String.class);
                        district = dataSnapshot.child("district").getValue(String.class);
                        taluka = dataSnapshot.child("taluka").getValue(String.class);

                    } else {
                        Toast.makeText(TraderDashboardActivity.this, "User details not found", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("TraderDashboard", "Data processing error: " + e.getMessage());
                    Toast.makeText(TraderDashboardActivity.this, "Error processing user details.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("TraderDashboard", "Database error: " + databaseError.getMessage());
                Toast.makeText(TraderDashboardActivity.this, "Error fetching user details. Please check your connection.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openListCropActivity() {
        try {
            Intent intent = new Intent(TraderDashboardActivity.this, ListCropActivity.class);
            intent.putExtra("USER_ID", userId);
            intent.putExtra("STATE", state);
            intent.putExtra("DISTRICT", district);
            intent.putExtra("TALUKA", taluka);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("TraderDashboard", "Error opening List Crop activity: " + e.getMessage());
            Toast.makeText(this, "Error opening List Crop page.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openEditCropListingActivity() {
        try {
            Intent intent = new Intent(TraderDashboardActivity.this, EditCropListingActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("TraderDashboard", "Error opening Edit Crop Listing activity: " + e.getMessage());
            Toast.makeText(this, "Error opening Edit Crop Listing page.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openMenuActivity() {
        try {
            Intent intent = new Intent(TraderDashboardActivity.this, MenuActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("TraderDashboard", "Error opening Menu activity: " + e.getMessage());
            Toast.makeText(this, "Error opening Menu page.", Toast.LENGTH_SHORT).show();
        }
    }
}
