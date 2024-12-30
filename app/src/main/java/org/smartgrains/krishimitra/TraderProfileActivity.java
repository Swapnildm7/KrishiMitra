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
    private Button updateButton;
    private TextView currentStateTextView, currentDistrictTextView, currentTalukaTextView; // TextViews to show current location
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
        currentStateTextView = findViewById(R.id.current_state); // Initialize TextViews
        currentDistrictTextView = findViewById(R.id.current_district);
        currentTalukaTextView = findViewById(R.id.current_taluka);

        // Fetch and display user details
        fetchUserDetails();

        // Set up update button listener
        updateButton.setOnClickListener(v -> updateUserDetails());

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
}
