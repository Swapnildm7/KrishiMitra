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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ContactUsActivity extends AppCompatActivity {

    private static final String TAG = "ContactUsActivity";
    private Button deleteButton;
    private DatabaseReference databaseReference;
    private String userId;
    private String userRole; // Added variable to store the user role

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        // Make status bar transparent
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        deleteButton = findViewById(R.id.button_delete_account);

        // Get user role and userId from the intent
        Intent intent = getIntent();
        userRole = intent.getStringExtra("USER_ROLE"); // Ensure the correct key is used
        userId = intent.getStringExtra("USER_ID");

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Set up delete account button click listener
        deleteButton.setOnClickListener(v -> {
            if ("Farmer".equalsIgnoreCase(userRole)) {
                confirmDeleteAccountForFarmer();
            } else if ("Trader".equalsIgnoreCase(userRole)) {
                confirmDeleteAccountForTrader();
            } else {
                Toast.makeText(this, "Invalid user role.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Farmer-specific deletion
    private void confirmDeleteAccountForFarmer() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account?")
                .setPositiveButton("Yes", (dialog, which) -> deleteAccountForFarmer())
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteAccountForFarmer() {
        if (userId == null) return;

        DatabaseReference userRef = databaseReference.child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    moveUserToPastUsers(dataSnapshot, userRef);
                } else {
                    Toast.makeText(ContactUsActivity.this, "User not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showError("Error deleting account", databaseError);
            }
        });
    }

    // Trader-specific deletion
    private void confirmDeleteAccountForTrader() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account?")
                .setPositiveButton("Yes", (dialog, which) -> deleteAccountForTrader())
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteAccountForTrader() {
        if (userId == null) return;

        DatabaseReference userRef = databaseReference.child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String, Object> userData = (Map<String, Object>) dataSnapshot.getValue();
                    if (userData != null) {
                        clearTraderSensitiveData(userData);

                        DatabaseReference pastUsersRef = FirebaseDatabase.getInstance().getReference("pastUsers").child(userId);
                        pastUsersRef.setValue(userData)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        deleteUserFromCurrentUsers(userRef);
                                    } else {
                                        Toast.makeText(ContactUsActivity.this, "Failed to move account details.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                } else {
                    Toast.makeText(ContactUsActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showError("Error deleting account", databaseError);
            }
        });
    }

    // Move user data for Farmer
    private void moveUserToPastUsers(DataSnapshot dataSnapshot, DatabaseReference userRef) {
        Map<String, Object> pastUserData = new HashMap<>((Map<String, Object>) dataSnapshot.getValue());
        clearFarmerSensitiveData(pastUserData);

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

    private void clearFarmerSensitiveData(Map<String, Object> data) {
        data.put("firstName", "");
        data.put("lastName", "");
        data.put("phoneNumber", "");
        data.put("address", "");
    }

    private void clearTraderSensitiveData(Map<String, Object> data) {
        data.put("firstName", "");
        data.put("lastName", "");
        data.put("phoneNumber", "");
        data.put("shopName", "");
        data.put("shopAddress", "");
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

    private void showError(String message, DatabaseError error) {
        Log.e(TAG, message + ": " + error.getMessage(), error.toException());
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
