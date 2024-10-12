package com.smartgrains.krishimitra;

public class TraderModel {
    private String traderName;
    private String shopName;
    private String shopAddress;
    private String phoneNumber;
    private String minPrice;
    private String maxPrice;
    private String quantity;
    private String state;      // Added state field
    private String district;   // Added district field
    private String taluka;     // Added taluka field

    // Default constructor required for calls to DataSnapshot.getValue(TraderModel.class)
    public TraderModel() {
    }

    // Updated constructor to include state, district, and taluka
    public TraderModel(String traderName, String shopName, String shopAddress, String phoneNumber,
                       String minPrice, String maxPrice, String quantity, String state, String district, String taluka) {
        this.traderName = traderName;
        this.shopName = shopName;
        this.shopAddress = shopAddress;
        this.phoneNumber = phoneNumber;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.quantity = quantity;
        this.state = state;           // Initialize state
        this.district = district;     // Initialize district
        this.taluka = taluka;         // Initialize taluka
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

    public String getState() {         // Getter for state
        return state;
    }

    public String getDistrict() {      // Getter for district
        return district;
    }

    public String getTaluka() {        // Getter for taluka
        return taluka;
    }
}
