package org.smartgrains.krishimitra;

public class Trader {
    private String traderName;
    private String shopName;
    private String shopAddress;
    private String minPrice;
    private String maxPrice;
    private String quantity;
    private String unit; // Field for unit
    private String phoneNumber;
    private String userId; // New field for user ID

    // Getters and Setters
    public String getTraderName() {
        return traderName;
    }

    public void setTraderName(String traderName) {
        this.traderName = traderName;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getShopAddress() {
        return shopAddress;
    }

    public void setShopAddress(String shopAddress) {
        this.shopAddress = shopAddress;
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
        return unit; // Getter for unit
    }

    public void setUnit(String unit) {
        this.unit = unit; // Setter for unit
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUserId() {
        return userId; // Getter for user ID
    }

    public void setUserId(String userId) {
        this.userId = userId; // Setter for user ID
    }

    public String getPriceRange() {
        return "₹" + minPrice + " - ₹" + maxPrice;
    }
}
