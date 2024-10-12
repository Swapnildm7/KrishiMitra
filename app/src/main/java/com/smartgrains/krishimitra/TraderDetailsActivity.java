package com.smartgrains.krishimitra;

import android.os.Bundle;
import android.util.Log;
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

public class TraderDetailsActivity extends AppCompatActivity {
    private static final String TAG = "TraderDetailsActivity";
    private String cropName;
    private String selectedState;
    private String selectedDistrict;
    private String selectedTaluka;
    private DatabaseReference databaseReference;
    private RecyclerView recyclerView;
    private TraderAdapter traderAdapter;
    private List<TraderModel> traderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trader_details);

        // Make status bar transparent
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        // Retrieve data from intent
        cropName = getIntent().getStringExtra("CROP_NAME");
        selectedState = getIntent().getStringExtra("STATE");
        selectedDistrict = getIntent().getStringExtra("DISTRICT");
        selectedTaluka = getIntent().getStringExtra("TALUKA");

        // Log the values to ensure they're updated
        Log.d(TAG, "Crop Name: " + cropName);
        Log.d(TAG, "State: " + selectedState);
        Log.d(TAG, "District: " + selectedDistrict);
        Log.d(TAG, "Taluka: " + selectedTaluka);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Set up RecyclerView
        recyclerView = findViewById(R.id.traderRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        traderList = new ArrayList<>();
        traderAdapter = new TraderAdapter(traderList);
        recyclerView.setAdapter(traderAdapter);

        // Fetch trader details based on crop name and location
        fetchTraderDetails();
    }

    private void fetchTraderDetails() {
        // Clear previous trader list before fetching new data
        traderList.clear();
        traderAdapter.notifyDataSetChanged();

        Log.d(TAG, "Fetching traders for crop: " + cropName + ", State: " + selectedState +
                ", District: " + selectedDistrict + ", Taluka: " + selectedTaluka);

        // Query traders by state
        databaseReference.orderByChild("state").equalTo(selectedState)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot stateSnapshot) {
                        Log.d(TAG, "Number of traders in state " + selectedState + ": " + stateSnapshot.getChildrenCount());
                        boolean traderFound = false;

                        // Iterate through each trader in the state
                        for (DataSnapshot traderSnapshot : stateSnapshot.getChildren()) {
                            String traderDistrict = traderSnapshot.child("district").getValue(String.class);
                            String traderTaluka = traderSnapshot.child("taluka").getValue(String.class);
                            String traderId = traderSnapshot.getKey(); // Get the trader ID

                            Log.d(TAG, "Checking trader: " + traderSnapshot.child("firstName").getValue(String.class) +
                                    " from district: " + traderDistrict + ", taluka: " + traderTaluka);

                            // Check if the trader matches the selected district and taluka
                            if (selectedDistrict.trim().equals(traderDistrict.trim()) &&
                                    selectedTaluka.trim().equals(traderTaluka.trim())) {

                                DataSnapshot listingsSnapshot = traderSnapshot.child("Listings");
                                Log.d(TAG, "Listings for trader: " + traderSnapshot.child("firstName").getValue(String.class) +
                                        ", Listings count: " + listingsSnapshot.getChildrenCount());

                                // Check if the trader has listed the selected crop
                                for (DataSnapshot listing : listingsSnapshot.getChildren()) {
                                    CropListingModel cropListing = listing.getValue(CropListingModel.class);
                                    Log.d(TAG, "Checking crop listing: " + (cropListing != null ? cropListing.getCropName() : "null"));

                                    // Check if the crop name matches the selected crop
                                    if (cropListing != null && cropListing.getCropName().equals(cropName)) {
                                        traderFound = true;

                                        // Create TraderModel and add to the list with trader ID
                                        TraderModel traderModel = new TraderModel(
                                                traderSnapshot.child("firstName").getValue(String.class) + " " +
                                                        traderSnapshot.child("lastName").getValue(String.class),
                                                traderSnapshot.child("shopName").getValue(String.class),
                                                traderSnapshot.child("shopAddress").getValue(String.class),
                                                traderSnapshot.child("phoneNumber").getValue(String.class),
                                                cropListing.getMinPrice(),
                                                cropListing.getMaxPrice(),
                                                cropListing.getQuantity(),
                                                cropListing.getUnit(), // Add unit here
                                                selectedState,
                                                selectedDistrict,
                                                selectedTaluka,
                                                traderId // Pass trader ID
                                        );

                                        traderList.add(traderModel);
                                        Log.d(TAG, "Trader added: " + traderModel.getTraderName() + ", Shop: " + traderModel.getShopName());
                                    }
                                }
                            }
                        }

                        if (!traderFound) {
                            Toast.makeText(TraderDetailsActivity.this, "No traders found for this crop in the selected location.", Toast.LENGTH_SHORT).show();
                        }

                        // Notify adapter once after all traders are processed
                        traderAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Error fetching trader details: " + databaseError.getMessage());
                    }
                });
    }
}
