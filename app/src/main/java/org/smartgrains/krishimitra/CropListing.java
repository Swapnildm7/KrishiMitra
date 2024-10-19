package org.smartgrains.krishimitra;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

public class CropListing extends AppCompatActivity {

    private Spinner spinnerCropName;
    private EditText etMinPrice, etMaxPrice, etQuantity;
    private Spinner unitSpinner;
    private Button btnSubmit;

    private DatabaseReference databaseReference;
    private String userId;
    private String state, district, taluka;
    private List<String> userListedCrops; // List to hold crops listed by the user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_crop);

        // Make status bar transparent
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Retrieve user details from intent extras
        userId = getIntent().getStringExtra("USER_ID");
        state = getIntent().getStringExtra("State");
        district = getIntent().getStringExtra("District");
        taluka = getIntent().getStringExtra("Taluka");

        // Initialize UI components
        spinnerCropName = findViewById(R.id.spinner_crop_name);
        etMinPrice = findViewById(R.id.et_min_price);
        etMaxPrice = findViewById(R.id.et_max_price);
        etQuantity = findViewById(R.id.et_quantity);
        unitSpinner = findViewById(R.id.unit_spinner);
        btnSubmit = findViewById(R.id.btn_submit);

        // Populate spinners
        populateCropSpinner();
        populateUnitSpinner();

        // Fetch user's listed crops at the beginning
        fetchUserListedCrops();

        // Set up the submit button listener
        btnSubmit.setOnClickListener(v -> checkForDuplicateListing());
    }

    private void populateCropSpinner() {
        String[] crops = getResources().getStringArray(R.array.crop_names_array);
        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(this, crops);
        spinnerCropName.setAdapter(adapter);
    }


    private void populateUnitSpinner() {
        String[] units = getResources().getStringArray(R.array.quantity_units_array);

        // Instantiate the custom spinner adapter
        CustomSpinnerAdapter unitAdapter = new CustomSpinnerAdapter(this, units);

        // Set the adapter to the unit spinner
        unitSpinner.setAdapter(unitAdapter);
    }

    private void fetchUserListedCrops() {
        userListedCrops = new ArrayList<>(); // Initialize the list

        // Query the user's listings
        databaseReference.child(userId).child("Listings").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Loop through the listings and add crop names to the list
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    CropListingModel listing = snapshot.getValue(CropListingModel.class);
                    if (listing != null) {
                        userListedCrops.add(listing.getCropName());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CropListing.this, "Failed to fetch listings: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkForDuplicateListing() {
        String cropName = spinnerCropName.getSelectedItem().toString();

        // Check if the crop is already listed by the user
        if (userListedCrops.contains(cropName)) {
            Toast.makeText(CropListing.this, "You have already listed this crop.", Toast.LENGTH_SHORT).show();
        } else {
            // If no existing listing, proceed to list the crop
            listCrop();
        }
    }

    private void listCrop() {
        String cropName = spinnerCropName.getSelectedItem().toString();
        String minPrice = etMinPrice.getText().toString().trim();
        String maxPrice = etMaxPrice.getText().toString().trim();
        String quantity = etQuantity.getText().toString().trim();
        String unit = unitSpinner.getSelectedItem().toString();

        // Validate input fields
        if (cropName.isEmpty() || minPrice.isEmpty() || maxPrice.isEmpty() || quantity.isEmpty()) {
            Toast.makeText(CropListing.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the values entered are numeric
        if (!isNumeric(minPrice) || !isNumeric(maxPrice) || !isNumeric(quantity)) {
            Toast.makeText(CropListing.this, "Please enter valid numeric values", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if minPrice is less than or equal to maxPrice
        if (Double.parseDouble(minPrice) > Double.parseDouble(maxPrice)) {
            Toast.makeText(CropListing.this, "Minimum price cannot be greater than maximum price", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate a unique ID for the listing
        String listingId = databaseReference.child(userId).child("Listings").push().getKey();
        if (listingId != null) {
            // Create a new CropListingModel object with the entered data
            CropListingModel listing = new CropListingModel(
                    listingId, cropName, minPrice, maxPrice, quantity, unit, state, district, taluka, userId
            );

            // Save the listing to the Firebase database
            databaseReference.child(userId).child("Listings").child(listingId).setValue(listing)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(CropListing.this, "Crop listed successfully", Toast.LENGTH_SHORT).show();
                            clearFields();
                            userListedCrops.add(cropName); // Update the list of user-listed crops
                        } else {
                            Toast.makeText(CropListing.this, "Listing failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void clearFields() {
        etMinPrice.setText("");
        etMaxPrice.setText("");
        etQuantity.setText("");
        spinnerCropName.setSelection(0); // Reset to the first item
        unitSpinner.setSelection(0); // Reset to the first item
    }
}
