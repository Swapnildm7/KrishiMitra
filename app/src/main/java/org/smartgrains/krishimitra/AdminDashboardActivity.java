package org.smartgrains.krishimitra;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView textViewWelcomeAdmin;
    private Button buttonUploadCrops, buttonLogout, buttonListCrops, buttonListPlaces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Make status bar transparent
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        // Initialize UI components
        textViewWelcomeAdmin = findViewById(R.id.textViewWelcomeAdmin);
        buttonUploadCrops = findViewById(R.id.buttonUploadCrops);
        buttonLogout = findViewById(R.id.buttonLogout);
        buttonListCrops = findViewById(R.id.buttonListCrops);
        buttonListPlaces = findViewById(R.id.buttonListPlaces);

        // Set click listener for "Upload Crops" button
        buttonUploadCrops.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to CropUploadActivity
                Intent intent = new Intent(AdminDashboardActivity.this, CropUploadActivity.class);
                startActivity(intent);
            }
        });

        buttonListCrops.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, CropLanguage.class);
            startActivity(intent);
        });

        // Set click listener for "Logout" button
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform logout
                logout();
            }
        });

        buttonListPlaces.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AddLocation.class);
            startActivity(intent);
        });
    }

    private void logout() {
        // Clear shared preferences or any stored user data here
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // Clear all stored preferences
        editor.apply(); // Apply changes

        // Navigate back to the Login Page
        Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Close the Admin Dashboard activity
    }
}
