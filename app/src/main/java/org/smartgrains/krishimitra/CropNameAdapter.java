package org.smartgrains.krishimitra;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CropNameAdapter extends RecyclerView.Adapter<CropNameAdapter.ViewHolder> {
    private List<String> cropNames;
    private List<String> selectedCrops;

    public CropNameAdapter(List<String> cropNames) {
        this.cropNames = cropNames != null ? cropNames : new ArrayList<>(); // Prevent null list
        this.selectedCrops = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_crop_name, parent, false);
            return new ViewHolder(view);
        } catch (Exception e) {
            Log.e("CropNameAdapter", "Error inflating item view", e);
            Toast.makeText(parent.getContext(), "Failed to load crop items", Toast.LENGTH_SHORT).show();
            return null; // Return null if view cannot be created
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            if (position < 0 || position >= cropNames.size()) {
                Log.e("CropNameAdapter", "Invalid position: " + position);
                return;
            }
            String cropName = cropNames.get(position);
            holder.bind(cropName, selectedCrops.contains(cropName));

            // Item view click listener
            holder.itemView.setOnClickListener(v -> handleSelection(holder, cropName, position));

            // Checkbox click listener
            holder.cropCheckBox.setOnClickListener(v -> handleSelection(holder, cropName, position));
        } catch (Exception e) {
            Log.e("CropNameAdapter", "Error binding view at position " + position, e);
        }
    }

    private void handleSelection(ViewHolder holder, String cropName, int position) {
        try {
            toggleSelection(cropName);
            notifyItemChanged(position);
        } catch (Exception e) {
            Log.e("CropNameAdapter", "Error updating selection for " + cropName, e);
            Toast.makeText(holder.itemView.getContext(), "Error updating selection", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleSelection(String cropName) {
        if (cropName == null) {
            Log.e("CropNameAdapter", "Attempted to toggle selection on a null crop name");
            return;
        }
        if (selectedCrops.contains(cropName)) {
            selectedCrops.remove(cropName);
        } else {
            selectedCrops.add(cropName);
        }
        Log.d("CropNameAdapter", "Selected crops: " + selectedCrops); // Log selected crops after change
    }

    @Override
    public int getItemCount() {
        return cropNames != null ? cropNames.size() : 0;
    }

    public List<String> getSelectedCrops() {
        return new ArrayList<>(selectedCrops);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView cropNameTextView;
        private CheckBox cropCheckBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            try {
                cropNameTextView = itemView.findViewById(R.id.cropNameTextView);
                cropCheckBox = itemView.findViewById(R.id.cropCheckBox);
            } catch (Exception e) {
                Log.e("CropNameAdapter", "Error initializing view holder", e);
            }
        }

        public void bind(String cropName, boolean isSelected) {
            try {
                cropNameTextView.setText(cropName);
                cropCheckBox.setChecked(isSelected);
            } catch (Exception e) {
                Log.e("CropNameAdapter", "Error binding crop name: " + cropName, e);
            }
        }
    }
}
