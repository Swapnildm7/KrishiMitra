package org.smartgrains.krishimitra;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class TraderProfileActivity extends AppCompatActivity {
    private EditText firstNameEditText, lastNameEditText, phoneNumberEditText, shopNameEditText, shopAddressEditText;
    private Spinner stateSpinner, districtSpinner, talukaSpinner;
    private Button updateButton, deleteButton;
    private TextView currentStateTextView, currentDistrictTextView, currentTalukaTextView, privacyPolicyTextView; // TextViews to show current location
    private DatabaseReference databaseReference;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Make status bar transparent
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        userId = getIntent().getStringExtra("USER_ID");
        if (userId == null) {
            Toast.makeText(this, "User ID is missing", Toast.LENGTH_SHORT).show();
            finish(); // Exit the activity if userId is not found
            return;
        }

        // Initialize UI elements
        firstNameEditText = findViewById(R.id.edit_first_name);
        lastNameEditText = findViewById(R.id.edit_last_name);
        phoneNumberEditText = findViewById(R.id.edit_phone_number);
        shopNameEditText = findViewById(R.id.edit_shop_name);
        shopAddressEditText = findViewById(R.id.edit_shop_address);
        updateButton = findViewById(R.id.button_save);
        deleteButton = findViewById(R.id.button_delete_account);
        currentStateTextView = findViewById(R.id.current_state); // Initialize TextViews
        currentDistrictTextView = findViewById(R.id.current_district);
        currentTalukaTextView = findViewById(R.id.current_taluka);
        privacyPolicyTextView = findViewById(R.id.tv_privacy_policy);

        // Fetch and display user details
        fetchUserDetails();

        // Set up update button listener
        updateButton.setOnClickListener(v -> updateUserDetails());

        // Set up delete button listener
        deleteButton.setOnClickListener(v -> deleteAccount());

        // Set OnClickListener to open the privacy policy link
        privacyPolicyTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Define the privacy policy URL
                String privacyPolicyUrl = "https://smartgrains.org/PrivacyPolicyKrishiMitra.html";

                // Create an Intent to open the URL in a browser
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl));
                startActivity(intent);
            }
        });
    }
    private void fetchUserDetails() {
        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Safely fetch user data
                    String firstName = dataSnapshot.child("firstName").getValue(String.class);
                    String lastName = dataSnapshot.child("lastName").getValue(String.class);
                    String phoneNumber = dataSnapshot.child("phoneNumber").getValue(String.class);
                    String shopName = dataSnapshot.child("shopName").getValue(String.class);
                    String shopAddress = dataSnapshot.child("shopAddress").getValue(String.class);
                    String state = dataSnapshot.child("state").getValue(String.class);
                    String district = dataSnapshot.child("district").getValue(String.class);
                    String taluka = dataSnapshot.child("taluka").getValue(String.class);

                    // Set text to EditTexts and TextViews
                    firstNameEditText.setText(firstName != null ? firstName : "");
                    lastNameEditText.setText(lastName != null ? lastName : "");
                    phoneNumberEditText.setText(phoneNumber != null ? phoneNumber : "");
                    shopNameEditText.setText(shopName != null ? shopName : "");
                    shopAddressEditText.setText(shopAddress != null ? shopAddress : "");
                    currentStateTextView.setText(state != null ? state : "[Current State]");
                    currentDistrictTextView.setText(district != null ? district : "[Current District]");
                    currentTalukaTextView.setText(taluka != null ? taluka : "[Current Taluka]");
                } else {
                    Toast.makeText(TraderProfileActivity.this, "User details not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(TraderProfileActivity.this, "Error fetching user details", Toast.LENGTH_SHORT).show();
                Log.e("TraderProfileActivity", "Database error: " + databaseError.getMessage());
            }
        });
    }

    private void updateUserDetails() {
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String phoneNumber = phoneNumberEditText.getText().toString().trim();
        String shopName = shopNameEditText.getText().toString().trim();
        String shopAddress = shopAddressEditText.getText().toString().trim();

        // Validate input data
        if (firstName.isEmpty() || phoneNumber.isEmpty() || shopName.isEmpty() || shopAddress.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a map to update user details
        Map<String, Object> updates = new HashMap<>();
        updates.put("firstName", firstName);
        updates.put("lastName", lastName);
        updates.put("phoneNumber", phoneNumber);
        updates.put("shopName", shopName);
        updates.put("shopAddress", shopAddress);

        // Update in the database
        databaseReference.child(userId).updateChildren(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(TraderProfileActivity.this, "User details updated successfully", Toast.LENGTH_SHORT).show();
                        fetchUserDetails(); // Refresh UI with updated details
                    } else {
                        Toast.makeText(TraderProfileActivity.this, "Failed to update user details", Toast.LENGTH_SHORT).show();
                        Exception exception = task.getException();
                        String errorMessage = (exception != null) ? exception.getMessage() : "Unknown error occurred";
                        Log.e("TraderProfileActivity", "Update error: " + errorMessage);
                    }
                });
    }

    private void deleteAccount() {
        // Confirmation dialog before deletion
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Fetch current user data from the 'Users' node
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // Get user data as a Map
                                Map<String, Object> userData = (Map<String, Object>) dataSnapshot.getValue();

                                if (userData != null) {
                                    // Clear personal details
                                    userData.put("firstName", "");
                                    userData.put("lastName", "");
                                    userData.put("phoneNumber", "");
                                    userData.put("shopName", "");
                                    userData.put("shopAddress", "");

                                    // Move modified data to the 'pastUsers' node
                                    DatabaseReference pastUsersRef = FirebaseDatabase.getInstance().getReference("pastUsers");
                                    pastUsersRef.child(userId).setValue(userData)
                                            .addOnCompleteListener(task -> {
                                                if (task.isSuccessful()) {
                                                    // If data is successfully moved, delete the user data from 'Users'
                                                    userRef.removeValue()
                                                            .addOnCompleteListener(deleteTask -> {
                                                                if (deleteTask.isSuccessful()) {
                                                                    Toast.makeText(TraderProfileActivity.this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                                                    clearUserPreferences(); // Clear Shared Preferences
                                                                    redirectToSignup();
                                                                } else {
                                                                    Toast.makeText(TraderProfileActivity.this, "Failed to delete account", Toast.LENGTH_SHORT).show();
                                                                    Log.e("TraderProfileActivity", "Delete error: " + deleteTask.getException().getMessage());
                                                                }
                                                            });
                                                } else {
                                                    Toast.makeText(TraderProfileActivity.this, "Failed to move data to past users", Toast.LENGTH_SHORT).show();
                                                    Log.e("TraderProfileActivity", "Move to past users error: " + task.getException().getMessage());
                                                }
                                            });
                                }
                            } else {
                                Toast.makeText(TraderProfileActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(TraderProfileActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                            Log.e("TraderProfileActivity", "Database error: " + databaseError.getMessage());
                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void clearUserPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // Clear all saved data
        editor.apply();
    }

    private void redirectToSignup() {
        Intent intent = new Intent(TraderProfileActivity.this, SignupActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear the activity stack
        startActivity(intent); // Start the SignupActivity
    }
}
