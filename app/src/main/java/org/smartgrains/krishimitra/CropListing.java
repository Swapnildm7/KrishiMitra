package org.smartgrains.krishimitra;

import android.os.Parcel;
import android.os.Parcelable;

public class CropListing implements Parcelable {
    private String listingId;
    private String cropName;
    private String minPrice;
    private String maxPrice;
    private String quantity;
    private String unit;
    private String userId;
    private String imageUrl; // New field for the crop image URL

    // Default constructor
    public CropListing() {}

    // Constructor to initialize all fields
    public CropListing(String listingId, String cropName, String minPrice, String maxPrice, String quantity, String unit, String userId, String imageUrl) {
        this.listingId = listingId;
        this.cropName = cropName;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.quantity = quantity;
        this.unit = unit;
        this.userId = userId;
        this.imageUrl = imageUrl; // Initialize imageUrl
    }

    // Getters and Setters
    public String getListingId() {
        return listingId;
    }

    public void setListingId(String listingId) {
        this.listingId = listingId;
    }

    public String getCropName() {
        return cropName;
    }

    public void setCropName(String cropName) {
        this.cropName = cropName;
    }

    public String getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(String minPrice) {
        this.minPrice = minPrice;
    }

    public String getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(String maxPrice) {
        this.maxPrice = maxPrice;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // Parcelable implementation
    protected CropListing(Parcel in) {
        listingId = in.readString();
        cropName = in.readString();
        minPrice = in.readString();
        maxPrice = in.readString();
        quantity = in.readString();
        unit = in.readString();
        userId = in.readString();
        imageUrl = in.readString(); // Read imageUrl from Parcel
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(listingId);
        dest.writeString(cropName);
        dest.writeString(minPrice);
        dest.writeString(maxPrice);
        dest.writeString(quantity);
        dest.writeString(unit);
        dest.writeString(userId);
        dest.writeString(imageUrl); // Write imageUrl to Parcel
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CropListing> CREATOR = new Creator<CropListing>() {
        @Override
        public CropListing createFromParcel(Parcel in) {
            return new CropListing(in);
        }

        @Override
        public CropListing[] newArray(int size) {
            return new CropListing[size];
        }
    };
}
