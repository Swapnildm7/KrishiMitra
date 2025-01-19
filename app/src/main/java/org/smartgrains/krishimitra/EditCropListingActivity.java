package org.smartgrains.krishimitra;

import android.graphics.Color;
import android.os.Build;
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

public class EditCropListingActivity extends AppCompatActivity {

    private static final String TAG = "EditCropListingActivity";
    private RecyclerView listingsRecyclerView;
    private CropListingAdapter cropListingAdapter;
    private List<CropListing> cropListingList;
    private String traderId;
    private DatabaseReference listingsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.setLocale(this);

        setContentView(R.layout.activity_edit_crop_listing);

        // Make status bar transparent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            EdgeToEdgeUtil.configureEdgeToEdge(getWindow());
        }

        initUI();
        traderId = getIntent().getStringExtra("USER_ID");
        if (traderId == null) {
            Toast.makeText(this, "User ID is missing.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "User ID is null.");
            finish();
            return;
        }

        listingsRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(traderId)
                .child("Listings");
        fetchCropListings();
    }

    private void initUI() {
        listingsRecyclerView = findViewById(R.id.listingsRecyclerView);
        listingsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cropListingList = new ArrayList<>();
        cropListingAdapter = new CropListingAdapter(this, cropListingList);
        listingsRecyclerView.setAdapter(cropListingAdapter);
    }

    private void fetchCropListings() {
        if (listingsRef == null) {
            Log.e(TAG, "Database reference is null.");
            Toast.makeText(this, "Failed to load data. Please try again later.", Toast.LENGTH_SHORT).show();
            return;
        }

        listingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cropListingList.clear();
                try {
                    for (DataSnapshot listingSnapshot : snapshot.getChildren()) {
                        CropListing cropListing = listingSnapshot.getValue(CropListing.class);
                        if (cropListing != null) {
                            cropListing.setListingId(listingSnapshot.getKey());
                            cropListingList.add(cropListing);
                        } else {
                            Log.w(TAG, "Null CropListing encountered.");
                        }
                    }
                    cropListingAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    Log.e(TAG, "Error processing data snapshot", e);
                    Toast.makeText(EditCropListingActivity.this, "Error processing listings.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage(), error.toException());
                Toast.makeText(EditCropListingActivity.this, "Error fetching listings. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onCropItemClicked(CropListing cropListing) {
        if (cropListing == null || cropListing.getListingId() == null) {
            Log.e(TAG, "Invalid crop listing clicked.");
            Toast.makeText(this, "Invalid crop selection.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            UpdateCropDetailsFragment fragment = UpdateCropDetailsFragment.newInstance(traderId, cropListing.getListingId());
            fragment.show(getSupportFragmentManager(), "UpdateCropDetailsFragment");
        } catch (Exception e) {
            Log.e(TAG, "Error displaying UpdateCropDetailsFragment", e);
            Toast.makeText(this, "Failed to open crop details.", Toast.LENGTH_SHORT).show();
        }
    }
}
