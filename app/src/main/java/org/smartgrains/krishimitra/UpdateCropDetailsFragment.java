package org.smartgrains.krishimitra;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class UpdateCropDetailsFragment extends BottomSheetDialogFragment {

    private TextView cropNameTextView;
    private EditText minPriceEditText, maxPriceEditText, quantityEditText;
    private Spinner unitSpinner;
    private Button updateButton, deleteButton;

    private String userId, listingId;
    private DatabaseReference listingsRef, historyRef;

    public static UpdateCropDetailsFragment newInstance(String userId, String listingId) {
        UpdateCropDetailsFragment fragment = new UpdateCropDetailsFragment();
        Bundle args = new Bundle();
        args.putString("USER_ID", userId);
        args.putString("LISTING_ID", listingId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update_crop_details, container, false);

        cropNameTextView = view.findViewById(R.id.cropNameTextView);
        minPriceEditText = view.findViewById(R.id.minPriceEditText);
        maxPriceEditText = view.findViewById(R.id.maxPriceEditText);
        quantityEditText = view.findViewById(R.id.quantityEditText);
        unitSpinner = view.findViewById(R.id.unitSpinner);
        updateButton = view.findViewById(R.id.updateButton);
        deleteButton = view.findViewById(R.id.deleteButton);

        if (getArguments() != null) {
            userId = getArguments().getString("USER_ID");
            listingId = getArguments().getString("LISTING_ID");
        }

        listingsRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Listings").child(listingId);
        historyRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("ListingHistory").child(listingId);

        String[] unitsArray = getResources().getStringArray(R.array.quantity_units_array);
        CustomSpinnerAdapter customAdapter = new CustomSpinnerAdapter(getContext(), unitsArray);
        unitSpinner.setAdapter(customAdapter);

        fetchCropDetails();

        updateButton.setOnClickListener(v -> updateCropDetails());
        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());

        return view;
    }

    private void fetchCropDetails() {
        listingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    CropListing cropListing = snapshot.getValue(CropListing.class);
                    if (cropListing != null) {
                        cropNameTextView.setText(cropListing.getCropName());
                        minPriceEditText.setText(cropListing.getMinPrice());
                        maxPriceEditText.setText(cropListing.getMaxPrice());
                        quantityEditText.setText(cropListing.getQuantity());

                        String unit = cropListing.getUnit();
                        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) unitSpinner.getAdapter();
                        int spinnerPosition = adapter.getPosition(unit);
                        unitSpinner.setSelection(spinnerPosition);
                    }
                } else {
                    Toast.makeText(getContext(), "Crop details not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to fetch crop details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCropDetails() {
        // Retrieve input values
        String minPriceStr = minPriceEditText.getText().toString().trim();
        String maxPriceStr = maxPriceEditText.getText().toString().trim();
        String quantity = quantityEditText.getText().toString().trim();
        String unit = unitSpinner.getSelectedItem().toString();

        // Check if all required fields are filled
        if (minPriceStr.isEmpty()) {
            Toast.makeText(getContext(), "Please enter the minimum price.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (maxPriceStr.isEmpty()) {
            Toast.makeText(getContext(), "Please enter the maximum price.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (quantity.isEmpty()) {
            Toast.makeText(getContext(), "Please enter the quantity.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate price values
        try {
            double minPrice = Double.parseDouble(minPriceStr);
            double maxPrice = Double.parseDouble(maxPriceStr);

            if (maxPrice < minPrice) {
                Toast.makeText(getContext(), "Max price cannot be less than min price.", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid price format.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save current crop details to history before updating
        saveCropToHistory();

        // Prepare the data to update
        Map<String, Object> updates = new HashMap<>();
        updates.put("minPrice", minPriceStr);
        updates.put("maxPrice", maxPriceStr);
        updates.put("quantity", quantity);
        updates.put("unit", unit);
        updates.put("timestamp", getReadableTimestamp());

        // Update the crop listing in the database
        listingsRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Crop details updated successfully.", Toast.LENGTH_SHORT).show();
                dismiss();
            } else {
                Toast.makeText(getContext(), "Failed to update crop details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Confirmation")
                .setMessage("Are you sure you want to delete this crop listing?")
                .setPositiveButton("Yes", (dialog, which) -> deleteCropListing())
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteCropListing() {
        saveCropToHistory();

        listingsRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Crop listing deleted successfully.", Toast.LENGTH_SHORT).show();
                dismiss();
            } else {
                Toast.makeText(getContext(), "Failed to delete crop listing.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveCropToHistory() {
        listingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Map<String, Object> cropDetails = (Map<String, Object>) snapshot.getValue();
                    if (cropDetails != null) {
                        String historyId = historyRef.push().getKey();
                        if (historyId != null) {
                            cropDetails.put("endTimestamp", getReadableTimestamp());
                            historyRef.child(historyId).setValue(cropDetails)
                                    .addOnSuccessListener(aVoid -> Log.d("History", "Crop details saved to history"))
                                    .addOnFailureListener(e -> Log.e("History", "Failed to save crop details to history", e));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("History", "Failed to retrieve crop details for history", error.toException());
            }
        });
    }

    private String getReadableTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        return sdf.format(new Date());
    }
}
