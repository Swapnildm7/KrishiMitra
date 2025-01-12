package org.smartgrains.krishimitra;

import android.content.Context;
import android.content.SharedPreferences;
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
import java.util.Map;

public class TraderListActivity extends AppCompatActivity {
    private static final String TAG = "TraderListActivity";
    private RecyclerView traderRecyclerView;
    private ProgressBar progressBar;
    private TextView cropNameTextView, textMessageCrop; // Reference to crop name TextView and message TextView
    private TraderAdapter traderAdapter;
    private List<Trader> traderList;
    private String cropName, farmerState, farmerDistrict, farmerTaluka, farmerUserId;
    private DatabaseReference databaseReference;
    private DatabaseReference translatedCropNamesRef;
    private String userLanguageCode;

    // Define a map to associate language codes with Firebase translation keys
    private static final Map<String, String> LANGUAGE_TRANSLATION_MAP = Map.of(
            "mr", "Marathi",
            "kn", "Kannada",
            "hi", "Hindi",
            "en", "English"
    );

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
        textMessageCrop = findViewById(R.id.textMessageCrop); // Reference to message TextView
        traderList = new ArrayList<>();

        // Get data from the Intent and validate it
        cropName = getIntent().getStringExtra("CROP_NAME");
        farmerState = getIntent().getStringExtra("FARMER_STATE");
        farmerDistrict = getIntent().getStringExtra("FARMER_DISTRICT");
        farmerTaluka = getIntent().getStringExtra("FARMER_TALUKA");
        farmerUserId = getIntent().getStringExtra("USER_ID");

        if (cropName == null || farmerState == null || farmerDistrict == null || farmerTaluka == null) {
            Toast.makeText(this, "Incomplete information provided.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Missing required data from Intent.");
            finish(); // Exit the activity if data is incomplete
            return;
        }

        // Retrieve user's preferred language code from SharedPreferences
        SharedPreferences preferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        userLanguageCode = preferences.getString("LanguageCode", "en").trim().toLowerCase();

        // Initialize Firebase reference to "TranslatedCropNames" node
        translatedCropNamesRef = FirebaseDatabase.getInstance().getReference("TranslatedCropNames");

        // Fetch the translated crop name
        fetchTranslatedCropName();

        // Set up the RecyclerView
        traderAdapter = new TraderAdapter(this, traderList, farmerUserId);
        traderRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        traderRecyclerView.setAdapter(traderAdapter);

        // Fetch trader listings based on the crop and farmer's location
        fetchTraderListings();
    }

    private void fetchTranslatedCropName() {
        // Fetch the translated crop name from Firebase
        translatedCropNamesRef.child(cropName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Get the translated name from Firebase if available
                            String translatedName = snapshot.child(LANGUAGE_TRANSLATION_MAP.getOrDefault(userLanguageCode, "English"))
                                    .getValue(String.class);

                            // Set the translated name or fallback to original crop name
                            cropNameTextView.setText(translatedName != null ? translatedName : cropName);
                        } else {
                            // If translation is not found, fallback to the original crop name
                            cropNameTextView.setText(cropName);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle Firebase errors gracefully
                        cropNameTextView.setText(cropName);
                    }
                });
    }

    private void fetchTraderListings() {
        progressBar.setVisibility(View.VISIBLE);
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                traderList.clear(); // Clear previous listings

                // Separate lists for hierarchical fallback
                List<DataSnapshot> talukaListings = new ArrayList<>();
                List<DataSnapshot> districtListings = new ArrayList<>();
                List<DataSnapshot> stateListings = new ArrayList<>();
                List<DataSnapshot> allListings = new ArrayList<>();

                // Organize listings into hierarchical buckets
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String role = userSnapshot.child("role").getValue(String.class);
                    if ("Trader".equals(role)) {
                        for (DataSnapshot listingSnapshot : userSnapshot.child("Listings").getChildren()) {
                            String listingCropName = listingSnapshot.child("cropName").getValue(String.class);
                            String state = listingSnapshot.child("state").getValue(String.class);
                            String district = listingSnapshot.child("district").getValue(String.class);
                            String taluka = listingSnapshot.child("taluka").getValue(String.class);

                            // Ensure required fields are present
                            if (listingCropName == null || state == null || district == null || taluka == null) {
                                Log.w(TAG, "Incomplete trader data: " + userSnapshot.getKey() + ". Skipping this listing.");
                                continue;
                            }

                            // Categorize listings by location
                            if (cropName.equals(listingCropName) && farmerTaluka.equals(taluka) && farmerDistrict.equals(district) && farmerState.equals(state)) {
                                talukaListings.add(userSnapshot);
                            } else if (cropName.equals(listingCropName) && farmerDistrict.equals(district) && farmerState.equals(state)) {
                                districtListings.add(userSnapshot);
                            } else if (cropName.equals(listingCropName) && farmerState.equals(state)) {
                                stateListings.add(userSnapshot);
                            } else if (cropName.equals(listingCropName)) {
                                allListings.add(userSnapshot);
                            }
                        }
                    }
                }

                // Process listings in fallback order and display relevant messages
                boolean listingsFound = processTraderListings(talukaListings, "Traders in " + farmerTaluka);
                if (!listingsFound) {
                    listingsFound = processTraderListings(districtListings, "No Traders found in your Location. Traders in your District " + farmerDistrict);
                }
                if (!listingsFound) {
                    listingsFound = processTraderListings(stateListings, "No Traders found in your District. Traders in your State " + farmerState);
                }
                if (!listingsFound) {
                    processTraderListings(allListings, "No traders found in your state. Showing all available traders.");
                }

                // Notify the adapter of the data change
                traderAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

                // Display message if no traders were found
                if (traderList.isEmpty()) {
                    textMessageCrop.setText("No traders found for this crop.");
                    Toast.makeText(TraderListActivity.this, "No traders found for this crop.", Toast.LENGTH_LONG).show();
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

    // Helper function to process trader listings and display a message
    private boolean processTraderListings(List<DataSnapshot> listings, String message) {
        boolean addedListings = false;

        for (DataSnapshot userSnapshot : listings) {
            Trader trader = new Trader();
            trader.setTraderName(userSnapshot.child("firstName").getValue(String.class));
            trader.setShopName(userSnapshot.child("shopName").getValue(String.class));
            trader.setShopAddress(userSnapshot.child("shopAddress").getValue(String.class));
            trader.setPhoneNumber(userSnapshot.child("phoneNumber").getValue(String.class));
            trader.setUserId(userSnapshot.getKey());

            // Loop through trader listings for crop-specific details
            for (DataSnapshot listingSnapshot : userSnapshot.child("Listings").getChildren()) {
                String listingCropName = listingSnapshot.child("cropName").getValue(String.class);
                if (cropName.equals(listingCropName)) {
                    trader.setMinPrice(listingSnapshot.child("minPrice").getValue(String.class));
                    trader.setMaxPrice(listingSnapshot.child("maxPrice").getValue(String.class));
                    trader.setQuantity(listingSnapshot.child("quantity").getValue(String.class));
                    trader.setUnit(listingSnapshot.child("unit").getValue(String.class));
                    break; // We only need one relevant listing per trader
                }
            }

            // Add trader to the list if not already present
            if (!traderList.contains(trader)) {
                traderList.add(trader);
                addedListings = true;
            }
        }

        if (addedListings) {
            textMessageCrop.setText(message); // Update message TextView with appropriate message
            Toast.makeText(TraderListActivity.this, message, Toast.LENGTH_SHORT).show();
        }
        return addedListings;
    }
}
