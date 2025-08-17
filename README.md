# 🌾 Krishi Mitra (कृषि मित्र)
### *Empowering Agriculture Through Technology*

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-orange.svg)](https://firebase.google.com)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Version](https://img.shields.io/badge/Version-2.7-brightgreen.svg)]()

> **"Grow Dreams, Empower Farmers And Strengthen Agriculture"**

Krishi Mitra is a comprehensive Android agricultural marketplace application that bridges the gap between farmers and traders across India, promoting transparency, fairness, and sustainability in agriculture.

## 📱 Features

### 🌟 **For Farmers**
- **Smart Crop Discovery** - Browse available crops with multilingual support
- **Location-Based Trading** - Find traders in your area (Taluka → District → State)
- **Direct Communication** - Connect directly with traders via phone
- **Fair Price Access** - Get transparent pricing without middlemen
- **Profile Management** - Manage personal and farming details

### 🏪 **For Traders**
- **Crop Listing** - Post buying requirements with price ranges
- **Inventory Management** - Manage multiple crop listings
- **Location-Based Visibility** - Reach farmers in your operational area
- **Rating System** - Build trust through customer reviews
- **Business Profile** - Showcase shop details and location

### 👨‍💼 **For Administrators**
- **Crop Database Management** - Add new crops with images
- **Location Management** - Maintain hierarchical location data
- **Multilingual Support** - Manage crop name translations
- **Content Moderation** - Oversee platform quality

## 🌍 **Multilingual Support**
- **English** 🇬🇧
- **हिन्दी (Hindi)** 🇮🇳
- **ಕನ್ನಡ (Kannada)** 🇮🇳
- **मराठी (Marathi)** 🇮🇳

## 🏗️ **Technical Architecture**

### **Tech Stack**
- **Frontend**: Native Android (Java)
- **Backend**: Firebase Realtime Database
- **Storage**: Firebase Storage
- **Authentication**: Firebase Auth
- **Image Loading**: Picasso
- **Location Services**: Google Location Services
- **UI Framework**: Material Design Components

### **Project Structure**

app/
├── src/main/java/org/smartgrains/krishimitra/
│ ├── activities/ # Activity classes
│ ├── adapters/ # RecyclerView adapters
│ ├── models/ # Data models
│ ├── utils/ # Utility classes
│ └── fragments/ # Fragment classes
├── res/
│ ├── layout/ # XML layouts
│ ├── values/ # Strings, colors, themes
│ ├── values-hi/ # Hindi translations
│ ├── values-kn/ # Kannada translations
│ ├── values-mr/ # Marathi translations
│ └── drawable/ # Images and icons
└── AndroidManifest.xml

### **Database Structure**
Firebase Realtime Database
├── Users/ # User profiles (Farmers & Traders)
│ ├── {userId}/
│ │ ├── role: "Farmer" | "Trader"
│ │ ├── location: {state, district, taluka}
│ │ └── Listings/ # Trader crop listings
├── CropResource/ # Available crops with images
├── TranslatedCropNames/ # Multilingual crop names
├── LocationData/ # Hierarchical location data
└── AppVersion/ # Version control

## 🚀 **Getting Started**

### **Prerequisites**
- Android Studio Arctic Fox or newer
- Android SDK (API level 21+)
- Firebase project setup
- Google Services configuration

### **Installation**

1. **Clone the repository**
git clone https://github.com/yourusername/krishi-mitra.git
cd krishi-mitra

2. **Firebase Setup**
- Create a Firebase project at [Firebase Console](https://console.firebase.google.com)
- Enable Realtime Database and Storage
- Download `google-services.json` and place it in `app/` directory

3. **Open in Android Studio**
- Open Android Studio
- Select "Open an existing project"
- Navigate to the cloned repository

4. **Build and Run**
./gradlew assembleDebug


### **Configuration**

#### **Firebase Database Rules**
{
"rules": {
"Users": {
".read": "auth != null",
".write": "auth != null"
},
"CropResource": {
".read": true,
".write": "auth != null"
},
"TranslatedCropNames": {
".read": true,
".write": "auth != null"
}
}
}


#### **Required Permissions**
<uses-permission android:name="android.permission.INTERNET" /> <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" /> <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" /> ```

📊 Key Functionalities
Smart Location Matching
Hierarchical Fallback: Taluka → District → State → All India

GPS Integration: Automatic location detection for farmers

Manual Override: Option to select location manually

Multilingual Crop Management
Dynamic crop name translations

Language-specific UI elements

Persistent language preferences

Secure Communication
Direct phone integration

Privacy-focused data handling

Secure Firebase authentication

🎯 Impact & Benefits
🚫 Reduces Exploitation - Eliminates middlemen for fair pricing

📈 Enhances Livelihoods - Direct farmer-trader connections

💪 Strengthens Rural Economies - Technology-driven market access

🌱 Promotes Sustainable Farming - Better market connectivity

🤝 Empowers Communities - Digital inclusion for rural areas

MIT License

Copyright (c) 2024 Smart Grains

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

