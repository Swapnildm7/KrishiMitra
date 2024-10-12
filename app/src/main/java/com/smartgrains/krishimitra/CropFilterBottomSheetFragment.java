package com.smartgrains.krishimitra;

import android.os.Bundle;
import android.os.Handler;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CropFilterBottomSheetFragment extends BottomSheetDialogFragment {

    private Set<String> selectedCrops;  // To store selected crops
    private CropSelectionAdapter cropSelectionAdapter;

    // Define a listener interface to communicate back to the activity
    public interface OnCropsSelectedListener {
        void onCropsSelected(List<String> selectedCrops);
    }

    private OnCropsSelectedListener cropsSelectedListener; // Listener for selected crops

    public CropFilterBottomSheetFragment() {
        // Required empty public constructor
    }

    // Method to set the listener for selected crops
    public void setOnCropsSelectedListener(OnCropsSelectedListener listener) {
        this.cropsSelectedListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crop_filter_bottom_sheet, container, false);

        // Initialize selectedCrops set
        selectedCrops = new HashSet<>();

        // Set up RecyclerView
        RecyclerView cropRecyclerView = view.findViewById(R.id.cropRecyclerView);
        cropRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load crop names from resources
        String[] cropNames = getResources().getStringArray(R.array.crop_names_array);
        List<String> cropList = new ArrayList<>();
        for (String crop : cropNames) {
            cropList.add(crop);
        }

        // Set up adapter for RecyclerView
        cropSelectionAdapter = new CropSelectionAdapter(cropList, selectedCrops, crop -> {
            // Update crops and handle logging here
            new Handler().post(cropSelectionAdapter::notifyDataSetChanged); // Notify adapter in next frame
        });
        cropRecyclerView.setAdapter(cropSelectionAdapter);

        // Set up done button to pass selected crops back
        Button doneButton = view.findViewById(R.id.doneButton);
        doneButton.setOnClickListener(v -> {
            // Check if listener is set and pass selected crops back
            if (cropsSelectedListener != null) {
                cropsSelectedListener.onCropsSelected(new ArrayList<>(selectedCrops));
            } else {
                Toast.makeText(getContext(), "Error: Listener not set", Toast.LENGTH_SHORT).show();
            }
            dismiss();  // Close the bottom sheet
        });

        return view;
    }
}
