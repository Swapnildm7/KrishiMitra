package org.smartgrains.krishimitra;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class FarmerDashboardActivity extends AppCompatActivity implements CropFilterBottomSheetFragment.OnCropsSelectedListener, NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "FarmerDashboardActivity";
    private static final String CURRENT_VERSION = "2.7";
    private RecyclerView cropImageRecyclerView;
    private ProgressBar progressBar;
    private CropImageAdapter cropImageAdapter;
    private List<CropListing> cropListingList;
    private Map<String, CropListing> allCropsMap;
    private DatabaseReference databaseReference;
    private String farmerState, farmerDistrict, farmerTaluka;
    private LinearLayout cropFilterButtonLayout, buttonTrader;
    private ImageView cropFilterButtonIcon, userProfile, headerProfile, icon_trader;
    private TextView cropFilterButtonText;
    private FloatingActionButton fabRefresh;
    private int previousCropCount = 0;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;
    private String userId;
    private String userRole; // Store user role
    private static final String UNKNOWN_ROLE = "Unknown role"; // Define unknown role string

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.setLocale(this);
        setContentView(R.layout.activity_farmer_dashboard);

        // Make status bar transparent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            EdgeToEdgeUtil.configureEdgeToEdge(getWindow());
        }
        checkForUpdate();

        // Initialize views
        cropImageRecyclerView = findViewById(R.id.cropImageRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        cropFilterButtonLayout = findViewById(R.id.cropFilterButton); // Crop filter button layout
        fabRefresh = findViewById(R.id.fabRefresh); // Refresh button layout
        buttonTrader = findViewById(R.id.buttonTraders); // Trader button layout)
        userProfile = findViewById(R.id.userProfile); // User profile ImageView

        // Initialize TextView and ImageView within crop filter button
        cropFilterButtonIcon = cropFilterButtonLayout.findViewById(R.id.icon_crop_filter);
        cropFilterButtonText = cropFilterButtonLayout.findViewById(R.id.text_crop_filter);

        // Initialize TextView and ImageView within trader button
        icon_trader = buttonTrader.findViewById(R.id.icon_trader);
        cropFilterButtonText = buttonTrader.findViewById(R.id.text_trader);

        cropListingList = new ArrayList<>();
        allCropsMap = new HashMap<>();

        cropImageAdapter = new CropImageAdapter(this, cropListingList, cropListing -> {
            Intent intent = new Intent(FarmerDashboardActivity.this, TraderListActivity.class);
            intent.putExtra("CROP_NAME", cropListing.getCropName());
            intent.putExtra("FARMER_STATE", farmerState);
            intent.putExtra("FARMER_DISTRICT", farmerDistrict);
            intent.putExtra("FARMER_TALUKA", farmerTaluka);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });

        // Initialize drawer layout and navigation view
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        // Setup Drawer Toggle
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.openDrawer, R.string.closeDrawer);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // Set NavigationItemSelectedListener
        navigationView.setNavigationItemSelectedListener(this);

        // Get the header view from the NavigationView
        View headerView = navigationView.getHeaderView(0);

        // Find the TextViews in the header layout
        TextView userFirstName = headerView.findViewById(R.id.userFirstName);
        TextView userPhoneNumber = headerView.findViewById(R.id.userPhoneNumber);
        headerProfile = headerView.findViewById(R.id.headerProfile);

        userId = getIntent().getStringExtra("USER_ID");

        // Fetch user details and update header
        fetchHeaderDetails(userId, userFirstName, userPhoneNumber);

        cropImageRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cropImageRecyclerView.setAdapter(cropImageAdapter);

        // Set onClickListener for crop filter button
        cropFilterButtonLayout.setOnClickListener(v -> showCropFilterBottomSheet());

        // Set onClickListener for refresh button
        fabRefresh.setOnClickListener(v -> fetchCropListings());

        // Set onClickListener for Trader button
        buttonTrader.setOnClickListener(v -> {
            Intent intent = new Intent(FarmerDashboardActivity.this, TradersDetailsList.class);
            intent.putExtra("FARMER_STATE", farmerState);
            intent.putExtra("FARMER_DISTRICT", farmerDistrict);
            intent.putExtra("FARMER_TALUKA", farmerTaluka);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });

        // Set onClickListener for User Profile (to open the Drawer)
        userProfile.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        fetchFarmerDetails();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        // Handle each item in the navigation menu
        if (id == R.id.nav_home) {
            // You can navigate to the Home/Dashboard screen here if needed
        } else if (id == R.id.nav_profile) {
            closeDrawerAndNavigate(() -> navigateToProfileActivity());
        } else if (id == R.id.nav_contact_us) {
            closeDrawerAndNavigate(() -> navigateToContactUsActivity());
        } else if (id == R.id.nav_language) {
            closeDrawerAndNavigate(() -> navigateToLanguageSelection());
        } else if (id == R.id.nav_logout) {
            closeDrawerAndNavigate(() -> performLogout());
        } else if (id == R.id.nav_privacy_policy) {
            String privacyPolicyUrl = "https://smartgrains.org/PrivacyPolicyKrishiMitra.html";
            // Create an Intent to open the URL in a browser
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl));
            startActivity(intent);
        }
        // Close the drawer after item selection
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void navigateToLanguageSelection() {
        Intent intent = new Intent(FarmerDashboardActivity.this, LanguageSelectionActivity.class);
        intent.putExtra("USER_ROLE", userRole);
        intent.putExtra("Dashboard", "FarmerDashboardActivity");
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out); // Smooth animation
    }

    private void fetchHeaderDetails(String userId, TextView userFirstName, TextView userPhoneNumber) {
        // Define your logic to fetch and update the header details
        // For example:
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String firstName = snapshot.child("firstName").getValue(String.class);
                String phoneNumber = snapshot.child("phoneNumber").getValue(String.class);
                userRole = snapshot.child("role").getValue(String.class);
                userFirstName.setText(firstName != null ? firstName : "Unknown");
                userPhoneNumber.setText(phoneNumber != null ? phoneNumber : "Unknown");

                // Fetch the greetings from the localized resources
                String[] greetings = getResources().getStringArray(R.array.greetings_array);

                // Generate a personalized random greeting
                String personalizedGreeting = getPersonalizedGreeting(greetings, firstName);

                // Display the greeting as a toast
                Toast.makeText(FarmerDashboardActivity.this, personalizedGreeting, Toast.LENGTH_SHORT).show();

                // Find the ImageView by ID
                ImageView roleImageView = findViewById(R.id.userProfile);

                if (userRole.equals("Farmer")) {
                    // Set image for Farmer
                    headerProfile.setImageResource(R.drawable.ic_farmer_profile);
                    roleImageView.setImageResource(R.drawable.ic_farmer_profile);
                } else if (userRole.equals("Trader")) {
                    // Set image for Trader
                    roleImageView.setImageResource(R.drawable.ic_trader_profile);
                    headerProfile.setImageResource(R.drawable.ic_trader_profile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to fetch user details: " + error.getMessage());
            }
        });
    }

    // Function to get a personalized random greeting
    private String getPersonalizedGreeting(String[] greetings, String userName) {
        Random random = new Random();
        int index = random.nextInt(greetings.length); // Generate random index
        return String.format(greetings[index], userName); // Replace %s with user's name
    }

    private void closeDrawerAndNavigate(Runnable navigationAction) {
        // Add a one-time listener to handle navigation
        DrawerLayout.SimpleDrawerListener drawerListener = new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerClosed(View drawerView) {
                navigationAction.run(); // Execute the navigation action
                drawerLayout.removeDrawerListener(this); // Remove the listener to prevent memory leaks
            }
        };

        // Add the listener and close the drawer
        drawerLayout.addDrawerListener(drawerListener);
        drawerLayout.closeDrawer(GravityCompat.START); // Close the drawer
    }

    private void showCropFilterBottomSheet() {
        CropFilterBottomSheetFragment bottomSheetFragment = new CropFilterBottomSheetFragment(this, allCropsMap);
        bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
    }

    private void fetchFarmerDetails() {
        String userId = getIntent().getStringExtra("USER_ID");
        progressBar.setVisibility(View.VISIBLE);
        if (userId == null) {
            Log.e(TAG, "User ID is null");
            Toast.makeText(this, "User ID is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                farmerState = snapshot.child("state").getValue(String.class);
                farmerDistrict = snapshot.child("district").getValue(String.class);
                farmerTaluka = snapshot.child("taluka").getValue(String.class);

                if (farmerState == null || farmerDistrict == null || farmerTaluka == null) {
                    Log.e(TAG, "Farmer location details are incomplete.");
                    Toast.makeText(FarmerDashboardActivity.this, "Failed to fetch complete farmer details.", Toast.LENGTH_SHORT).show();
                    return;
                }

                fetchCropListings();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
                Toast.makeText(FarmerDashboardActivity.this, "Failed to fetch farmer details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchCropListings() {
        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> uniqueCropNames = new HashSet<>();
                allCropsMap.clear();

                // Separate lists for hierarchical fallback
                List<DataSnapshot> talukaListings = new ArrayList<>();
                List<DataSnapshot> districtListings = new ArrayList<>();
                List<DataSnapshot> stateListings = new ArrayList<>();
                List<DataSnapshot> allListings = new ArrayList<>();

                // Organize listings into hierarchical buckets
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String role = userSnapshot.child("role").getValue(String.class);
                    if ("Trader".equals(role)) {
                        for (DataSnapshot listingSnapshot : userSnapshot.child("Listings").getChildren()) {
                            String state = listingSnapshot.child("state").getValue(String.class);
                            String district = listingSnapshot.child("district").getValue(String.class);
                            String taluka = listingSnapshot.child("taluka").getValue(String.class);
                            String cropName = listingSnapshot.child("cropName").getValue(String.class);

                            // Skip incomplete data
                            if (state == null || district == null || taluka == null || cropName == null) {
                                Log.w(TAG, "Incomplete listing data for trader: " + userSnapshot.getKey() + ". Skipping this listing.");
                                continue;
                            }

                            // Categorize listings by location
                            if (farmerTaluka.equals(taluka) && farmerDistrict.equals(district) && farmerState.equals(state)) {
                                talukaListings.add(listingSnapshot);
                            } else if (farmerDistrict.equals(district) && farmerState.equals(state)) {
                                districtListings.add(listingSnapshot);
                            } else if (farmerState.equals(state)) {
                                stateListings.add(listingSnapshot);
                            } else {
                                allListings.add(listingSnapshot);
                            }
                        }
                    }
                }

                // Update the TextView with appropriate messages based on listings
                TextView textMessageCrop = findViewById(R.id.textMessageCrop);

                boolean listingsFound = processListings(talukaListings, uniqueCropNames, "Crop listings in: " + farmerTaluka, textMessageCrop);
                if (!listingsFound) {
                    listingsFound = processListings(districtListings, uniqueCropNames, "No listings found in your Location. Crop listings in your District: " + farmerDistrict, textMessageCrop);
                }
                if (!listingsFound) {
                    listingsFound = processListings(stateListings, uniqueCropNames, "No listings found in your District. Crop listings in your State: " + farmerState, textMessageCrop);
                }
                if (!listingsFound) {
                    processListings(allListings, uniqueCropNames, "No listings found for your State. Showing all available listings.", textMessageCrop);
                }

                // Update UI
                cropListingList.clear();
                cropListingList.addAll(allCropsMap.values());
                cropImageAdapter.notifyDataSetChanged();

                if (cropListingList.isEmpty()) {
                    textMessageCrop.setText("No crop listings available at the moment.");
                    Toast.makeText(FarmerDashboardActivity.this, "No crop listings available at the moment.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(FarmerDashboardActivity.this, cropListingList.size() + " unique crop listings displayed.", Toast.LENGTH_SHORT).show();
                }

                previousCropCount = cropListingList.size();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load crop listings: " + error.getMessage());
                Toast.makeText(FarmerDashboardActivity.this, "An error occurred while fetching crop listings. Please try again later.", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    // Helper function to process listings and update the TextView
    private boolean processListings(List<DataSnapshot> listings, Set<String> uniqueCropNames, String message, TextView textMessageCrop) {
        boolean addedListings = false;

        for (DataSnapshot listingSnapshot : listings) {
            String cropName = listingSnapshot.child("cropName").getValue(String.class);
            if (!uniqueCropNames.contains(cropName)) {
                CropListing cropListing = new CropListing();
                cropListing.setCropName(cropName);
                cropListing.setImageUrl(listingSnapshot.child("imageUrl").getValue(String.class));
                cropListing.setUserId(listingSnapshot.child("userId").getValue(String.class));

                uniqueCropNames.add(cropName);
                allCropsMap.put(cropName, cropListing);
                addedListings = true;
            }
        }

        if (addedListings) {
            textMessageCrop.setText(message);
        }
        return addedListings;
    }

    @Override
    public void onCropsSelected(List<String> selectedCrops) {
        progressBar.setVisibility(View.VISIBLE);

        cropListingList.clear();
        for (CropListing crop : allCropsMap.values()) {
            if (selectedCrops.contains(crop.getCropName())) {
                cropListingList.add(crop);
            }
        }

        cropImageAdapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
    }

    private void checkForUpdate() {
        DatabaseReference appInfoRef = FirebaseDatabase.getInstance().getReference("AppVersion");

        appInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String latestVersion = snapshot.getValue(String.class);
                if (latestVersion != null && !CURRENT_VERSION.equals(latestVersion)) {
                    showUpdateDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to check for updates: " + error.getMessage());
            }
        });
    }

    private void navigateToProfileActivity() {
        closeOptionsMenu();
        if (userRole != null) {
            Intent intent;
            if ("farmer".equalsIgnoreCase(userRole.trim())) {
                intent = new Intent(FarmerDashboardActivity.this, FarmerProfileActivity.class);
            } else if ("trader".equalsIgnoreCase(userRole.trim())) {
                intent = new Intent(FarmerDashboardActivity.this, TraderProfileActivity.class);
            } else {
                Log.d(TAG, UNKNOWN_ROLE + ": " + userRole); // Log unknown role
                Toast.makeText(this, UNKNOWN_ROLE + ": " + userRole, Toast.LENGTH_SHORT).show();
                return; // Exit if role is unknown
            }
            intent.putExtra("USER_ID", userId); // Pass the user ID to the profile activity
            startActivity(intent);
        } else {
            Log.d(TAG, "User role not yet fetched. Please wait."); // Log message if role is not available
            Toast.makeText(this, "User role not available", Toast.LENGTH_SHORT).show();
        }
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void navigateToContactUsActivity() {
        closeOptionsMenu();
        // Navigate to the ContactUsActivity
        Intent intent = new Intent(FarmerDashboardActivity.this, ContactUsActivity.class);
        intent.putExtra("USER_ID", userId);
        intent.putExtra("USER_ROLE", userRole);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void performLogout() {
        closeOptionsMenu();

        // Access SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Preserve the language selection
        String languageCode = sharedPreferences.getString("LanguageCode", "en"); // Default to "en" if not set
        editor.clear(); // Clear all data
        editor.putString("LanguageCode", languageCode); // Restore the language preference
        editor.putBoolean("IS_FIRST_TIME_USER", false); // Mark as not a first-time user
        editor.apply();

        // Redirect to Login Page
        Intent loginIntent = new Intent(FarmerDashboardActivity.this, LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
        startActivity(loginIntent);

        // Finish the activity
        finish();
    }

    private void showUpdateDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Update Required")
                .setMessage("A new version of the app is available. Please update to continue.")
                .setCancelable(false)
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openPlayStore();
                    }
                })
                .show();
    }

    private void openPlayStore() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
            finish(); // Close the activity to ensure users can only continue after updating
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open Play Store.", Toast.LENGTH_SHORT).show();
        }
    }
}
