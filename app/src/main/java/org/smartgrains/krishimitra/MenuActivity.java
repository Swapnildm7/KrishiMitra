package org.smartgrains.krishimitra;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MenuActivity extends AppCompatActivity {

    private String userId; // User ID from intent
    private String userRole; // Store user role
    private static final String TAG = "MenuActivity"; // Tag for logging
    private static final String UNKNOWN_ROLE = "Unknown role"; // Define unknown role string

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

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

        // Initialize buttons for profile, home, contact us, and logout
        Button profileButton = findViewById(R.id.nav_profile); // Button for profile
        Button homeButton = findViewById(R.id.nav_home); // Button for home
        Button contactUsButton = findViewById(R.id.nav_contact_us); // Button for contact us
        Button logoutButton = findViewById(R.id.nav_logout); // Button for logout

        // Fetch user role from the database (consider using SharedPreferences cache)
        fetchUserRole();

        // Set click listeners for the buttons
        profileButton.setOnClickListener(v -> navigateToProfileActivity());
        homeButton.setOnClickListener(v -> navigateToDashboard());
        contactUsButton.setOnClickListener(v -> navigateToContactUsActivity()); // Add Contact Us listener
        logoutButton.setOnClickListener(v -> performLogout());
    }

    private void fetchUserRole() {
        // Fetch user role from the Firebase database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        databaseReference.child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    userRole = dataSnapshot.getValue(String.class);
                    Log.d(TAG, "User role fetched: " + userRole); // Log fetched role
                } else {
                    Log.d(TAG, "User role not found"); // Log if role not found
                    Toast.makeText(MenuActivity.this, "User role not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching user role: " + databaseError.getMessage()); // Log any errors
                Toast.makeText(MenuActivity.this, "Error fetching user role", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToProfileActivity() {
        if (userRole != null) {
            Intent intent;
            if ("farmer".equalsIgnoreCase(userRole.trim())) {
                intent = new Intent(MenuActivity.this, FarmerProfileActivity.class);
            } else if ("trader".equalsIgnoreCase(userRole.trim())) {
                intent = new Intent(MenuActivity.this, TraderProfileActivity.class);
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
    }

    private void navigateToDashboard() {
        if (userRole != null) {
            Intent intent;
            if ("farmer".equalsIgnoreCase(userRole.trim())) {
                intent = new Intent(MenuActivity.this, FarmerDashboardActivity.class);
            } else if ("trader".equalsIgnoreCase(userRole.trim())) {
                intent = new Intent(MenuActivity.this, TraderDashboardActivity.class);
            } else {
                Log.d(TAG, UNKNOWN_ROLE + ": " + userRole); // Log unknown role
                Toast.makeText(this, UNKNOWN_ROLE + ": " + userRole, Toast.LENGTH_SHORT).show();
                return; // Exit if role is unknown
            }
            intent.putExtra("USER_ID", userId); // Pass the user ID if needed
            startActivity(intent);
        } else {
            Log.d(TAG, "User role not yet fetched. Please wait."); // Log message if role is not available
            Toast.makeText(this, "User role not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToContactUsActivity() {
        // Navigate to the ContactUsActivity
        Intent intent = new Intent(MenuActivity.this, ContactUsActivity.class);
        startActivity(intent);
    }

    private void performLogout() {
        // Clear Firebase session (if using Firebase authentication)
        // mAuth.signOut(); // Uncomment if using Firebase authentication

        // Clear SharedPreferences for user session
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // Clear all saved data
        editor.apply();

        // Redirect to Login Page
        Intent loginIntent = new Intent(MenuActivity.this, LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
        startActivity(loginIntent);

        // Finish the activity
        finish();
    }
}
