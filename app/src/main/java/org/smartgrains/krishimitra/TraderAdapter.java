package org.smartgrains.krishimitra;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TraderAdapter extends RecyclerView.Adapter<TraderAdapter.TraderViewHolder> {
    private Context context;
    private List<Trader> traderList;
    private final String currentUserId;

    public TraderAdapter(Context context, List<Trader> traderList, String currentUserId) {
        this.context = context;
        this.traderList = traderList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public TraderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_trader, parent, false);
        return new TraderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TraderViewHolder holder, int position) {
        Trader trader = traderList.get(position);
        holder.traderNameTextView.setText(trader.getTraderName());
        holder.shopNameTextView.setText(trader.getShopName());
        holder.shopAddressTextView.setText(trader.getShopAddress());
        holder.priceTextView.setText(trader.getPriceRange());
        holder.quantityTextView.setText(trader.getQuantity() + " " + trader.getUnit());
        holder.phoneNumberTextView.setText(trader.getPhoneNumber());

        // Add real-time listener for trader's average rating and review count
        DatabaseReference traderRef = FirebaseDatabase.getInstance().getReference("Users").child(trader.getUserId());
        traderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Float averageRating = snapshot.child("averageRating").getValue(Float.class);
                Long reviewCount = snapshot.child("reviewCount").getValue(Long.class);

                holder.ratingBar.setRating(averageRating != null ? averageRating : 0);
                holder.ratingTextView.setText(reviewCount != null
                        ? reviewCount + " reviews, " + (averageRating != null ? averageRating : 0) + " stars"
                        : "No reviews yet");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to fetch trader data", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up click listener for the phone icon
        holder.phoneIcon.setOnClickListener(v -> {
            String phoneNumber = trader.getPhoneNumber();
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                copyToClipboard(phoneNumber);
                openDialer(phoneNumber);
            } else {
                Toast.makeText(context, "Phone number is not available", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up click listener for the phone number TextView
        holder.phoneNumberTextView.setOnClickListener(v -> {
            String phoneNumber = trader.getPhoneNumber();
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                openDialer(phoneNumber);
            } else {
                Toast.makeText(context, "Phone number is not available", Toast.LENGTH_SHORT).show();
            }
        });

        // Show review dialog on long-click
        holder.itemView.setOnLongClickListener(v -> {
            showReviewDialog(trader, position);
            return true;
        });

        // Handle "View Reviews" button click
        holder.viewReviewsButton.setOnClickListener(v -> {
            if (holder.reviewsContainer.getVisibility() == View.GONE) {
                holder.reviewsContainer.setVisibility(View.VISIBLE);
                loadReviews(trader.getUserId(), holder.reviewsContainer); // Load reviews dynamically
            } else {
                holder.reviewsContainer.setVisibility(View.GONE);
            }
        });

        // Set up click listener for the trader item
        holder.itemView.setOnClickListener(v -> {
            String userId = trader.getUserId(); // Accessing userId
            if (userId == null) {
                Log.e("TraderAdapter", "User ID is null for trader: " + trader.getTraderName());
                Toast.makeText(context, "Trader ID is not available", Toast.LENGTH_SHORT).show();
                return; // Prevent further action
            }

            // Create intent and pass necessary details
            Intent intent = new Intent(context, TraderDetailsActivity.class);
            intent.putExtra("traderId", trader.getUserId()); // Pass trader ID
            intent.putExtra("traderName", trader.getTraderName());
            intent.putExtra("shopName", trader.getShopName());
            intent.putExtra("shopAddress", trader.getShopAddress());
            intent.putExtra("phoneNumber", trader.getPhoneNumber());
            intent.putExtra("userId", userId); // Pass the existing user ID

            try {
                context.startActivity(intent);
            } catch (Exception e) {
                Log.e("TraderAdapter", "Error starting TraderDetailsActivity: " + e.getMessage());
                Toast.makeText(context, "Failed to open trader details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return traderList != null ? traderList.size() : 0; // Ensure the list is not null
    }

    static class TraderViewHolder extends RecyclerView.ViewHolder {
        TextView traderNameTextView, shopNameTextView, shopAddressTextView, priceTextView, quantityTextView, phoneNumberTextView, ratingTextView;
        RatingBar ratingBar;
        Button viewReviewsButton;
        LinearLayout reviewsContainer;
        ImageView phoneIcon;

        public TraderViewHolder(@NonNull View itemView) {
            super(itemView);
            traderNameTextView = itemView.findViewById(R.id.traderNameTextView);
            shopNameTextView = itemView.findViewById(R.id.shopNameTextView);
            shopAddressTextView = itemView.findViewById(R.id.shopAddressTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
            ratingTextView = itemView.findViewById(R.id.ratingTextView);
            phoneNumberTextView = itemView.findViewById(R.id.phoneNumberTextView);
            phoneIcon = itemView.findViewById(R.id.callIcon);
            ratingBar = itemView.findViewById(R.id.ratingBar); // Initialize the RatingBar

            // Bind RatingBar
            ratingBar = itemView.findViewById(R.id.ratingBar);

            // Bind Button for viewing reviews
            viewReviewsButton = itemView.findViewById(R.id.viewReviewsButton);

            // Bind the container for reviews (expandable section)
            reviewsContainer = itemView.findViewById(R.id.reviewsContainer);
        }
    }

    private void copyToClipboard(String phoneNumber) {
        try {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                ClipData clip = ClipData.newPlainText("Phone Number", phoneNumber);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, "Phone number copied to clipboard", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("TraderAdapter", "Clipboard manager is null");
                Toast.makeText(context, "Failed to access clipboard", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("TraderAdapter", "Error copying to clipboard: " + e.getMessage());
            Toast.makeText(context, "Failed to copy phone number", Toast.LENGTH_SHORT).show();
        }
    }

    private void openDialer(String phoneNumber) {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e("TraderAdapter", "Error opening dialer: " + e.getMessage());
            Toast.makeText(context, "Failed to open dialer", Toast.LENGTH_SHORT).show();
        }
    }

    private void showReviewDialog(Trader trader, int position) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_review, null);

        EditText reviewEditText = dialogView.findViewById(R.id.reviewEditText);
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);

        DatabaseReference userReviewRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(trader.getUserId())
                .child("reviews")
                .child(currentUserId);

        // Builder for the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Review " + trader.getTraderName());
        builder.setView(dialogView);

        // Add buttons dynamically
        builder.setPositiveButton("Submit", null); // We'll handle the click manually
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.setNeutralButton("Delete", null); // Add the Delete button but handle visibility later

        // Create dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Get the Delete button from the dialog
        final View deleteButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
        deleteButton.setVisibility(View.GONE); // Initially hide the Delete button

        // Fetch existing review
        userReviewRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String existingReview = snapshot.child("review").getValue(String.class);
                    Float existingRating = snapshot.child("rating").getValue(Float.class);

                    reviewEditText.setText(existingReview);
                    ratingBar.setRating(existingRating != null ? existingRating : 0);

                    // Make Delete button visible since review exists
                    deleteButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to fetch review", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle Submit button click manually
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            float rating = ratingBar.getRating(); // Rating value
            String reviewText = reviewEditText.getText().toString().trim(); // Review text (optional)

            // Validate that rating is mandatory
            if (rating == 0) {
                Toast.makeText(context, "Rating is required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Prepare review data
            Map<String, Object> reviewData = new HashMap<>();
            reviewData.put("rating", rating); // Add the mandatory rating
            if (!reviewText.isEmpty()) {
                reviewData.put("review", reviewText); // Only add the review text if provided
            }
            reviewData.put("timestamp", System.currentTimeMillis());

            // Save review to Firebase
            userReviewRef.setValue(reviewData).addOnSuccessListener(unused -> {
                Toast.makeText(context, "Review submitted", Toast.LENGTH_SHORT).show();
                updateAverageRating(trader.getUserId(), position); // Update RecyclerView and ratings
                dialog.dismiss();
            });
        });

        // Handle Delete button click manually
        deleteButton.setOnClickListener(v -> {
            userReviewRef.removeValue().addOnSuccessListener(unused -> {
                Toast.makeText(context, "Review deleted", Toast.LENGTH_SHORT).show();
                updateAverageRating(trader.getUserId(), position); // Update RecyclerView and ratings
                dialog.dismiss();
            });
        });
    }

    private void updateAverageRating(String traderId, int position) {
        DatabaseReference reviewsRef = FirebaseDatabase.getInstance().getReference("Users").child(traderId).child("reviews");
        reviewsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalReviews = 0;
                float totalStars = 0;

                for (DataSnapshot reviewSnapshot : snapshot.getChildren()) {
                    Float rating = reviewSnapshot.child("rating").getValue(Float.class);
                    if (rating != null) {
                        totalStars += rating;
                        totalReviews++;
                    }
                }

                // Calculate and round average rating to 1 decimal place
                float averageRating = totalReviews > 0 ? totalStars / totalReviews : 0;
                averageRating = Math.round(averageRating * 10) / 10.0f; // Round to 1 decimal place

                DatabaseReference traderRef = FirebaseDatabase.getInstance().getReference("Users").child(traderId);

                Map<String, Object> updates = new HashMap<>();
                updates.put("averageRating", averageRating); // Update rounded value
                updates.put("reviewCount", totalReviews);

                traderRef.updateChildren(updates).addOnSuccessListener(unused -> {
                    // Notify the RecyclerView about the data change
                    notifyItemChanged(position);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to update ratings", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to load reviews dynamically
    private void loadReviews(String traderId, LinearLayout reviewsContainer) {
        DatabaseReference reviewsRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(traderId)
                .child("reviews");

        reviewsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reviewsContainer.removeAllViews(); // Clear previous reviews

                if (snapshot.exists()) {
                    for (DataSnapshot reviewSnapshot : snapshot.getChildren()) {
                        // Inflate review item layout
                        View reviewView = LayoutInflater.from(context).inflate(R.layout.review_item, reviewsContainer, false);

                        TextView reviewerNameTextView = reviewView.findViewById(R.id.reviewerNameTextView);
                        RatingBar reviewRatingBar = reviewView.findViewById(R.id.reviewRatingBar);
                        TextView reviewTextView = reviewView.findViewById(R.id.reviewTextView);

                        // Set review details
                        Float rating = reviewSnapshot.child("rating").getValue(Float.class);
                        String reviewText = reviewSnapshot.child("review").getValue(String.class);
                        String reviewerId = reviewSnapshot.getKey();

                        if (rating != null) {
                            reviewRatingBar.setRating(rating);
                        }
                        // Check if review text is empty and handle visibility
                        if (reviewText != null && !reviewText.trim().isEmpty()) {
                            reviewTextView.setText(reviewText);
                            reviewTextView.setVisibility(View.VISIBLE); // Show the TextView
                        } else {
                            reviewTextView.setVisibility(View.GONE); // Hide the TextView
                        }

                        // Fetch and set reviewer name
                        fetchReviewerName(reviewerId, reviewerNameTextView);

                        // Add the review view to the container
                        reviewsContainer.addView(reviewView);
                    }
                } else {
                    // Add a message if no reviews exist
                    TextView noReviewsTextView = new TextView(context);
                    noReviewsTextView.setText("No reviews available.");
                    noReviewsTextView.setTextColor(context.getResources().getColor(R.color.black));
                    noReviewsTextView.setTextSize(16);
                    noReviewsTextView.setGravity(Gravity.CENTER);
                    reviewsContainer.addView(noReviewsTextView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to load reviews", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Fetch reviewer name from Firebase
    private void fetchReviewerName(String reviewerId, TextView reviewerNameTextView) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(reviewerId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String firstName = snapshot.child("firstName").getValue(String.class);
                String lastName = snapshot.child("lastName").getValue(String.class);
                if (firstName != null && lastName != null) {
                    reviewerNameTextView.setText(firstName + " " + lastName);
                } else {
                    reviewerNameTextView.setText("Anonymous");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                reviewerNameTextView.setText("Anonymous");
            }
        });
    }
}
