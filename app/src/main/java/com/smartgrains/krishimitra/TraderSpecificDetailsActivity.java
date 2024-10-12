package com.smartgrains.krishimitra;

import android.content.Intent;
import android.net.Uri; // Import this for dialing
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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

public class TraderSpecificDetailsActivity extends AppCompatActivity {
    private TextView traderNameTextView, shopNameTextView, shopAddressTextView, phoneNumberTextView;
    private RecyclerView cropsRecyclerView;
    private CropAdapter cropAdapter;
    private List<CropListingModel> cropList;
    private DatabaseReference databaseReference;
    private ImageView callIcon; // Add this line

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trader_specific_details);

        // Make status bar transparent
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        // Initialize TextViews for trader details
        traderNameTextView = findViewById(R.id.traderNameTextView);
        shopNameTextView = findViewById(R.id.shopNameTextView);
        shopAddressTextView = findViewById(R.id.shopAddressTextView);
        phoneNumberTextView = findViewById(R.id.phoneNumberTextView);
        callIcon = findViewById(R.id.callIcon); // Initialize call icon

        // Retrieve trader details from Intent
        Intent intent = getIntent();
        String traderId = intent.getStringExtra("TRADER_ID");
        String traderName = intent.getStringExtra("TRADER_NAME");
        String shopName = intent.getStringExtra("SHOP_NAME");
        String shopAddress = intent.getStringExtra("SHOP_ADDRESS");
        String phoneNumber = intent.getStringExtra("PHONE_NUMBER");

        // Set trader details in the views
        traderNameTextView.setText(traderName);
        shopNameTextView.setText(shopName);
        shopAddressTextView.setText(shopAddress);
        phoneNumberTextView.setText(phoneNumber);

        // Set the onClickListener for the call icon
        callIcon.setOnClickListener(v -> makeCall(phoneNumber));

        // Set up RecyclerView for crop listings
        cropsRecyclerView = findViewById(R.id.cropsRecyclerView);
        cropsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cropList = new ArrayList<>();

        // Initialize CropAdapter with OnItemClickListener
        cropAdapter = new CropAdapter(cropList, (crop, listingId) -> {
            // Handle crop item click event
            Intent cropDetailIntent = new Intent(TraderSpecificDetailsActivity.this, CropDetailsActivity.class);
            cropDetailIntent.putExtra("LISTING_ID", listingId);
            cropDetailIntent.putExtra("TRADER_ID", traderId); // Pass trader ID to CropDetailsActivity
            startActivity(cropDetailIntent);
        }, false); // Pass true for clickable items



        cropsRecyclerView.setAdapter(cropAdapter);

        // Fetch crop listings from Firebase
        fetchCropListings(traderId);
    }

    private void fetchCropListings(String traderId) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
                .child(traderId).child("Listings");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cropList.clear(); // Clear the list before adding new data
                for (DataSnapshot cropSnapshot : snapshot.getChildren()) {
                    CropListingModel crop = cropSnapshot.getValue(CropListingModel.class);
                    if (crop != null) { // Ensure crop is not null
                        cropList.add(crop);
                    }
                }
                cropAdapter.notifyDataSetChanged(); // Notify the adapter for data changes
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error (optional: show a Toast or Log error)
                // You can add a Toast message or a Log statement here
            }
        });
    }

    private void makeCall(String phoneNumber) {
        // Intent to open the dialer with the phone number
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }
}
