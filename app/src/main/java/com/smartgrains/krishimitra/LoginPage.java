package com.smartgrains.krishimitra;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class LoginPage extends AppCompatActivity {

    private EditText editTextPhoneNumber, editTextPassword;
    private Button buttonLogin;
    private TextView signUpText;

    private DatabaseReference databaseReference;

    // SharedPreferences to store login state
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_ROLE = "USER_ROLE";
    private static final String KEY_USER_ID = "USER_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        // Make status bar transparent
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize Views
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        signUpText = findViewById(R.id.sign_up_text);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Check if the user is already logged in
        checkLoginState();

        // Set Click Listener for Login Button
        buttonLogin.setOnClickListener(v -> loginUser());

        // Set Click Listener for Sign Up Text
        signUpText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginPage.this, SignupPage.class);
            startActivity(intent);
        });
    }

    private void checkLoginState() {
        String role = sharedPreferences.getString(KEY_ROLE, null);
        String userId = sharedPreferences.getString(KEY_USER_ID, null);

        if (role != null && userId != null) {
            // User is already logged in, navigate to the respective dashboard
            navigateToDashboard(role, userId);
        }
    }

    private void loginUser() {
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Validate input fields
        if (phoneNumber.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginPage.this, "Please enter phone number and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check user credentials in the database
        databaseReference.orderByChild("phoneNumber").equalTo(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String storedPassword = userSnapshot.child("password").getValue(String.class);
                        String role = userSnapshot.child("role").getValue(String.class);
                        String userId = userSnapshot.getKey(); // Get the User ID (key)

                        if (storedPassword != null && storedPassword.equals(hashPassword(password))) {
                            // Store the login state in SharedPreferences
                            saveLoginState(role, userId);

                            // Update the last login timestamp
                            recordLastLogin(userId);

                            // Navigate to the corresponding dashboard with User ID
                            navigateToDashboard(role, userId);
                        } else {
                            Toast.makeText(LoginPage.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(LoginPage.this, "User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(LoginPage.this, "Error checking credentials", Toast.LENGTH_SHORT).show();
            }
        });
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
            intent = new Intent(LoginPage.this, TraderDashboardActivity.class);
        } else if ("Farmer".equals(role)) {
            intent = new Intent(LoginPage.this, FarmerDashboardActivity.class);
        } else {
            Toast.makeText(LoginPage.this, "Invalid role", Toast.LENGTH_SHORT).show();
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

        // Create a SimpleDateFormat for the desired date format
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());

        // Set the time zone to IST (Indian Standard Time)
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));

        // Format the timestamp into a readable date string
        return sdf.format(calendar.getTime());
    }
}
