package com.smartgrains.krishimitra;

import android.os.Parcel;
import android.os.Parcelable;

public class CropListingModel implements Parcelable {
    private String id; // Listing ID
    private String cropName;
    private String minPrice;
    private String maxPrice;
    private String quantity;
    private String unit;
    private String state;
    private String district;
    private String taluka;
    private String userId; // Add userId field

    // Default constructor required for Firebase
    public CropListingModel() {
    }

    public CropListingModel(String id, String cropName, String minPrice, String maxPrice, String quantity, String unit, String state, String district, String taluka, String userId) {
        this.id = id; // Initialize the id
        this.cropName = cropName;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.quantity = quantity;
        this.unit = unit;
        this.state = state;
        this.district = district;
        this.taluka = taluka;
        this.userId = userId; // Initialize userId
    }

    // Getter for the listing ID
    public String getListingId() {
        return id; // Return the id as the listing ID
    }

    // Setter for the listing ID
    public void setListingId(String id) {
        this.id = id; // Set the id as the listing ID
    }

    // Getter for user ID
    public String getUserId() {
        return userId; // Return userId
    }

    // Setters for the fields you want to update
    public void setMinPrice(String minPrice) {
        this.minPrice = minPrice;
    }

    public void setMaxPrice(String maxPrice) {
        this.maxPrice = maxPrice;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    // Existing getters for other fields
    public String getCropName() {
        return cropName;
    }

    public String getMinPrice() {
        return minPrice;
    }

    public String getMaxPrice() {
        return maxPrice;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public String getState() {
        return state;
    }

    public String getDistrict() {
        return district;
    }

    public String getTaluka() {
        return taluka;
    }

    // Parcelable implementation
    protected CropListingModel(Parcel in) {
        id = in.readString(); // Read the id from Parcel
        cropName = in.readString();
        minPrice = in.readString();
        maxPrice = in.readString();
        quantity = in.readString();
        unit = in.readString();
        state = in.readString();
        district = in.readString();
        taluka = in.readString();
        userId = in.readString(); // Read userId from Parcel
    }

    public static final Creator<CropListingModel> CREATOR = new Creator<CropListingModel>() {
        @Override
        public CropListingModel createFromParcel(Parcel in) {
            return new CropListingModel(in);
        }

        @Override
        public CropListingModel[] newArray(int size) {
            return new CropListingModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id); // Write the id to Parcel
        dest.writeString(cropName);
        dest.writeString(minPrice);
        dest.writeString(maxPrice);
        dest.writeString(quantity);
        dest.writeString(unit);
        dest.writeString(state);
        dest.writeString(district);
        dest.writeString(taluka);
        dest.writeString(userId); // Write userId to Parcel
    }
}
