package com.smartgrains.krishimitra;

public class TraderModel {
    private String traderId;
    private String traderName;
    private String shopName;
    private String shopAddress;
    private String phoneNumber;
    private String minPrice;
    private String maxPrice;
    private String quantity;
    private String unit; // New field for unit
    private String state;
    private String district;
    private String taluka;

    // Default constructor required for calls to DataSnapshot.getValue(TraderModel.class)
    public TraderModel() {
    }

    public TraderModel(String traderName, String shopName, String shopAddress, String phoneNumber,
                       String minPrice, String maxPrice, String quantity, String unit, // Add unit here
                       String state, String district, String taluka, String traderId) {
        this.traderName = traderName;
        this.shopName = shopName;
        this.shopAddress = shopAddress;
        this.phoneNumber = phoneNumber;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.quantity = quantity;
        this.unit = unit; // Initialize unit
        this.state = state;
        this.district = district;
        this.taluka = taluka;
        this.traderId = traderId; // Initialize traderId
    }

    // Getters for all fields
    public String getTraderId() {
        return traderId;
    }

    public String getTraderName() {
        return traderName;
    }

    public String getShopName() {
        return shopName;
    }

    public String getShopAddress() {
        return shopAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
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

    public String getUnit() { // New getter for unit
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
}
