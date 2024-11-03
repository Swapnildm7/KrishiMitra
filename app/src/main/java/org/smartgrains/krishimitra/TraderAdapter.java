package org.smartgrains.krishimitra;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TraderAdapter extends RecyclerView.Adapter<TraderAdapter.TraderViewHolder> {
    private Context context;
    private List<Trader> traderList;

    public TraderAdapter(Context context, List<Trader> traderList) {
        this.context = context;
        this.traderList = traderList;
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
        TextView traderNameTextView;
        TextView shopNameTextView;
        TextView shopAddressTextView;
        TextView priceTextView;
        TextView quantityTextView;
        TextView phoneNumberTextView;
        ImageView phoneIcon;

        public TraderViewHolder(@NonNull View itemView) {
            super(itemView);
            traderNameTextView = itemView.findViewById(R.id.traderNameTextView);
            shopNameTextView = itemView.findViewById(R.id.shopNameTextView);
            shopAddressTextView = itemView.findViewById(R.id.shopAddressTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
            phoneNumberTextView = itemView.findViewById(R.id.phoneNumberTextView);
            phoneIcon = itemView.findViewById(R.id.callIcon);
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
}
