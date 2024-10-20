package org.smartgrains.krishimitra;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
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
import java.util.List;
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

        // Fetch user ID from intent
        userId = getIntent().getStringExtra("USER_ID");

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
                    firstNameEditText.setText(dataSnapshot.child("firstName").getValue(String.class));
                    lastNameEditText.setText(dataSnapshot.child("lastName").getValue(String.class));
                    phoneNumberEditText.setText(dataSnapshot.child("phoneNumber").getValue(String.class));
                    shopNameEditText.setText(dataSnapshot.child("shopName").getValue(String.class));
                    shopAddressEditText.setText(dataSnapshot.child("shopAddress").getValue(String.class));

                    // Fetch state, district, and taluka from the data
                    String state = dataSnapshot.child("state").getValue(String.class);
                    String district = dataSnapshot.child("district").getValue(String.class);
                    String taluka = dataSnapshot.child("taluka").getValue(String.class);

                    // Display the current state, district, and taluka in TextViews
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
            }
        });
    }

    private void updateUserDetails() {
        String firstName = firstNameEditText.getText().toString();
        String lastName = lastNameEditText.getText().toString();
        String phoneNumber = phoneNumberEditText.getText().toString();
        String shopName = shopNameEditText.getText().toString();
        String shopAddress = shopAddressEditText.getText().toString();

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
                        // Fetch updated details from the database to refresh UI
                        fetchUserDetails(); // This fetches the latest user details
                    } else {
                        Toast.makeText(TraderProfileActivity.this, "Failed to update user details", Toast.LENGTH_SHORT).show();
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
                                    // Modify the personal details to be blank
                                    userData.put("firstName", "");
                                    userData.put("lastName", "");
                                    userData.put("phoneNumber", "");
                                    userData.put("shopName", "");
                                    userData.put("shopAddress", "");

                                    // Move the modified data to the 'pastUsers' node
                                    DatabaseReference pastUsersRef = FirebaseDatabase.getInstance().getReference("pastUsers");
                                    pastUsersRef.child(userId).setValue(userData)
                                            .addOnCompleteListener(task -> {
                                                if (task.isSuccessful()) {
                                                    // If data is successfully moved, delete the user data from 'Users'
                                                    userRef.removeValue()
                                                            .addOnCompleteListener(deleteTask -> {
                                                                if (deleteTask.isSuccessful()) {
                                                                    Toast.makeText(TraderProfileActivity.this, "Account deleted Successfully", Toast.LENGTH_SHORT).show();

                                                                    // Clear Shared Preferences
                                                                    SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                                                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                                                    editor.clear(); // Clear all saved data
                                                                    editor.apply();

                                                                    // Redirect to Signup page
                                                                    Intent intent = new Intent(TraderProfileActivity.this, SignupPage.class);
                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear the activity stack
                                                                    startActivity(intent); // Start the SignupActivity

                                                                } else {
                                                                    // Handle failure in deleting user data from 'Users'
                                                                    Toast.makeText(TraderProfileActivity.this, "Failed to delete Account", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                } else {
                                                    // Handle failure in moving data to 'pastUsers'
                                                    Toast.makeText(TraderProfileActivity.this, "Failed to move data to past users", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            } else {
                                // Handle case where user data does not exist
                                Toast.makeText(TraderProfileActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle database error
                            Toast.makeText(TraderProfileActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }
}
