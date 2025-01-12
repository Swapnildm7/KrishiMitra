package org.smartgrains.krishimitra;

import android.content.Context;
import android.content.SharedPreferences;
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
import java.util.Objects;
import java.util.TimeZone;

public class UpdateCropDetailsFragment extends BottomSheetDialogFragment {

    private TextView cropNameTextView;
    private EditText minPriceEditText, maxPriceEditText, quantityEditText;
    private Spinner unitSpinner;
    private Button updateButton, deleteButton;
    private String userId, listingId, cropName;
    private DatabaseReference listingsRef, historyRef, translatedCropNamesRef;
    private String userLanguageCode;

    // Map to handle language translation
    private static final Map<String, String> LANGUAGE_TRANSLATION_MAP = Map.of(
            "mr", "Marathi",
            "kn", "Kannada",
            "hi", "Hindi",
            "en", "English"
    );

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
        translatedCropNamesRef = FirebaseDatabase.getInstance().getReference("TranslatedCropNames");

        // Get the units array from resources
        String[] unitsArray = getResources().getStringArray(R.array.quantity_units_array);

        // Set up the CustomSpinnerAdapter with the units array
        CustomSpinnerAdapter customAdapter = new CustomSpinnerAdapter(getContext(), unitsArray);
        unitSpinner.setAdapter(customAdapter);

        // Retrieve user's preferred language code from SharedPreferences
        SharedPreferences preferences = getContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        userLanguageCode = preferences.getString("LanguageCode", "en").trim().toLowerCase();
        Log.d("LanguageCode", "User's Language Code: " + userLanguageCode); // Log the preferred language code

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
                        cropName = cropListing.getCropName();
                        Log.d("CropName", "Original Crop Name: " + cropName); // Log the original crop name
                        // Fetch translated crop name
                        fetchTranslatedCropName();
                        minPriceEditText.setText(cropListing.getMinPrice());
                        maxPriceEditText.setText(cropListing.getMaxPrice());
                        quantityEditText.setText(cropListing.getQuantity());

                        // Handle the unit spinner selection
                        String unit = cropListing.getUnit();
                        if (unit != null) {
                            CustomSpinnerAdapter adapter = (CustomSpinnerAdapter) unitSpinner.getAdapter();
                            int spinnerPosition = adapter.getPosition(unit);
                            if (spinnerPosition != -1) {
                                unitSpinner.setSelection(spinnerPosition);
                            }
                        }
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

    private void fetchTranslatedCropName() {
        translatedCropNamesRef.child(cropName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.d("TranslatedCropName", "Snapshot exists for crop: " + cropName);
                    String translatedName = snapshot.child(Objects.requireNonNull(LANGUAGE_TRANSLATION_MAP.getOrDefault(userLanguageCode, "English"))).getValue(String.class);
                    Log.d("TranslatedCropName", "Translated Crop Name: " + translatedName); // Log the translated crop name
                    if (translatedName != null) {
                        cropNameTextView.setText(translatedName);
                    } else {
                        Log.d("TranslatedCropName", "No translation available, displaying original name.");
                        cropNameTextView.setText(cropName);
                    }
                } else {
                    Log.d("TranslatedCropName", "No translated data found in Firebase for crop: " + cropName);
                    cropNameTextView.setText(cropName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TranslatedCropName", "Failed to retrieve translated crop name for crop: " + cropName, error.toException());
                cropNameTextView.setText(cropName);
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

    private String getReadableTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        return sdf.format(new Date());
    }

    private void saveCropToHistory() {
        String timestamp = getReadableTimestamp();
        Map<String, Object> historyData = new HashMap<>();
        historyData.put("cropName", cropName);
        historyData.put("minPrice", minPriceEditText.getText().toString().trim());
        historyData.put("maxPrice", maxPriceEditText.getText().toString().trim());
        historyData.put("quantity", quantityEditText.getText().toString().trim());
        historyData.put("unit", unitSpinner.getSelectedItem().toString());
        historyData.put("timestamp", timestamp);

        historyRef.setValue(historyData);
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this crop listing?")
                .setPositiveButton("Yes", (dialog, which) -> deleteCropListing())
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteCropListing() {
        listingsRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Crop listing deleted successfully.", Toast.LENGTH_SHORT).show();
                dismiss();
            } else {
                Toast.makeText(getContext(), "Failed to delete crop listing.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
