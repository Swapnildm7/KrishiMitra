package org.smartgrains.krishimitra;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;

public class CropAdapter extends RecyclerView.Adapter<CropAdapter.CropViewHolder> {
    private List<Crop> cropList; // Change to List<Crop>
    private Context context;
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

    public CropAdapter(Context context, List<Crop> cropList) {
        this.context = context;
        this.cropList = cropList;

        // Initialize Firebase reference to "TranslatedCropNames" node
        translatedCropNamesRef = FirebaseDatabase.getInstance().getReference("TranslatedCropNames");

        // Retrieve user's preferred language code from SharedPreferences
        SharedPreferences preferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        userLanguageCode = preferences.getString("LanguageCode", "en").trim().toLowerCase();
    }

    @NonNull
    @Override
    public CropViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_trader_crop_listing, parent, false);
        return new CropViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CropViewHolder holder, int position) {
        Crop crop = cropList.get(position);

        if (crop != null) {
            String originalCropName = crop.getCropName();

            // Fetch the translated crop name from Firebase
            translatedCropNamesRef.child(originalCropName)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                // Get the translated name from Firebase if available
                                String translatedName = snapshot.child(LANGUAGE_TRANSLATION_MAP.getOrDefault(userLanguageCode, "English"))
                                        .getValue(String.class);

                                // Set the translated name or fallback to original crop name
                                holder.cropNameTextView.setText(translatedName != null ? translatedName : originalCropName);
                            } else {
                                // If translation is not found, fallback to the original crop name
                                holder.cropNameTextView.setText(originalCropName);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle Firebase errors gracefully
                            holder.cropNameTextView.setText(originalCropName);
                        }
                    });

            // Set other crop details
            holder.minPriceTextView.setText(context.getString(R.string.minPriceTv) + " " + (crop.getMinPrice() != null ? crop.getMinPrice() : "N/A"));
            holder.maxPriceTextView.setText(context.getString(R.string.maxPriceTv) + " " + (crop.getMaxPrice() != null ? crop.getMaxPrice() : "N/A"));
            holder.quantityTextView.setText(context.getString(R.string.quantityTv) + " " + (crop.getQuantity() != null ? crop.getQuantity() : "N/A"));
            holder.unitTextView.setText(context.getString(R.string.unitTv) + " " + (crop.getUnit() != null ? crop.getUnit() : "N/A"));
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
        TextView cropNameTextView, minPriceTextView, maxPriceTextView, quantityTextView, unitTextView;
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
