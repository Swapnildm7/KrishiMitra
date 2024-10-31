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

public class FarmerProfileActivity extends AppCompatActivity {

    private static final String TAG = "FarmerProfileActivity";

    private EditText firstNameEditText, lastNameEditText, phoneNumberEditText, addressEditText;
    private Spinner stateSpinner, districtSpinner, talukaSpinner;
    private Button updateButton, deleteButton;
    private TextView currentStateTextView, currentDistrictTextView, currentTalukaTextView, privacyPolicyTextView;
    private DatabaseReference databaseReference;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_profile);

        // Transparent status bar setup
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        initUI();
        initFirebase();
        fetchUserDetails();
        setupListeners();
    }

    private void initUI() {
        firstNameEditText = findViewById(R.id.edit_first_name);
        lastNameEditText = findViewById(R.id.edit_last_name);
        phoneNumberEditText = findViewById(R.id.edit_phone_number);
        addressEditText = findViewById(R.id.edit_address);
        updateButton = findViewById(R.id.button_save);
        deleteButton = findViewById(R.id.button_delete_account);
        currentStateTextView = findViewById(R.id.current_state);
        currentDistrictTextView = findViewById(R.id.current_district);
        currentTalukaTextView = findViewById(R.id.current_taluka);
        privacyPolicyTextView = findViewById(R.id.tv_privacy_policy);
    }

    private void initFirebase() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        userId = getIntent().getStringExtra("USER_ID");
        if (userId == null) {
            Toast.makeText(this, "User ID not found.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "User ID is null.");
            finish();
        }
    }

    private void fetchUserDetails() {
        if (userId == null) return;

        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    setProfileData(dataSnapshot);
                } else {
                    Toast.makeText(FarmerProfileActivity.this, "User details not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showError("Error fetching user details", databaseError);
            }
        });
    }

    private void setProfileData(DataSnapshot dataSnapshot) {
        try {
            firstNameEditText.setText(dataSnapshot.child("firstName").getValue(String.class));
            lastNameEditText.setText(dataSnapshot.child("lastName").getValue(String.class));
            phoneNumberEditText.setText(dataSnapshot.child("phoneNumber").getValue(String.class));
            addressEditText.setText(dataSnapshot.child("address").getValue(String.class));

            currentStateTextView.setText(getDataOrPlaceholder(dataSnapshot, "state", "[Current State]"));
            currentDistrictTextView.setText(getDataOrPlaceholder(dataSnapshot, "district", "[Current District]"));
            currentTalukaTextView.setText(getDataOrPlaceholder(dataSnapshot, "taluka", "[Current Taluka]"));
        } catch (Exception e) {
            Log.e(TAG, "Error parsing user details", e);
            Toast.makeText(this, "Error loading user details.", Toast.LENGTH_SHORT).show();
        }
    }

    private String getDataOrPlaceholder(DataSnapshot snapshot, String key, String placeholder) {
        String value = snapshot.child(key).getValue(String.class);
        return value != null ? value : placeholder;
    }

    private void setupListeners() {
        updateButton.setOnClickListener(v -> updateUserDetails());
        deleteButton.setOnClickListener(v -> confirmDeleteAccount());
        privacyPolicyTextView.setOnClickListener(v -> openPrivacyPolicy());
    }

    private void updateUserDetails() {
        String firstName = firstNameEditText.getText().toString().trim();
        String phoneNumber = phoneNumberEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();

        // Check if required fields are filled
        if (firstName.isEmpty()) {
            Toast.makeText(this, "First name is required.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "Phone number is required.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (address.isEmpty()) {
            Toast.makeText(this, "Address is required.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("firstName", firstName);
        updates.put("lastName", lastNameEditText.getText().toString());
        updates.put("phoneNumber", phoneNumber);
        updates.put("address", address);

        if (userId == null) return;

        databaseReference.child(userId).updateChildren(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "User details updated successfully.", Toast.LENGTH_SHORT).show();
                        fetchUserDetails();
                    } else {
                        Toast.makeText(this, "Failed to update user details.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating user details", e);
                    Toast.makeText(this, "Error updating user details.", Toast.LENGTH_SHORT).show();
                });
    }

    private void confirmDeleteAccount() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account?")
                .setPositiveButton("Yes", (dialog, which) -> deleteAccount())
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteAccount() {
        if (userId == null) return;

        DatabaseReference userRef = databaseReference.child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    moveUserToPastUsers(dataSnapshot, userRef);
                } else {
                    Toast.makeText(FarmerProfileActivity.this, "User not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showError("Error deleting account", databaseError);
            }
        });
    }

    private void moveUserToPastUsers(DataSnapshot dataSnapshot, DatabaseReference userRef) {
        Map<String, Object> pastUserData = new HashMap<>((Map<String, Object>) dataSnapshot.getValue());
        clearSensitiveData(pastUserData);

        DatabaseReference pastUsersRef = FirebaseDatabase.getInstance().getReference("pastUsers").child(userId);
        pastUsersRef.setValue(pastUserData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        deleteUserFromCurrentUsers(userRef);
                    } else {
                        Toast.makeText(this, "Failed to move account details.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void clearSensitiveData(Map<String, Object> data) {
        data.put("firstName", "");
        data.put("lastName", "");
        data.put("phoneNumber", "");
        data.put("address", "");
    }

    private void deleteUserFromCurrentUsers(DatabaseReference userRef) {
        userRef.removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        clearSharedPreferencesAndRedirect();
                    } else {
                        Toast.makeText(this, "Failed to delete account.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void clearSharedPreferencesAndRedirect() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();

        Intent intent = new Intent(this, SignupActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void openPrivacyPolicy() {
        String privacyPolicyUrl = "https://smartgrains.org/PrivacyPolicyKrishiMitra.html";
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl)));
    }

    private void showError(String message, DatabaseError error) {
        Log.e(TAG, message + ": " + error.getMessage(), error.toException());
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
