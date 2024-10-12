package com.smartgrains.krishimitra;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CropAdapter extends RecyclerView.Adapter<CropAdapter.CropViewHolder> {
    private List<CropListingModel> cropList;
    private OnItemClickListener listener;

    // Interface for click events
    public interface OnItemClickListener {
        void onItemClick(CropListingModel crop, String listingId);
    }

    public CropAdapter(List<CropListingModel> cropList, OnItemClickListener listener) {
        this.cropList = cropList;
        this.listener = listener;
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
        holder.cropNameTextView.setText(crop.getCropName());
        holder.minPriceTextView.setText("Min Price: " + crop.getMinPrice());
        holder.maxPriceTextView.setText("Max Price: " + crop.getMaxPrice());
        holder.quantityTextView.setText("Quantity: " + crop.getQuantity());
        holder.unitTextView.setText("Unit: " + crop.getUnit());

        // Set the click listener for the card
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                // Trigger the click event
                listener.onItemClick(crop, crop.getListingId()); // Ensure getListingId() returns the listing ID
            }
        });
    }

    @Override
    public int getItemCount() {
        return cropList.size();
    }

    public static class CropViewHolder extends RecyclerView.ViewHolder {
        public TextView cropNameTextView;
        public TextView minPriceTextView;
        public TextView maxPriceTextView;
        public TextView quantityTextView;
        public TextView unitTextView;

        public CropViewHolder(View itemView) {
            super(itemView);
            cropNameTextView = itemView.findViewById(R.id.cropNameTextView);
            minPriceTextView = itemView.findViewById(R.id.minPriceTextView);
            maxPriceTextView = itemView.findViewById(R.id.maxPriceTextView);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
            unitTextView = itemView.findViewById(R.id.unitTextView);
        }
    }
}
