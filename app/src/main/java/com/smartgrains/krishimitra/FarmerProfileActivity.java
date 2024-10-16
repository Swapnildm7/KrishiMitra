package com.smartgrains.krishimitra;

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

public class FarmerProfileActivity extends AppCompatActivity {

    private EditText firstNameEditText, lastNameEditText, phoneNumberEditText, addressEditText;
    private Spinner stateSpinner, districtSpinner, talukaSpinner;
    private Button updateButton, deleteButton;
    private TextView currentStateTextView, currentDistrictTextView, currentTalukaTextView, privacyPolicyTextView;
    private DatabaseReference databaseReference;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_profile); // Update layout name as needed

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
        addressEditText = findViewById(R.id.edit_address);
        stateSpinner = findViewById(R.id.spinner_state);
        districtSpinner = findViewById(R.id.spinner_district);
        talukaSpinner = findViewById(R.id.spinner_taluka);
        updateButton = findViewById(R.id.button_save);
        deleteButton = findViewById(R.id.button_delete_account);
        currentStateTextView = findViewById(R.id.current_state);
        currentDistrictTextView = findViewById(R.id.current_district);
        currentTalukaTextView = findViewById(R.id.current_taluka);
        privacyPolicyTextView = findViewById(R.id.tv_privacy_policy);

        // Populate State Spinner
        List<String> states = LocationData.getStates();
        String[] statesArray = states.toArray(new String[0]);
        CustomSpinnerAdapter stateAdapter = new CustomSpinnerAdapter(this, statesArray);
        stateSpinner.setAdapter(stateAdapter);

        // Set up spinner listeners
        stateSpinner.setOnItemSelectedListener(new StateSelectionListener());
        districtSpinner.setOnItemSelectedListener(new DistrictSelectionListener());

        // Fetch and display user details
        fetchUserDetails();

        // Set up update button listener
        updateButton.setOnClickListener(v -> updateUserDetails());

        // Set up delete button listener
        deleteButton.setOnClickListener(v -> deleteAccount());

        privacyPolicyTextView.setOnClickListener(v -> {
            String privacyPolicyUrl = "https://smartgrains.org/PrivacyPolicyKrishiMitra.html";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl));
            startActivity(intent);
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
                    addressEditText.setText(dataSnapshot.child("address").getValue(String.class));

                    String state = dataSnapshot.child("state").getValue(String.class);
                    String district = dataSnapshot.child("district").getValue(String.class);
                    String taluka = dataSnapshot.child("taluka").getValue(String.class);

                    currentStateTextView.setText(state != null ? state : "[Current State]");
                    currentDistrictTextView.setText(district != null ? district : "[Current District]");
                    currentTalukaTextView.setText(taluka != null ? taluka : "[Current Taluka]");

                    stateSpinner.setSelection(((ArrayAdapter<String>) stateSpinner.getAdapter()).getPosition(state));
                    populateDistrictSpinner(state);
                    districtSpinner.setSelection(((ArrayAdapter<String>) districtSpinner.getAdapter()).getPosition(district));

                    List<String> talukas = LocationData.getTalukas(district);
                    CustomSpinnerAdapter talukaAdapter = new CustomSpinnerAdapter(FarmerProfileActivity.this, talukas.toArray(new String[0]));
                    talukaSpinner.setAdapter(talukaAdapter);
                    talukaSpinner.setSelection(talukaAdapter.getPosition(taluka));
                } else {
                    Toast.makeText(FarmerProfileActivity.this, "User details not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(FarmerProfileActivity.this, "Error fetching user details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateDistrictSpinner(String state) {
        List<String> districts = LocationData.getDistricts(state);
        CustomSpinnerAdapter districtAdapter = new CustomSpinnerAdapter(this, districts.toArray(new String[0]));
        districtSpinner.setAdapter(districtAdapter);
        districtSpinner.setSelection(0);
        talukaSpinner.setAdapter(null);
    }

    private void updateUserDetails() {
        String firstName = firstNameEditText.getText().toString();
        String lastName = lastNameEditText.getText().toString();
        String phoneNumber = phoneNumberEditText.getText().toString();
        String address = addressEditText.getText().toString();

        String selectedState = stateSpinner.getSelectedItem().toString();
        String selectedDistrict = districtSpinner.getSelectedItem().toString();
        String selectedTaluka = talukaSpinner.getSelectedItem().toString();

        Map<String, Object> updates = new HashMap<>();
        updates.put("firstName", firstName);
        updates.put("lastName", lastName);
        updates.put("phoneNumber", phoneNumber);
        updates.put("address", address);
        updates.put("state", selectedState);
        updates.put("district", selectedDistrict);
        updates.put("taluka", selectedTaluka);

        databaseReference.child(userId).updateChildren(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(FarmerProfileActivity.this, "User details updated successfully", Toast.LENGTH_SHORT).show();
                        fetchUserDetails();
                    } else {
                        Toast.makeText(FarmerProfileActivity.this, "Failed to update user details", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteAccount() {
        new AlertDialog.Builder(this)
                .setTitle("Clear Account Details")
                .setMessage("Are you sure you want to delete your account?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Map<String, Object> userData = (Map<String, Object>) dataSnapshot.getValue();

                                if (userData != null) {
                                    // Prepare data for pastUsers node
                                    Map<String, Object> pastUserData = new HashMap<>(userData);
                                    // Set personal details to blank
                                    pastUserData.put("firstName", "");
                                    pastUserData.put("lastName", "");
                                    pastUserData.put("phoneNumber", "");
                                    pastUserData.put("address", "");

                                    // Move data to the pastUsers node
                                    DatabaseReference pastUsersRef = FirebaseDatabase.getInstance().getReference("pastUsers");
                                    pastUsersRef.child(userId).setValue(pastUserData)
                                            .addOnCompleteListener(task -> {
                                                if (task.isSuccessful()) {
                                                    // If moved successfully, now delete from Users node
                                                    userRef.removeValue().addOnCompleteListener(removeTask -> {
                                                        if (removeTask.isSuccessful()) {
                                                            Toast.makeText(FarmerProfileActivity.this, "Account deleted successfully.", Toast.LENGTH_SHORT).show();

                                                            // Clear Shared Preferences
                                                            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                                            editor.clear();
                                                            editor.apply();

                                                            // Redirect to Signup page
                                                            Intent intent = new Intent(FarmerProfileActivity.this, SignupPage.class);
                                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                            startActivity(intent);
                                                        } else {
                                                            Toast.makeText(FarmerProfileActivity.this, "Failed to delete account.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                } else {
                                                    Toast.makeText(FarmerProfileActivity.this, "Failed to move account details.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                } else {
                                    Toast.makeText(FarmerProfileActivity.this, "No user data found.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(FarmerProfileActivity.this, "User does not exist.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(FarmerProfileActivity.this, "Error fetching user data.", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }

    // Spinner listener for State selection
    private class StateSelectionListener implements android.widget.AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
            String selectedState = (String) parent.getItemAtPosition(position);
            populateDistrictSpinner(selectedState);
        }

        @Override
        public void onNothingSelected(android.widget.AdapterView<?> parent) {
            // Do nothing
        }
    }

    // Spinner listener for District selection
    private class DistrictSelectionListener implements android.widget.AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
            String selectedDistrict = (String) parent.getItemAtPosition(position);
            List<String> talukas = LocationData.getTalukas(selectedDistrict);
            CustomSpinnerAdapter talukaAdapter = new CustomSpinnerAdapter(FarmerProfileActivity.this, talukas.toArray(new String[0]));
            talukaSpinner.setAdapter(talukaAdapter);
        }

        @Override
        public void onNothingSelected(android.widget.AdapterView<?> parent) {
            // Do nothing
        }
    }
}