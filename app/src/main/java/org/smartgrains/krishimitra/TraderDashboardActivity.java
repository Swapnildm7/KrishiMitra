package org.smartgrains.krishimitra;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class TraderDashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "TraderDashboardActivity";
    private ImageView userProfile, headerProfile;
    private TextView appName;
    private static final String CURRENT_VERSION = "2.7";
    private LinearLayout listCropLayout, viewListingsLayout; // Update to LinearLayout
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;
    private DatabaseReference databaseReference;
    private String userId;
    private String userRole; // Store user role
    private static final String UNKNOWN_ROLE = "Unknown role"; // Define unknown role string

    // Variables to store user details
    private String state, district, taluka;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.setLocale(this);
        setContentView(R.layout.activity_trader_dashboard);

        // Make status bar transparent
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        // Fetch user ID from intent
        userId = getIntent().getStringExtra("USER_ID");

        // Check if userId is null
        if (userId == null) {
            Log.e(TAG, "User ID is null, cannot fetch user role.");
            Toast.makeText(this, "User ID is not available", Toast.LENGTH_SHORT).show();
            finish(); // Optionally, close the activity
            return; // Exit the method if userId is null
        }

        checkForUpdate();

        try {
            // Initialize Views
            userProfile = findViewById(R.id.userProfile);
            appName = findViewById(R.id.appName);
            listCropLayout = findViewById(R.id.listCropLayout); // Update to LinearLayout
            viewListingsLayout = findViewById(R.id.viewListingsLayout); // Update to LinearLayout
            drawerLayout = findViewById(R.id.drawerLayout);
            navigationView = findViewById(R.id.navigationView); // Initialize NavigationView

            // Get User ID from Intent
            userId = getIntent().getStringExtra("USER_ID");

            if (userId == null) {
                throw new NullPointerException("User ID is missing in the intent extras.");
            }

            // Initialize Firebase Database reference
            databaseReference = FirebaseDatabase.getInstance().getReference("Users");

            // Fetch user details from the database
            fetchUserDetails(userId);

            // Get the header view from the NavigationView
            View headerView = navigationView.getHeaderView(0);

            // Find the TextViews in the header layout
            TextView userFirstName = headerView.findViewById(R.id.userFirstName);
            TextView userPhoneNumber = headerView.findViewById(R.id.userPhoneNumber);
            headerProfile = headerView.findViewById(R.id.headerProfile);

            // Fetch user details and update header
            fetchHeaderDetails(userId, userFirstName, userPhoneNumber);

            // Set onClickListener for List Crop Layout
            listCropLayout.setOnClickListener(v -> openListCropActivity());

            // Set onClickListener for View Listings Layout
            viewListingsLayout.setOnClickListener(v -> openEditCropListingActivity());

            // Setup Drawer Toggle
            drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.openDrawer, R.string.closeDrawer);
            drawerLayout.addDrawerListener(drawerToggle);
            drawerToggle.syncState();

            // Set NavigationItemSelectedListener
            navigationView.setNavigationItemSelectedListener(this);

            // Set onClickListener for User Profile (to open the Drawer)
            userProfile.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        } catch (Exception e) {
            Log.e(TAG, "Initialization error: " + e.getMessage());
            Toast.makeText(this, "Error initializing dashboard. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    // Fetch user details and update header TextViews
    private void fetchHeaderDetails(String userId, TextView userFirstName, TextView userPhoneNumber) {
        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Fetch the first name and phone number from the database
                    String firstName = dataSnapshot.child("firstName").getValue(String.class);
                    String phoneNumber = dataSnapshot.child("phoneNumber").getValue(String.class);

                    // Fetch the greetings from the localized resources
                    String[] greetings = getResources().getStringArray(R.array.greetings_array);

                    // Generate a personalized random greeting
                    String personalizedGreeting = getPersonalizedGreeting(greetings, firstName);

                    // Display the greeting as a toast
                    Toast.makeText(TraderDashboardActivity.this, personalizedGreeting, Toast.LENGTH_SHORT).show();

                    // Update the header TextViews
                    if (firstName != null) {
                        userFirstName.setText(firstName);
                    }
                    if (phoneNumber != null) {
                        userPhoneNumber.setText(phoneNumber);
                    }
                } else {
                    Log.e(TAG, "User details not found");
                    Toast.makeText(TraderDashboardActivity.this, "User details not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                Toast.makeText(TraderDashboardActivity.this, "Error fetching user details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getPersonalizedGreeting(String[] greetings, String userName) {
        Random random = new Random();
        int index = random.nextInt(greetings.length); // Generate random index
        return String.format(greetings[index], userName); // Replace %s with user's name
    }

    private void fetchUserDetails(String userId) {
        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.exists()) {
                        state = dataSnapshot.child("state").getValue(String.class);
                        district = dataSnapshot.child("district").getValue(String.class);
                        taluka = dataSnapshot.child("taluka").getValue(String.class);
                        userRole = dataSnapshot.child("role").getValue(String.class); // Fetch the role

                        if (userRole == null) {
                            Log.e(TAG, "User role is null in the database");
                            Toast.makeText(TraderDashboardActivity.this, "User role not found", Toast.LENGTH_SHORT).show();
                        } else {
                            // Find the ImageView by ID
                            ImageView roleImageView = findViewById(R.id.userProfile);

                            if (userRole.equals("Farmer")) {
                                // Set image for Farmer
                                headerProfile.setImageResource(R.drawable.ic_farmer_profile);
                                roleImageView.setImageResource(R.drawable.ic_farmer_profile);

                            } else if (userRole.equals("Trader")) {
                                // Set image for Trader
                                headerProfile.setImageResource(R.drawable.ic_trader_profile);
                                roleImageView.setImageResource(R.drawable.ic_trader_profile);  // Replace with actual trader icon

                            }
                            Log.d(TAG, "User role fetched: " + userRole);
                        }
                    } else {
                        Toast.makeText(TraderDashboardActivity.this, "User details not found", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Data processing error: " + e.getMessage());
                    Toast.makeText(TraderDashboardActivity.this, "Error processing user details.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                Toast.makeText(TraderDashboardActivity.this, "Error fetching user details. Please check your connection.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // Handle each item in the navigation menu
        if (id == R.id.nav_home) {
            drawerLayout.closeDrawer(GravityCompat.START); // Close the drawer
        } else if (id == R.id.nav_profile) {
            drawerLayout.closeDrawer(GravityCompat.START);
            // Immediately launch the activity after closing the drawer
            closeDrawerAndNavigate(() -> navigateToProfileActivity());
        } else if (id == R.id.nav_contact_us) {
            drawerLayout.closeDrawer(GravityCompat.START);
            closeDrawerAndNavigate(() -> navigateToContactUsActivity());
        } else if (id == R.id.nav_language) {
            closeDrawerAndNavigate(() -> navigateToLanguageSelection());
        } else if (id == R.id.nav_logout) {
            drawerLayout.closeDrawer(GravityCompat.START);
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
        Intent intent = new Intent(TraderDashboardActivity.this, LanguageSelectionActivity.class);
        intent.putExtra("USER_ID", userId);
        intent.putExtra("USER_ROLE", userRole);
        intent.putExtra("Dashboard", "TraderDashboardActivity");
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out); // Smooth animation
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

    private void openListCropActivity() {
        Intent intent = new Intent(this, ListCropActivity.class);
        intent.putExtra("USER_ID", userId);
        intent.putExtra("STATE", state);
        intent.putExtra("DISTRICT", district);
        intent.putExtra("TALUKA", taluka);
        startActivity(intent);
    }

    private void openEditCropListingActivity() {
        Intent intent = new Intent(this, EditCropListingActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
    }

    private void navigateToProfileActivity() {
        closeOptionsMenu();
        if (userRole != null) {
            Intent intent;
            if ("farmer".equalsIgnoreCase(userRole.trim())) {
                intent = new Intent(TraderDashboardActivity.this, FarmerProfileActivity.class);
            } else if ("trader".equalsIgnoreCase(userRole.trim())) {
                intent = new Intent(TraderDashboardActivity.this, TraderProfileActivity.class);
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
        Intent intent = new Intent(TraderDashboardActivity.this, ContactUsActivity.class);
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
        Intent loginIntent = new Intent(TraderDashboardActivity.this, LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
        startActivity(loginIntent);

        // Finish the activity
        finish();
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

    private void showUpdateDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Update Required")
                .setMessage("A new version of the app is available. Please update to continue.")
                .setCancelable(false)
                .setPositiveButton("Update", (dialog, which) -> openPlayStore())
                .show();
    }

    private void openPlayStore() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open Play Store.", Toast.LENGTH_SHORT).show();
        }
    }
}
