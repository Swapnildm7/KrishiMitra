package org.smartgrains.krishimitra;

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

public class CropImageAdapter extends RecyclerView.Adapter<CropImageAdapter.CropViewHolder> {
    private Context context;
    private List<CropListing> cropListingList;
    private CropClickListener cropClickListener;

    // Interface for handling clicks
    public interface CropClickListener {
        void onCropClick(CropListing cropListing);
    }

    // Constructor now includes a CropClickListener parameter
    public CropImageAdapter(Context context, List<CropListing> cropListingList, CropClickListener cropClickListener) {
        this.context = context;
        this.cropListingList = cropListingList;
        this.cropClickListener = cropClickListener;
    }

    @NonNull
    @Override
    public CropViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_crop_image, parent, false);
        return new CropViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CropViewHolder holder, int position) {
        if (position < 0 || position >= cropListingList.size()) {
            Log.e("CropImageAdapter", "Invalid position: " + position);
            return;
        }

        CropListing cropListing = cropListingList.get(position);
        if (cropListing != null) {
            String cropName = cropListing.getCropName();

            // Fetch translations from CropTranslation
            CropTranslation.getTranslation(cropName, new CropTranslation.TranslationCallback() {
                @Override
                public void onTranslationFetched(String[] translations) {
                    if (translations != null && translations.length >= 3) {
                        String hindiName = translations[0];
                        String kannadaName = translations[1];
                        String marathiName = translations[2];

                        // Combine crop names in all three languages
                        String combinedText = cropName.toUpperCase() + " (" + hindiName + ", " + kannadaName + ", " + marathiName + ")";
                        holder.cropNameTextView.setText(combinedText);
                    } else {
                        // In case of no translations, set a fallback text
                        holder.cropNameTextView.setText(cropName);
                    }
                }
            });

            // Load image using Picasso with error handling
            Picasso.get()
                    .load(cropListing.getImageUrl())
                    .placeholder(R.drawable.placeholder_image) // Placeholder while loading
                    .error(R.drawable.error_image) // Image if there's an error
                    .into(holder.cropImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            Log.d("CropImageAdapter", "Image loaded successfully");
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("CropImageAdapter", "Error loading image: " + e.getMessage());
                            Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    });

            // Set click listener for the item with error handling
            holder.itemView.setOnClickListener(v -> {
                try {
                    cropClickListener.onCropClick(cropListing);
                } catch (Exception e) {
                    Log.e("CropImageAdapter", "Error on crop click: " + e.getMessage());
                    Toast.makeText(context, "Error handling crop click", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e("CropImageAdapter", "Null CropListing at position " + position);
        }
    }

    @Override
    public int getItemCount() {
        return cropListingList != null ? cropListingList.size() : 0;
    }

    static class CropViewHolder extends RecyclerView.ViewHolder {
        TextView cropNameTextView;
        ImageView cropImageView;

        public CropViewHolder(@NonNull View itemView) {
            super(itemView);
            cropNameTextView = itemView.findViewById(R.id.cropNameTextView);
            cropImageView = itemView.findViewById(R.id.cropImageView);
        }
    }
}
