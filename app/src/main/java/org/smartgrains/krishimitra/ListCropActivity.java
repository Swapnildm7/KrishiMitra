package org.smartgrains.krishimitra;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ListCropActivity extends AppCompatActivity {

    private Spinner spinnerCropName, unitSpinner;
    private EditText etMinPrice, etMaxPrice, etQuantity;
    private Button btnSubmit;
    private ProgressBar progressBar;

    private DatabaseReference cropRef, listingsRef;
    private String traderId;
    private String locationState, locationDistrict, locationTaluka;
    private List<String> cropNames;
    private String cropImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_crop);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        spinnerCropName = findViewById(R.id.spinner_crop_name);
        etMinPrice = findViewById(R.id.et_min_price);
        etMaxPrice = findViewById(R.id.et_max_price);
        etQuantity = findViewById(R.id.et_quantity);
        unitSpinner = findViewById(R.id.unit_spinner);
        btnSubmit = findViewById(R.id.btn_submit);
        progressBar = findViewById(R.id.progress_bar); // Add progress bar reference

        cropNames = new ArrayList<>();

        traderId = getIntent().getStringExtra("USER_ID");
        locationState = getIntent().getStringExtra("STATE");
        locationDistrict = getIntent().getStringExtra("DISTRICT");
        locationTaluka = getIntent().getStringExtra("TALUKA");

        cropRef = FirebaseDatabase.getInstance().getReference("CropResource");
        listingsRef = FirebaseDatabase.getInstance().getReference("Users").child(traderId).child("Listings");

        populateCropNamesSpinner();
        populateUnitSpinner();

        btnSubmit.setOnClickListener(v -> checkForDuplicateListing());
    }

    private void populateCropNamesSpinner() {
        cropRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                cropNames.clear();
                for (DataSnapshot cropSnapshot : dataSnapshot.getChildren()) {
                    String cropName = cropSnapshot.child("cropName").getValue(String.class);
                    if (cropName != null) {
                        cropNames.add(cropName);
                    }
                }
                String[] cropArray = cropNames.toArray(new String[0]);
                CustomSpinnerAdapter cropAdapter = new CustomSpinnerAdapter(ListCropActivity.this, cropArray);
                spinnerCropName.setAdapter(cropAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ListCropActivity.this, "Error fetching crop names.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateUnitSpinner() {
        String[] units = getResources().getStringArray(R.array.quantity_units_array);
        CustomSpinnerAdapter unitAdapter = new CustomSpinnerAdapter(this, units);
        unitSpinner.setAdapter(unitAdapter);
    }

    private void checkForDuplicateListing() {
        String selectedCrop = spinnerCropName.getSelectedItem().toString();

        // Disable button and show progress bar
        btnSubmit.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        listingsRef.orderByChild("cropName").equalTo(selectedCrop).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(ListCropActivity.this, "This crop is already listed!", Toast.LENGTH_SHORT).show();
                    resetButtonAndProgressBar();
                } else {
                    fetchCropImageUrl(selectedCrop);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ListCropActivity.this, "Error checking for duplicate listing.", Toast.LENGTH_SHORT).show();
                resetButtonAndProgressBar();
            }
        });
    }

    private void fetchCropImageUrl(String selectedCrop) {
        cropRef.orderByChild("cropName").equalTo(selectedCrop).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot cropSnapshot : snapshot.getChildren()) {
                        cropImageUrl = cropSnapshot.child("imageUrl").getValue(String.class);
                        break;
                    }
                    saveCropListing(selectedCrop);
                } else {
                    Toast.makeText(ListCropActivity.this, "Crop image URL not found.", Toast.LENGTH_SHORT).show();
                    resetButtonAndProgressBar();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ListCropActivity.this, "Error fetching crop image URL.", Toast.LENGTH_SHORT).show();
                resetButtonAndProgressBar();
            }
        });
    }

    private void saveCropListing(String selectedCrop) {
        DatabaseReference persistentIdsRef = FirebaseDatabase.getInstance().getReference("PersistentListingIds")
                .child(traderId).child(selectedCrop);

        persistentIdsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String listingId = snapshot.exists() ? snapshot.getValue(String.class) : listingsRef.push().getKey();

                if (!snapshot.exists()) {
                    persistentIdsRef.setValue(listingId);
                }

                submitCropListing(selectedCrop, listingId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ListCropActivity.this, "Error saving listing.", Toast.LENGTH_SHORT).show();
                resetButtonAndProgressBar();
            }
        });
    }

    private void submitCropListing(String selectedCrop, String listingId) {
        String minPriceStr = etMinPrice.getText().toString().trim();
        String maxPriceStr = etMaxPrice.getText().toString().trim();
        String quantity = etQuantity.getText().toString().trim();
        String unit = unitSpinner.getSelectedItem().toString();

        // Check if all fields are entered
        if (minPriceStr.isEmpty() || maxPriceStr.isEmpty() || quantity.isEmpty()) {
            Toast.makeText(ListCropActivity.this, "Please enter Min Price, Max Price, and Quantity.", Toast.LENGTH_SHORT).show();
            resetButtonAndProgressBar();
            return;
        }

        // Convert price strings to double for comparison
        double minPrice = Double.parseDouble(minPriceStr);
        double maxPrice = Double.parseDouble(maxPriceStr);

        // Validate that max price is not less than min price
        if (maxPrice < minPrice) {
            Toast.makeText(ListCropActivity.this, "Max price cannot be less than min price.", Toast.LENGTH_SHORT).show();
            resetButtonAndProgressBar();
            return;
        }

        // Prepare listing details for database
        HashMap<String, Object> listingDetails = new HashMap<>();
        listingDetails.put("listingId", listingId);
        listingDetails.put("cropName", selectedCrop);
        listingDetails.put("minPrice", minPriceStr);
        listingDetails.put("maxPrice", maxPriceStr);
        listingDetails.put("quantity", quantity);
        listingDetails.put("unit", unit);
        listingDetails.put("state", locationState);
        listingDetails.put("district", locationDistrict);
        listingDetails.put("taluka", locationTaluka);
        listingDetails.put("userId", traderId);
        listingDetails.put("imageUrl", cropImageUrl);

        // Add timestamp
        long currentTimestamp = System.currentTimeMillis();
        listingDetails.put("timestamp", getReadableTimestamp(currentTimestamp));

        // Save listing in database
        listingsRef.child(listingId).setValue(listingDetails).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ListCropActivity.this, "Crop listed successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(ListCropActivity.this, "Failed to list crop.", Toast.LENGTH_SHORT).show();
            }
            resetButtonAndProgressBar();
        });
    }

    private String getReadableTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        return sdf.format(new Date());
    }

    private void resetButtonAndProgressBar() {
        btnSubmit.setEnabled(true);
        progressBar.setVisibility(View.GONE);
    }
}
