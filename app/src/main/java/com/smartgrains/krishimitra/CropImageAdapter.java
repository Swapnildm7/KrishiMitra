package com.smartgrains.krishimitra;

import android.content.Context;
import android.content.Intent;
import android.util.Log; // Import Log for logging
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CropImageAdapter extends RecyclerView.Adapter<CropImageAdapter.CropViewHolder> {

    private List<CropImageModel> cropImageList;
    private Context context; // Context to start new activities
    private String selectedState;
    private String selectedDistrict;
    private String selectedTaluka;

    // Constructor to accept context and location details
    public CropImageAdapter(Context context, List<CropImageModel> cropImageList,
                            String selectedState, String selectedDistrict, String selectedTaluka) {
        this.context = context;
        this.cropImageList = cropImageList;
        this.selectedState = selectedState;
        this.selectedDistrict = selectedDistrict;
        this.selectedTaluka = selectedTaluka;
    }

    @NonNull
    @Override
    public CropViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_crop_image, parent, false);
        return new CropViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CropViewHolder holder, int position) {
        CropImageModel cropImage = cropImageList.get(position);
        holder.cropImageView.setImageResource(cropImage.getImageResource());
        holder.cropNameTextView.setText(cropImage.getCropName());

        // Handle item click to start TraderDetailsActivity
        holder.itemView.setOnClickListener(v -> {
            Log.d("CropImageAdapter", "Crop Name: " + cropImage.getCropName());
            Log.d("CropImageAdapter", "State: " + selectedState);
            Log.d("CropImageAdapter", "District: " + selectedDistrict);
            Log.d("CropImageAdapter", "Taluka: " + selectedTaluka);

            Intent intent = new Intent(context, TraderDetailsActivity.class);
            intent.putExtra("CROP_NAME", cropImage.getCropName());
            intent.putExtra("STATE", selectedState); // Ensure these variables hold the updated values
            intent.putExtra("DISTRICT", selectedDistrict);
            intent.putExtra("TALUKA", selectedTaluka);
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return cropImageList.size();
    }

    static class CropViewHolder extends RecyclerView.ViewHolder {
        ImageView cropImageView;
        TextView cropNameTextView;

        public CropViewHolder(@NonNull View itemView) {
            super(itemView);
            cropImageView = itemView.findViewById(R.id.cropImageView);
            cropNameTextView = itemView.findViewById(R.id.cropNameTextView);
        }
    }
}
