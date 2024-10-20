package org.smartgrains.krishimitra;

import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

    private List<TraderModel> traderList;

    public TraderAdapter(List<TraderModel> traderList) {
        this.traderList = traderList;
    }

    @NonNull
    @Override
    public TraderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trader, parent, false);
        return new TraderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TraderViewHolder holder, int position) {
        TraderModel trader = traderList.get(position);
        holder.traderNameTextView.setText(trader.getTraderName());
        holder.shopNameTextView.setText(trader.getShopName());
        holder.shopAddressTextView.setText(trader.getShopAddress());
        holder.phoneNumberTextView.setText(trader.getPhoneNumber());
        holder.priceTextView.setText("Rs " + trader.getMinPrice() + "  -  " + "Rs " + trader.getMaxPrice());
        holder.quantityTextView.setText(trader.getQuantity() + " " + trader.getUnit()); // Include unit

        // Set click listener to navigate to TraderSpecificDetailsActivity
        holder.itemView.setOnClickListener(v -> {
            // Create an intent to navigate to TraderSpecificDetailsActivity
            Intent intent = new Intent(holder.itemView.getContext(), TraderSpecificDetailsActivity.class);
            // Passing Trader ID and other relevant details
            intent.putExtra("TRADER_ID", trader.getTraderId());  // Use trader ID to fetch more details
            intent.putExtra("TRADER_NAME", trader.getTraderName());
            intent.putExtra("SHOP_NAME", trader.getShopName());
            intent.putExtra("SHOP_ADDRESS", trader.getShopAddress());
            intent.putExtra("PHONE_NUMBER", trader.getPhoneNumber());

            // Start the TraderSpecificDetailsActivity with the intent
            holder.itemView.getContext().startActivity(intent);
        });

        // Click listener for the phone icon
        holder.phoneIcon.setOnClickListener(v -> {
            String phoneNumber = trader.getPhoneNumber();
            Context context = holder.itemView.getContext();

            // Copy the phone number to clipboard
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("phone number", phoneNumber);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "Phone number copied to clipboard", Toast.LENGTH_SHORT).show();

            // Open the dialer with the phone number
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(Uri.parse("tel:" + phoneNumber));
            context.startActivity(dialIntent);
        });
    }

    @Override
    public int getItemCount() {
        return traderList.size();
    }

    // Method to update the trader list
    public void updateTraderList(List<TraderModel> newTraderList) {
        this.traderList.clear();
        this.traderList.addAll(newTraderList);
        notifyDataSetChanged();
    }

    static class TraderViewHolder extends RecyclerView.ViewHolder {
        TextView traderNameTextView;
        TextView shopNameTextView;
        TextView shopAddressTextView;
        TextView phoneNumberTextView;
        TextView priceTextView;
        TextView quantityTextView;
        ImageView phoneIcon;  // Add ImageView for the phone icon

        public TraderViewHolder(@NonNull View itemView) {
            super(itemView);
            traderNameTextView = itemView.findViewById(R.id.traderNameTextView);
            shopNameTextView = itemView.findViewById(R.id.shopNameTextView);
            shopAddressTextView = itemView.findViewById(R.id.shopAddressTextView);
            phoneNumberTextView = itemView.findViewById(R.id.phoneNumberTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
            phoneIcon = itemView.findViewById(R.id.phoneIcon);  // Initialize phone icon ImageView
        }
    }
}
