package com.smartgrains.krishimitra;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Set;

public class CropSelectionAdapter extends RecyclerView.Adapter<CropSelectionAdapter.ViewHolder> {

    private List<String> cropList;
    private Set<String> selectedCrops;
    private CropSelectionListener listener;

    public CropSelectionAdapter(List<String> cropList, Set<String> selectedCrops, CropSelectionListener listener) {
        this.cropList = cropList;
        this.selectedCrops = selectedCrops;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_crop_selection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String crop = cropList.get(position);
        holder.cropNameTextView.setText(crop);
        holder.cropCheckBox.setChecked(selectedCrops.contains(crop)); // Update checkbox state

        // Handle checkbox click independently
        holder.cropCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedCrops.add(crop); // Add crop if checked
                listener.onCropSelected(crop); // Notify listener
            } else {
                selectedCrops.remove(crop); // Remove crop if unchecked
                listener.onCropSelected(crop); // Notify listener
            }
        });

        // Prevent recycling issues
        holder.itemView.setOnClickListener(v -> {
            // Directly toggle checkbox on item click
            holder.cropCheckBox.setChecked(!holder.cropCheckBox.isChecked()); // Toggle checkbox
        });
    }

    @Override
    public int getItemCount() {
        return cropList.size();
    }

    public interface CropSelectionListener {
        void onCropSelected(String crop);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView cropNameTextView;
        CheckBox cropCheckBox;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cropNameTextView = itemView.findViewById(R.id.crop_name);
            cropCheckBox = itemView.findViewById(R.id.crop_checkbox);
        }
    }
}
