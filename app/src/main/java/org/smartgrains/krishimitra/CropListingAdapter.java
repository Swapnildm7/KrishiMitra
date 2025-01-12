package org.smartgrains.krishimitra;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;

public class CropListingAdapter extends RecyclerView.Adapter<CropListingAdapter.CropViewHolder> {
    private Context context;
    private List<CropListing> cropListingList;
    private DatabaseReference translatedCropNamesRef;
    private String userLanguageCode;

    // Define a map to associate language codes with Firebase translation keys
    private static final Map<String, String> LANGUAGE_TRANSLATION_MAP = Map.of(
            "mr", "Marathi",
            "kn", "Kannada",
            "hi", "Hindi",
            "en", "English"
            // Add other mappings as necessary
    );

    public CropListingAdapter(Context context, List<CropListing> cropListingList) {
        this.context = context;
        this.cropListingList = cropListingList;

        // Initialize Firebase reference to "TranslatedCropNames" node
        translatedCropNamesRef = FirebaseDatabase.getInstance().getReference("TranslatedCropNames");

        // Retrieve user's preferred language code from SharedPreferences
        SharedPreferences preferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        userLanguageCode = preferences.getString("LanguageCode", "en").trim().toLowerCase();
        Log.d("CropListingAdapter", "User language code: " + userLanguageCode);
    }

    @NonNull
    @Override
    public CropViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("CropListingAdapter", "onCreateViewHolder called");
        View view = LayoutInflater.from(context).inflate(R.layout.item_crop, parent, false);
        return new CropViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CropViewHolder holder, int position) {
        if (cropListingList == null || position < 0 || position >= cropListingList.size()) {
            Log.e("CropListingAdapter", "Invalid position or empty list");
            return;
        }

        CropListing cropListing = cropListingList.get(position);
        if (cropListing != null) {
            String originalCropName = cropListing.getCropName();

            Log.d("CropListingAdapter", "Original crop name: " + originalCropName);

            // Fetch the translated crop name from Firebase
            translatedCropNamesRef.child(originalCropName)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Log.d("FirebaseData", "Snapshot received for crop: " + originalCropName);
                            if (snapshot.exists()) {
                                Log.d("FirebaseData", "Crop found: " + originalCropName);

                                // Get the translated name from Firebase if available
                                String translatedName = snapshot.child(LANGUAGE_TRANSLATION_MAP.getOrDefault(userLanguageCode, "English"))
                                        .getValue(String.class);

                                // Set the translated name or fallback to original crop name
                                holder.cropNameTextView.setText(translatedName != null ? translatedName : originalCropName);
                            } else {
                                Log.w("CropMissing", "Crop " + originalCropName + " not found in TranslatedCropNames");
                                holder.cropNameTextView.setText(originalCropName);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle Firebase errors gracefully
                            Log.e("CropListingAdapter", "Firebase error: " + error.getMessage());
                            holder.cropNameTextView.setText(originalCropName);
                        }
                    });

            // Set other crop details (abstracted to a method)
            setCropDetails(holder, cropListing);

            // Load crop image with Picasso
            loadCropImage(holder, cropListing);

            // Set item click listener for additional actions
            holder.itemView.setOnClickListener(v -> {
                try {
                    Log.d("CropListingAdapter", "Item clicked for crop: " + originalCropName);
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

    private void setCropDetails(CropViewHolder holder, CropListing cropListing) {
        holder.minPriceTextView.setText(context.getString(R.string.minPriceTv) + " " + cropListing.getMinPrice());
        holder.maxPriceTextView.setText(context.getString(R.string.maxPriceTv) + " " + cropListing.getMaxPrice());
        holder.quantityTextView.setText(context.getString(R.string.quantityTv) + " " + cropListing.getQuantity());
        holder.unitTextView.setText(context.getString(R.string.unitTv) + " " + cropListing.getUnit());
    }

    private void loadCropImage(CropViewHolder holder, CropListing cropListing) {
        Picasso.get()
                .load(cropListing.getImageUrl())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
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
