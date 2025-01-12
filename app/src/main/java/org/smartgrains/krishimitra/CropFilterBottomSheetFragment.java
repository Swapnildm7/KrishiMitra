package org.smartgrains.krishimitra;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CropFilterBottomSheetFragment extends BottomSheetDialogFragment {
    private RecyclerView cropRecyclerView;
    private Button doneButton;
    private CropNameAdapter cropNameAdapter;
    private List<String> cropNames = new ArrayList<>();
    private Map<String, CropListing> allCropsMap;
    private Map<String, String> translatedToOriginalMap = new HashMap<>();

    public interface OnCropsSelectedListener {
        void onCropsSelected(List<String> selectedCrops);
    }

    private OnCropsSelectedListener listener;

    public CropFilterBottomSheetFragment(OnCropsSelectedListener listener, Map<String, CropListing> allCropsMap) {
        this.listener = listener;
        this.allCropsMap = allCropsMap;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_fragment_crop_filter_bottom_sheet, container, false);

        // Initialize Views
        cropRecyclerView = view.findViewById(R.id.cropRecyclerView);
        doneButton = view.findViewById(R.id.doneButton);

        // Initialize Adapter
        cropNameAdapter = new CropNameAdapter(cropNames);
        cropRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cropRecyclerView.setAdapter(cropNameAdapter);

        // Fetch crop names and translate them
        fetchAndTranslateCropNames();

        // Done button listener
        doneButton.setOnClickListener(v -> {
            try {
                List<String> selectedCrops = cropNameAdapter.getSelectedCrops();
                List<String> originalCropKeys = new ArrayList<>();
                for (String translatedCrop : selectedCrops) {
                    originalCropKeys.add(translatedToOriginalMap.get(translatedCrop)); // Map back to original names
                }
                Log.d("CropFilterBottomSheet", "Original crop keys: " + originalCropKeys);
                listener.onCropsSelected(originalCropKeys);
            } catch (Exception e) {
                Log.e("CropFilterBottomSheet", "Error in selecting crops: " + e.getMessage());
                Toast.makeText(getContext(), "Error selecting crops", Toast.LENGTH_SHORT).show();
            }
            dismiss();
        });

        return view;
    }

    private void fetchAndTranslateCropNames() {
        // Check if allCropsMap is null and handle it
        if (allCropsMap == null) {
            Log.e("CropFilterBottomSheet", "allCropsMap is null");
            Toast.makeText(getContext(), "Failed to load crop data", Toast.LENGTH_SHORT).show();
            return;
        }

        // Retrieve preferred language code
        String preferredLanguageCode = getPreferredLanguageCode();

        // Map language codes to Firebase keys for translations
        String firebaseLanguageKey = getFirebaseLanguageKey(preferredLanguageCode);

        // Firebase reference for translations
        DatabaseReference translationRef = FirebaseDatabase.getInstance().getReference("TranslatedCropNames");

        translationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                cropNames.clear();
                translatedToOriginalMap.clear(); // Clear the map before populating
                for (String cropKey : allCropsMap.keySet()) {
                    if (preferredLanguageCode.equals("en")) {
                        // For English, use the crop key directly
                        cropNames.add(cropKey);
                        translatedToOriginalMap.put(cropKey, cropKey);
                    } else {
                        // Check if the crop key exists in the translation node
                        DataSnapshot cropSnapshot = dataSnapshot.child(cropKey);
                        if (cropSnapshot.exists()) {
                            // Get the crop name in the preferred language
                            String translatedName = cropSnapshot.child(firebaseLanguageKey).getValue(String.class);
                            if (translatedName != null) {
                                cropNames.add(translatedName);
                                translatedToOriginalMap.put(translatedName, cropKey);
                            } else {
                                // Fallback to crop key if translation is missing
                                cropNames.add(cropKey);
                                translatedToOriginalMap.put(cropKey, cropKey);
                            }
                        } else {
                            // Fallback to crop key if crop translation node is missing
                            cropNames.add(cropKey);
                            translatedToOriginalMap.put(cropKey, cropKey);
                        }
                    }
                }
                cropNameAdapter.notifyDataSetChanged(); // Notify adapter of data change
                Log.d("CropFilterBottomSheet", "Fetched crop names: " + cropNames);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("CropFilterBottomSheet", "Error fetching crop names: " + databaseError.getMessage());
                Toast.makeText(getContext(), "Error loading crops", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getPreferredLanguageCode() {
        SharedPreferences preferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        return preferences.getString("LanguageCode", "en"); // Default to English if no preference
    }

    private String getFirebaseLanguageKey(String languageCode) {
        switch (languageCode) {
            case "hi":
                return "Hindi";
            case "kn":
                return "Kannada";
            case "mr":
                return "Marathi";
            default:
                return "English"; // Default to English if language code is unsupported
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
