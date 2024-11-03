package org.smartgrains.krishimitra;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CropListingAdapter extends RecyclerView.Adapter<CropListingAdapter.CropViewHolder> {

    private Context context;
    private List<CropListing> cropListingList;

    public CropListingAdapter(Context context, List<CropListing> cropListingList) {
        this.context = context;
        this.cropListingList = cropListingList;
    }

    @NonNull
    @Override
    public CropViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_crop, parent, false);
        return new CropViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull CropViewHolder holder, int position) {
        if (cropListingList == null || position < 0 || position >= cropListingList.size()) {
            Log.e("CropListingAdapter", "Invalid position or empty list");
            return;
        }

        CropListing cropListing = cropListingList.get(position);
        if (cropListing != null) {
            holder.cropNameTextView.setText(cropListing.getCropName());
            holder.minPriceTextView.setText("Min Price: ₹" + cropListing.getMinPrice());
            holder.maxPriceTextView.setText("Max Price: ₹" + cropListing.getMaxPrice());
            holder.quantityTextView.setText("Quantity: " + cropListing.getQuantity());
            holder.unitTextView.setText("Unit: " + cropListing.getUnit());

            // Load image with error handling using Picasso
            Picasso.get()
                    .load(cropListing.getImageUrl())
                    .placeholder(R.drawable.placeholder_image) // Placeholder while loading
                    .error(R.drawable.error_image) // Fallback image on error
                    .into(holder.cropImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            Log.d("CropListingAdapter", "Image loaded successfully for: " + cropListing.getCropName());
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("CropListingAdapter", "Error loading image for " + cropListing.getCropName() + ": " + e.getMessage());
                            Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    });

            // Set onClick listener with safe typecasting and error handling
            holder.itemView.setOnClickListener(v -> {
                try {
                    if (context instanceof EditCropListingActivity) {
                        ((EditCropListingActivity) context).onCropItemClicked(cropListing);
                    } else {
                        Log.e("CropListingAdapter", "Context is not an instance of EditCropListingActivity");
                    }
                } catch (ClassCastException e) {
                    Log.e("CropListingAdapter", "Error casting context: " + e.getMessage());
                    Toast.makeText(context, "Action unavailable in this context", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e("CropListingAdapter", "Null CropListing object at position " + position);
        }
    }

    @Override
    public int getItemCount() {
        return cropListingList != null ? cropListingList.size() : 0;
    }

    static class CropViewHolder extends RecyclerView.ViewHolder {
        TextView cropNameTextView, minPriceTextView, maxPriceTextView, quantityTextView, unitTextView;
        ImageView cropImageView;

        public CropViewHolder(@NonNull View itemView) {
            super(itemView);
            cropNameTextView = itemView.findViewById(R.id.cropNameTextView);
            minPriceTextView = itemView.findViewById(R.id.minPriceTextView);
            maxPriceTextView = itemView.findViewById(R.id.maxPriceTextView);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
            unitTextView = itemView.findViewById(R.id.unitTextView);
            cropImageView = itemView.findViewById(R.id.cropImageView);
        }
    }
}
