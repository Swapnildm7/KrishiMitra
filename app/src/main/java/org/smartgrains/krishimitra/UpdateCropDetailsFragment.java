package org.smartgrains.krishimitra;

import android.os.Bundle;
import android.util.Log; // Import for logging
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UpdateCropDetailsFragment extends BottomSheetDialogFragment {

    private static final String ARG_CROP = "crop";
    private static final String ARG_USER_ID = "userId";
    private static final String ARG_LISTING_ID = "listingId"; // New constant for listingId

    private CropListingModel crop;
    private String userId; // Store userId passed from the activity
    private String listingId; // Store the listingId to be updated

    private EditText minPriceEditText;
    private EditText maxPriceEditText;
    private EditText quantityEditText;
    private Spinner unitSpinner;
    private Button updateButton;
    private Button deleteButton;

    private static final String TAG = "UpdateCropDetailsFragment"; // Tag for logging

    // Static method to create a new instance of the fragment
    public static UpdateCropDetailsFragment newInstance(CropListingModel crop, String listingId, String userId) {
        UpdateCropDetailsFragment fragment = new UpdateCropDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_CROP, crop); // Use Parcelable to pass CropListingModel
        args.putString(ARG_USER_ID, userId);
        args.putString(ARG_LISTING_ID, listingId); // Pass the listingId to the fragment
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            crop = getArguments().getParcelable(ARG_CROP); // Retrieve crop from arguments
            userId = getArguments().getString(ARG_USER_ID); // Retrieve userId from arguments
            listingId = getArguments().getString(ARG_LISTING_ID); // Retrieve listingId from arguments
            Log.d(TAG, "onCreate - userId: " + userId + ", listingId: " + listingId); // Log userId and listingId
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update_crop_details, container, false);

        minPriceEditText = view.findViewById(R.id.minPriceEditText);
        maxPriceEditText = view.findViewById(R.id.maxPriceEditText);
        quantityEditText = view.findViewById(R.id.quantityEditText);
        unitSpinner = view.findViewById(R.id.unitSpinner);
        updateButton = view.findViewById(R.id.updateButton);
        deleteButton = view.findViewById(R.id.deleteButton);

        // Set the existing crop details
        minPriceEditText.setText(crop.getMinPrice());
        maxPriceEditText.setText(crop.getMaxPrice());
        quantityEditText.setText(crop.getQuantity());

        // Get the units array from resources
        String[] unitsArray = getResources().getStringArray(R.array.quantity_units_array);

        // Set up the custom spinner adapter using the String array
        CustomSpinnerAdapter customAdapter = new CustomSpinnerAdapter(getContext(), unitsArray);
        unitSpinner.setAdapter(customAdapter);

        // Set the current unit in the spinner
        int unitPosition = customAdapter.getPosition(crop.getUnit());
        unitSpinner.setSelection(unitPosition);

        // Set up the update button
        updateButton.setOnClickListener(v -> {
            updateCropDetails(listingId); // Directly use listingId passed from arguments
        });

        // Set up the delete button
        deleteButton.setOnClickListener(v -> {
            deleteCropDetails(listingId); // Directly use listingId passed from arguments
        });

        return view;
    }

    private void updateCropDetails(String listingId) {
        // Get updated values
        String newMinPrice = minPriceEditText.getText().toString();
        String newMaxPrice = maxPriceEditText.getText().toString();
        String newQuantity = quantityEditText.getText().toString();
        String newUnit = unitSpinner.getSelectedItem().toString();

        // Validate input
        if (newMinPrice.isEmpty() || newMaxPrice.isEmpty() || newQuantity.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ensure max price is greater than min price
        if (Double.parseDouble(newMaxPrice) <= Double.parseDouble(newMinPrice)) {
            Toast.makeText(getContext(), "Max price must be greater than Min price", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get a reference to the database for the current listing
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(userId)
                .child("Listings")
                .child(listingId);

        // Get a reference to the history node where old data will be stored
        DatabaseReference historyReference = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(userId)
                .child("ListingsHistory")
                .child(listingId)
                .push(); // Generate a unique key for each history entry

        // Move the current crop details to the history node
        historyReference.setValue(crop).addOnCompleteListener(historyTask -> {
            if (historyTask.isSuccessful()) {
                // Successfully moved the current crop to history

                // Now, update the crop object with new values
                crop.setMinPrice(newMinPrice);
                crop.setMaxPrice(newMaxPrice);
                crop.setQuantity(newQuantity);
                crop.setUnit(newUnit);

                // Write the updated crop object to the database
                databaseReference.setValue(crop).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        Toast.makeText(getContext(), "Crop updated successfully!", Toast.LENGTH_SHORT).show();
                        dismiss(); // Close the bottom sheet
                    } else {
                        Toast.makeText(getContext(), "Update failed: " + updateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), "Failed to move old listing to history: " + historyTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteCropDetails(String listingId) {
        // Get a reference to the database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(userId)
                .child("Listings")
                .child(listingId); // Use the fetched listing ID

        // Get a reference to the history node where old data will be stored
        DatabaseReference historyReference = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(userId)
                .child("ListingsHistory")
                .child(listingId)
                .push(); // Generate a unique key for each history entry

        // Move the current crop details to the history node
        historyReference.setValue(crop).addOnCompleteListener(historyTask -> {
            if (historyTask.isSuccessful()) {
                // Successfully moved the current crop to history

                // Remove the crop from the database
                databaseReference.removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Crop deleted successfully!", Toast.LENGTH_SHORT).show();
                        dismiss(); // Close the bottom sheet
                    } else {
                        Toast.makeText(getContext(), "Deletion failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), "Failed to move old listing to history: " + historyTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
