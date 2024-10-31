package org.smartgrains.krishimitra;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import java.util.List;

public class TraderListActivity extends AppCompatActivity {
    private static final String TAG = "TraderListActivity";
    private RecyclerView traderRecyclerView;
    private ProgressBar progressBar;
    private TextView cropNameTextView; // New TextView for displaying crop name
    private TraderAdapter traderAdapter;
    private List<Trader> traderList;
    private String cropName, farmerState, farmerDistrict, farmerTaluka;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trader_list);

        // Make status bar transparent
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        // Initialize views
        traderRecyclerView = findViewById(R.id.traderRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        cropNameTextView = findViewById(R.id.cropNameTextView); // Reference to crop name TextView
        traderList = new ArrayList<>();

        // Get data from the Intent and validate it
        cropName = getIntent().getStringExtra("CROP_NAME");
        farmerState = getIntent().getStringExtra("FARMER_STATE");
        farmerDistrict = getIntent().getStringExtra("FARMER_DISTRICT");
        farmerTaluka = getIntent().getStringExtra("FARMER_TALUKA");

        if (cropName == null || farmerState == null || farmerDistrict == null || farmerTaluka == null) {
            Toast.makeText(this, "Incomplete information provided.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Missing required data from Intent.");
            finish(); // Exit the activity if data is incomplete
            return;
        }

        // Set the crop name at the top
        cropNameTextView.setText(cropName);

        // Set up the RecyclerView
        traderAdapter = new TraderAdapter(this, traderList);
        traderRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        traderRecyclerView.setAdapter(traderAdapter);

        // Fetch trader listings based on the crop and farmer's location
        fetchTraderListings();
    }

    private void fetchTraderListings() {
        progressBar.setVisibility(View.VISIBLE);
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Query all users to find traders
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                traderList.clear(); // Clear previous listings

                // Loop through all users to find traders and their crop listings
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String role = userSnapshot.child("role").getValue(String.class);
                    if ("Trader".equals(role)) {
                        // Check if this trader has listings
                        for (DataSnapshot listingSnapshot : userSnapshot.child("Listings").getChildren()) {
                            String listingCropName = listingSnapshot.child("cropName").getValue(String.class);
                            String state = listingSnapshot.child("state").getValue(String.class);
                            String district = listingSnapshot.child("district").getValue(String.class);
                            String taluka = listingSnapshot.child("taluka").getValue(String.class);

                            // Check if the crop name and location match
                            if (cropName.equals(listingCropName) && farmerState.equals(state)
                                    && farmerDistrict.equals(district) && farmerTaluka.equals(taluka)) {

                                Trader trader = new Trader();
                                // Fetch trader details safely
                                trader.setTraderName(userSnapshot.child("firstName").getValue(String.class));
                                trader.setShopName(userSnapshot.child("shopName").getValue(String.class));
                                trader.setShopAddress(userSnapshot.child("shopAddress").getValue(String.class));
                                trader.setMinPrice(listingSnapshot.child("minPrice").getValue(String.class));
                                trader.setMaxPrice(listingSnapshot.child("maxPrice").getValue(String.class));
                                trader.setQuantity(listingSnapshot.child("quantity").getValue(String.class));
                                trader.setUnit(listingSnapshot.child("unit").getValue(String.class));
                                trader.setPhoneNumber(userSnapshot.child("phoneNumber").getValue(String.class));

                                // Set the user ID (key of the userSnapshot)
                                trader.setUserId(userSnapshot.getKey());

                                // Log trader details
                                Log.d(TAG, "Trader Found: " + trader.getTraderName() + ", Shop: " + trader.getShopName());

                                traderList.add(trader); // Add the trader to the list
                            }
                        }
                    }
                }

                // Notify the adapter of the data change
                traderAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

                // Check if trader list is empty and show a message
                if (traderList.isEmpty()) {
                    Toast.makeText(TraderListActivity.this, "No traders found for this crop in your area.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
                Toast.makeText(TraderListActivity.this, "Failed to load trader listings. Please try again later.", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
