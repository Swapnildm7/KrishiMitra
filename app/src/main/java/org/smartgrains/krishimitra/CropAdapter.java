package org.smartgrains.krishimitra;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CropAdapter extends RecyclerView.Adapter<CropAdapter.CropViewHolder> {
    private List<CropListingModel> cropList;
    private OnItemClickListener itemClickListener;
    private boolean isClickable; // Variable to check if items should be clickable

    // Constructor
    public CropAdapter(List<CropListingModel> cropList, OnItemClickListener itemClickListener, boolean isClickable) {
        this.cropList = cropList;
        this.itemClickListener = itemClickListener;
        this.isClickable = isClickable;
    }

    @NonNull
    @Override
    public CropViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_crop, parent, false);
        return new CropViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CropViewHolder holder, int position) {
        CropListingModel crop = cropList.get(position);

        // Set crop details to the views
        holder.cropNameTextView.setText(crop.getCropName());
        holder.minPriceTextView.setText("Min Price: " + crop.getMinPrice());
        holder.maxPriceTextView.setText("Max Price: " + crop.getMaxPrice());
        holder.quantityTextView.setText("Quantity: " + crop.getQuantity());
        holder.unitTextView.setText("Unit: " + crop.getUnit());

        // Set click listener based on whether items should be clickable
        holder.itemView.setOnClickListener(v -> {
            if (isClickable) {
                // If items are clickable, notify the listener
                itemClickListener.onItemClick(crop, crop.getListingId());
            }
        });

        // If not clickable, ensure that no interaction is possible
        holder.itemView.setClickable(isClickable);
        holder.itemView.setFocusable(isClickable);
    }

    @Override
    public int getItemCount() {
        return cropList.size();
    }

    // ViewHolder class
    static class CropViewHolder extends RecyclerView.ViewHolder {
        TextView cropNameTextView, minPriceTextView, maxPriceTextView, quantityTextView, unitTextView;

        public CropViewHolder(@NonNull View itemView) {
            super(itemView);
            cropNameTextView = itemView.findViewById(R.id.cropNameTextView);
            minPriceTextView = itemView.findViewById(R.id.minPriceTextView);
            maxPriceTextView = itemView.findViewById(R.id.maxPriceTextView);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
            unitTextView = itemView.findViewById(R.id.unitTextView);
        }
    }

    // Interface for item click listener
    public interface OnItemClickListener {
        void onItemClick(CropListingModel crop, String listingId);
    }
}
