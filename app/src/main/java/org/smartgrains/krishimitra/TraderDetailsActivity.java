package org.smartgrains.krishimitra;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
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

public class TraderDetailsActivity extends AppCompatActivity {
    private TextView traderNameTextView, shopNameTextView, shopAddressTextView, phoneNumberTextView, ratingTextView;
    private ImageView callIcon;
    private RecyclerView cropsRecyclerView;
    private RatingBar ratingBar;
    private CropAdapter cropAdapter;
    private List<Crop> cropList;
    private String userId;
    private DatabaseReference databaseReference;
    private static final String TAG = "TraderDetailsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trader_details);

        // Make status bar transparent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            EdgeToEdgeUtil.configureEdgeToEdge(getWindow());
        }

        // Initialize views
        traderNameTextView = findViewById(R.id.traderNameTextView);
        shopNameTextView = findViewById(R.id.shopNameTextView);
        shopAddressTextView = findViewById(R.id.shopAddressTextView);
        phoneNumberTextView = findViewById(R.id.phoneNumberTextView);
        ratingTextView = findViewById(R.id.ratingTextView);
        callIcon = findViewById(R.id.callIcon);
        cropsRecyclerView = findViewById(R.id.cropsRecyclerView);
        ratingBar = findViewById(R.id.ratingBar);

        // Get data from Intent
        userId = getIntent().getStringExtra("userId");
        String traderName = getIntent().getStringExtra("traderName");
        String shopName = getIntent().getStringExtra("shopName");
        String shopAddress = getIntent().getStringExtra("shopAddress");
        String phoneNumber = getIntent().getStringExtra("phoneNumber");

        // Log the userId to verify if it's fetched properly
        Log.d(TAG, "Fetched userId: " + userId);

        // Set trader details
        traderNameTextView.setText(traderName != null ? traderName : "N/A");
        shopNameTextView.setText(shopName != null ? shopName : "N/A");
        shopAddressTextView.setText(shopAddress != null ? shopAddress : "N/A");
        phoneNumberTextView.setText(phoneNumber != null ? phoneNumber : "N/A");

        // Add real-time listener for trader's average rating and review count
        DatabaseReference traderRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        traderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Float averageRating = snapshot.child("averageRating").getValue(Float.class);
                Long reviewCount = snapshot.child("reviewCount").getValue(Long.class);

                ratingBar.setRating(averageRating != null ? averageRating : 0);
                ratingTextView.setText(reviewCount != null
                        ? reviewCount + " reviews, " + "( " + (averageRating != null ? averageRating : 0) + " stars )"
                        : "No reviews yet");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TraderDetailsActivity.this, "Failed to fetch trader data", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up click listener for call icon
        callIcon.setOnClickListener(v -> {
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                copyToClipboard(phoneNumber);
                openDialer(phoneNumber);
            } else {
                Toast.makeText(this, "Phone number is not available", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up click listener for phone number text view
        phoneNumberTextView.setOnClickListener(v -> {
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                copyToClipboard(phoneNumber);
                openDialer(phoneNumber);
            } else {
                Toast.makeText(this, "Phone number is not available", Toast.LENGTH_SHORT).show();
            }
        });

        // Initialize RecyclerView
        cropList = new ArrayList<>();

        // Pass the context along with the list to the adapter
        cropAdapter = new CropAdapter(this, cropList);
        cropsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cropsRecyclerView.setAdapter(cropAdapter);

        // Fetch crops from Firebase based on userId
        if (userId != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference("Users");
            databaseReference.child(userId).child("Listings").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    cropList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Crop crop = snapshot.getValue(Crop.class);
                        if (crop != null) {
                            cropList.add(crop);
                        } else {
                            Log.e(TAG, "Crop data is null for snapshot: " + snapshot);
                        }
                    }
                    cropAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error
                    Log.e(TAG, "Database error: " + databaseError.getMessage());
                    Toast.makeText(TraderDetailsActivity.this, "Failed to load crops", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e(TAG, "User ID is null. Cannot fetch crops.");
            Toast.makeText(this, "Unable to retrieve trader data.", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to copy phone number to clipboard
    private void copyToClipboard(String phoneNumber) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Phone Number", phoneNumber);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Phone number copied to clipboard", Toast.LENGTH_SHORT).show();
        } else {
            Log.e(TAG, "Clipboard manager is null");
            Toast.makeText(this, "Failed to access clipboard", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to open the dialer with the phone number
    private void openDialer(String phoneNumber) {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening dialer: " + e.getMessage());
            Toast.makeText(this, "Failed to open dialer", Toast.LENGTH_SHORT).show();
        }
    }
}
