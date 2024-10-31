package org.smartgrains.krishimitra;

public class Crop {
    private String cropName;
    private String minPrice;
    private String maxPrice;
    private String quantity;
    private String unit;
    private String userId; // To associate with the user (trader)
    private String imageUrl; // Add this field for the image URL

    // Empty constructor for Firebase
    public Crop() {}

    // Getters and Setters
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
        return userId; // Getter for userId
    }

    public void setUserId(String userId) {
        this.userId = userId; // Setter for userId
    }

    public String getImageUrl() { // Add this method
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) { // Add this method
        this.imageUrl = imageUrl;
    }
}
