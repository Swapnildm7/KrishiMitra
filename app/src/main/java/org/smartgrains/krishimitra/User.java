package org.smartgrains.krishimitra;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    private String userId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String password; // Password field
    private String role; // "Farmer" or "Trader"
    private String shopName; // Only for Trader
    private String shopAddress; // Only for Trader
    private String address; // Only for Farmer
    private String state;
    private String district;
    private String taluka;

    // Constructor for Trader
    public User(String userId, String firstName, String lastName, String phoneNumber, String password, String role,
                String shopName, String shopAddress, String state, String district, String taluka) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.role = role;
        this.shopName = shopName;
        this.shopAddress = shopAddress;
        this.state = state;
        this.district = district;
        this.taluka = taluka;
        this.address = null; // Initialize address to null for Traders
    }

    // Constructor for Farmer
    public User(String userId, String firstName, String lastName, String phoneNumber, String password, String role,
                String address, String state, String district, String taluka) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.role = role;
        this.shopName = null; // Initialize shopName to null for Farmers
        this.shopAddress = null; // Initialize shopAddress to null for Farmers
        this.address = address;
        this.state = state;
        this.district = district;
        this.taluka = taluka;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getTaluka() {
        return taluka;
    }

    public void setTaluka(String taluka) {
        this.taluka = taluka;
    }

    // Parcelable implementation
    protected User(Parcel in) {
        userId = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        phoneNumber = in.readString();
        password = in.readString();
        role = in.readString();
        state = in.readString();
        district = in.readString();
        taluka = in.readString();

        // Read based on role
        if ("Trader".equals(role)) {
            shopName = in.readString();
            shopAddress = in.readString();
            address = null; // For Traders, address is not used
        } else {
            address = in.readString();
            shopName = null; // For Farmers, shopName is not used
            shopAddress = null; // For Farmers, shopAddress is not used
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(phoneNumber);
        dest.writeString(password);
        dest.writeString(role);
        dest.writeString(state);
        dest.writeString(district);
        dest.writeString(taluka);

        // Write based on role
        if ("Trader".equals(role)) {
            dest.writeString(shopName);
            dest.writeString(shopAddress);
        } else {
            dest.writeString(address);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
