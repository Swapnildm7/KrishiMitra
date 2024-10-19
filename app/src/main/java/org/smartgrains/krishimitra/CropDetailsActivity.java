package org.smartgrains.krishimitra;

import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CropDetailsActivity extends AppCompatActivity {
    private TextView cropNameTextView, minPriceTextView, maxPriceTextView, quantityTextView, unitTextView;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_details);

        // Initialize TextViews for crop details
        cropNameTextView = findViewById(R.id.cropNameTextView);
        minPriceTextView = findViewById(R.id.minPriceTextView);
        maxPriceTextView = findViewById(R.id.maxPriceTextView);
        quantityTextView = findViewById(R.id.quantityTextView);
        unitTextView = findViewById(R.id.unitTextView);

        // Retrieve listing ID from Intent
        String listingId = getIntent().getStringExtra("LISTING_ID");

        // Fetch crop details from Firebase
        fetchCropDetails(listingId);
    }

    private void fetchCropDetails(String listingId) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
                .child("<TRADER_ID>").child("Listings").child(listingId); // Replace <TRADER_ID> as needed

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    CropListingModel crop = snapshot.getValue(CropListingModel.class);
                    if (crop != null) {
                        // Set crop details to TextViews
                        cropNameTextView.setText(crop.getCropName());
                        minPriceTextView.setText(crop.getMinPrice());
                        maxPriceTextView.setText(crop.getMaxPrice());
                        quantityTextView.setText("Quantity: " + crop.getQuantity());
                        unitTextView.setText("Unit: " + crop.getUnit());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error (optional: show a Toast or Log error)
            }
        });
    }
}
