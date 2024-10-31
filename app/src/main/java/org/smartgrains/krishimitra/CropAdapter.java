package org.smartgrains.krishimitra;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class CropAdapter extends RecyclerView.Adapter<CropAdapter.CropViewHolder> {
    private List<Crop> cropList; // Change to List<Crop>

    public CropAdapter(List<Crop> cropList) {
        this.cropList = cropList;
    }

    @NonNull
    @Override
    public CropViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_trader_crop_listing, parent, false);
        return new CropViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull CropViewHolder holder, int position) {
        Crop crop = cropList.get(position);

        if (crop != null) {
            holder.cropNameTextView.setText(crop.getCropName() != null ? crop.getCropName() : "N/A");
            holder.minPriceTextView.setText("Min Price: ₹" + (crop.getMinPrice() != null ? crop.getMinPrice() : "N/A"));
            holder.maxPriceTextView.setText("Max Price: ₹" + (crop.getMaxPrice() != null ? crop.getMaxPrice() : "N/A"));
            holder.quantityTextView.setText("Quantity: " + (crop.getQuantity() != null ? crop.getQuantity() : "N/A"));
            holder.unitTextView.setText("Unit: " + (crop.getUnit() != null ? crop.getUnit() : "N/A"));
        }

        // Load image from imageUrl using Picasso
        Picasso.get()
                .load(crop.getImageUrl())
                .placeholder(R.drawable.placeholder_image) // Placeholder while loading
                .error(R.drawable.error_image) // Image if there's an error
                .into(holder.cropImageView);
    }

    @Override
    public int getItemCount() {
        return cropList.size();
    }

    public static class CropViewHolder extends RecyclerView.ViewHolder {
        TextView cropNameTextView;
        TextView minPriceTextView;
        TextView maxPriceTextView;
        TextView quantityTextView;
        TextView unitTextView;
        ImageView cropImageView; // Declare the ImageView

        public CropViewHolder(@NonNull View itemView) {
            super(itemView);
            cropNameTextView = itemView.findViewById(R.id.cropNameTextView);
            minPriceTextView = itemView.findViewById(R.id.minPriceTextView);
            maxPriceTextView = itemView.findViewById(R.id.maxPriceTextView);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
            unitTextView = itemView.findViewById(R.id.unitTextView);
            cropImageView = itemView.findViewById(R.id.cropImageView); // Initialize the ImageView
        }
    }
}
