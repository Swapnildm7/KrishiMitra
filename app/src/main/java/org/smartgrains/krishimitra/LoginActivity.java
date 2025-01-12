package org.smartgrains.krishimitra;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextPhoneNumber, editTextPassword;
    private Button buttonLogin;
    private TextView signUpText;
    private DatabaseReference userDatabaseReference;
    private DatabaseReference adminDatabaseReference; // Reference for Admins node
    private ProgressBar progressBar; // Progress bar to show loading state

    // SharedPreferences to store login state
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_ROLE = "USER_ROLE";
    private static final String KEY_USER_ID = "USER_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.setLocale(this);
        setContentView(R.layout.activity_login);

        // Make status bar transparent
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        // Initialize Firebase Database references
        userDatabaseReference = FirebaseDatabase.getInstance().getReference("Users");
        adminDatabaseReference = FirebaseDatabase.getInstance().getReference("Admins"); // Admins node reference

        // Initialize Views
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        signUpText = findViewById(R.id.sign_up_text);
        progressBar = findViewById(R.id.progressBar); // Initialize ProgressBar

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Set Click Listener for Login Button
        buttonLogin.setOnClickListener(v -> loginUser());

        // Set Click Listener for Sign Up Text
        signUpText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Validate input fields
        if (phoneNumber.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, getString(R.string.empty_fields_error), Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate phone number length (should not exceed 10 digits)
        if (phoneNumber.length() != 10) {
            Toast.makeText(LoginActivity.this, getString(R.string.invalid_phone_number_error), Toast.LENGTH_SHORT).show();
            return;
        }

        // Show the progress bar and disable the login button
        showProgressBar(true);

        // Check for admin credentials in the Admins node
        adminDatabaseReference.orderByChild("phoneNumber").equalTo(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    handleAdminLogin(dataSnapshot, password);
                } else {
                    // If not an admin, check user credentials in the Users node
                    checkUserCredentials(phoneNumber, password);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                handleDatabaseError(databaseError);
            }
        });
    }

    private void handleAdminLogin(DataSnapshot dataSnapshot, String password) {
        for (DataSnapshot adminSnapshot : dataSnapshot.getChildren()) {
            String storedPassword = adminSnapshot.child("password").getValue(String.class);
            String adminId = adminSnapshot.getKey(); // Get the Admin User ID (key)

            if (storedPassword != null && storedPassword.equals(hashPassword(password))) {
                // Save user role and ID in SharedPreferences
                saveLoginState("Admin", adminId);

                // Navigate to Admin Dashboard
                navigateToDashboard("Admin", adminId);
                return; // Exit after successful login
            } else {
                Toast.makeText(LoginActivity.this, getString(R.string.incorrect_admin_password), Toast.LENGTH_SHORT).show();
            }
        }
        // Hide the progress bar and re-enable the login button
        showProgressBar(false);
    }

    private void checkUserCredentials(String phoneNumber, String password) {
        userDatabaseReference.orderByChild("phoneNumber").equalTo(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String storedPassword = userSnapshot.child("password").getValue(String.class);
                        String role = userSnapshot.child("role").getValue(String.class);
                        String userId = userSnapshot.getKey(); // Get the User ID (key)

                        if (storedPassword != null && storedPassword.equals(hashPassword(password))) {
                            // Save user role and ID in SharedPreferences
                            saveLoginState(role, userId);

                            // Update the last login timestamp
                            recordLastLogin(userId);

                            // Navigate to User Dashboard
                            navigateToDashboard(role, userId);
                            return; // Exit after successful login
                        } else {
                            Toast.makeText(LoginActivity.this, getString(R.string.incorrect_password), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(LoginActivity.this, getString(R.string.user_not_found), Toast.LENGTH_SHORT).show();
                }
                // Hide the progress bar and re-enable the login button
                showProgressBar(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                handleDatabaseError(databaseError);
            }
        });
    }

    private void handleDatabaseError(DatabaseError databaseError) {
        Log.e("LoginActivity", "Database error: " + databaseError.getMessage());
        Toast.makeText(LoginActivity.this, getString(R.string.database_error), Toast.LENGTH_SHORT).show();
        // Hide the progress bar and re-enable the login button
        showProgressBar(false);
    }

    private void showProgressBar(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            buttonLogin.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            buttonLogin.setEnabled(true);
        }
    }

    private void saveLoginState(String role, String userId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_ROLE, role);
        editor.putString(KEY_USER_ID, userId);
        editor.apply();
    }

    private void navigateToDashboard(String role, String userId) {
        Intent intent;
        if ("Trader".equals(role)) {
            intent = new Intent(LoginActivity.this, TraderDashboardActivity.class);
        } else if ("Farmer".equals(role)) {
            intent = new Intent(LoginActivity.this, FarmerDashboardActivity.class);
        } else if ("Admin".equals(role)) {
            intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
        } else {
            Toast.makeText(LoginActivity.this, getString(R.string.invalid_role), Toast.LENGTH_SHORT).show();
            return;
        }

        // Pass the User ID to the dashboard activity
        intent.putExtra("USER_ID", userId);

        // Clear the back stack to prevent returning to login page
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish(); // Close the login page
    }

    // Method to hash the password
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString(); // Return the hashed password as a hexadecimal string
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // Method to record last login time
    private void recordLastLogin(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        // Get current time in milliseconds
        long currentTimeMillis = System.currentTimeMillis();

        // Convert to readable format in IST
        String lastLoginTime = convertToIST(currentTimeMillis);

        // Update the last login time in human-readable format
        Map<String, Object> updates = new HashMap<>();
        updates.put("lastLogin", lastLoginTime); // Store the formatted date-time string
        userRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("LoginPage", "Last login time recorded successfully.");
            } else {
                Log.e("LoginPage", "Failed to record last login time.");
            }
        });
    }

    // Helper method to convert timestamp to IST
    private String convertToIST(long timestamp) {
        // Create a calendar instance and set the time
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        return sdf.format(new Date());
    }
}
