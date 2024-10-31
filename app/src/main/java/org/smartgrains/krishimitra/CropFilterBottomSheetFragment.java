package org.smartgrains.krishimitra;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CropFilterBottomSheetFragment extends BottomSheetDialogFragment {
    private RecyclerView cropRecyclerView;
    private Button doneButton;
    private CropNameAdapter cropNameAdapter;
    private List<String> cropNames = new ArrayList<>();
    private Map<String, CropListing> allCropsMap; // Map to hold unique crops

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

        // Attempt to fetch crop names
        fetchCropNamesFromMap();

        // Done button listener
        doneButton.setOnClickListener(v -> {
            try {
                List<String> selectedCrops = cropNameAdapter.getSelectedCrops();
                Log.d("CropFilterBottomSheet", "Selected crops: " + selectedCrops);
                listener.onCropsSelected(selectedCrops);
            } catch (Exception e) {
                Log.e("CropFilterBottomSheet", "Error in selecting crops: " + e.getMessage());
                Toast.makeText(getContext(), "Error selecting crops", Toast.LENGTH_SHORT).show();
            }
            dismiss();
        });

        return view;
    }

    private void fetchCropNamesFromMap() {
        // Check if allCropsMap is null and handle it
        if (allCropsMap == null) {
            Log.e("CropFilterBottomSheet", "allCropsMap is null");
            Toast.makeText(getContext(), "Failed to load crop data", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            cropNames.clear();
            cropNames.addAll(allCropsMap.keySet());
            cropNameAdapter.notifyDataSetChanged();
            Log.d("CropFilterBottomSheet", "Fetched crop names: " + cropNames);
        } catch (Exception e) {
            Log.e("CropFilterBottomSheet", "Error fetching crop names: " + e.getMessage());
            Toast.makeText(getContext(), "Error loading crops", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // Nullify listener to prevent memory leaks
        listener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Handle any cleanup if needed
    }
}
