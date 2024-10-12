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
        firstNameEditText = findViewById(R.id.edit_first_name); // Make this blank
        lastNameEditText = findViewById(R.id.edit_last_name); // Make this blank
        phoneNumberEditText = findViewById(R.id.edit_phone_number); // Make this blank
        addressEditText = findViewById(R.id.edit_address); // Address EditText for Farmer
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
                    addressEditText.setText(dataSnapshot.child("address").getValue(String.class));

                    // Fetch state, district, and taluka from the data
                    String state = dataSnapshot.child("state").getValue(String.class);
                    String district = dataSnapshot.child("district").getValue(String.class);
                    String taluka = dataSnapshot.child("taluka").getValue(String.class);

                    // Display the current state, district, and taluka in TextViews
                    currentStateTextView.setText(state != null ? state : "[Current State]");
                    currentDistrictTextView.setText(district != null ? district : "[Current District]");
                    currentTalukaTextView.setText(taluka != null ? taluka : "[Current Taluka]");

                    // Set the selected state in the spinner
                    stateSpinner.setSelection(((ArrayAdapter<String>) stateSpinner.getAdapter()).getPosition(state));

                    // Populate districts based on selected state
                    populateDistrictSpinner(state);

                    // Set the selected district in the spinner
                    districtSpinner.setSelection(((ArrayAdapter<String>) districtSpinner.getAdapter()).getPosition(district));

                    // Populate talukas based on selected district
                    List<String> talukas = LocationData.getTalukas(district);
                    CustomSpinnerAdapter talukaAdapter = new CustomSpinnerAdapter(FarmerProfileActivity.this, talukas.toArray(new String[0]));
                    talukaSpinner.setAdapter(talukaAdapter);

                    // Set the selected taluka in the spinner
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
        districtSpinner.setSelection(0); // Reset district selection
        talukaSpinner.setAdapter(null); // Reset taluka spinner
    }

    private void updateUserDetails() {
        // Keep firstName, lastName, phoneNumber, and address blank as per your request
        String firstName = firstNameEditText.getText().toString();
        String lastName = lastNameEditText.getText().toString();
        String phoneNumber = phoneNumberEditText.getText().toString();
        String address = addressEditText.getText().toString();

        // Get selected values from spinners
        String selectedState = stateSpinner.getSelectedItem().toString();
        String selectedDistrict = districtSpinner.getSelectedItem().toString();
        String selectedTaluka = talukaSpinner.getSelectedItem().toString();

        // Create a map to update user details
        Map<String, Object> updates = new HashMap<>();
        updates.put("firstName", firstName);
        updates.put("lastName", lastName);
        updates.put("phoneNumber", phoneNumber);
        updates.put("address", address);
        updates.put("state", selectedState);
        updates.put("district", selectedDistrict);
        updates.put("taluka", selectedTaluka);

        // Update in the database
        databaseReference.child(userId).updateChildren(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(FarmerProfileActivity.this, "User details updated successfully", Toast.LENGTH_SHORT).show();
                        // Fetch updated details from the database to refresh UI
                        fetchUserDetails(); // This fetches the latest user details
                    } else {
                        Toast.makeText(FarmerProfileActivity.this, "Failed to update user details", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteAccount() {
        // Confirmation dialog before deletion
        new AlertDialog.Builder(this)
                .setTitle("Clear Account Details")
                .setMessage("Are you sure you want to delete your account?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Reference to the user's current data in the 'Users' node
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

                    // Fetch the user's current details
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // Get the user data from the 'Users' node
                                Map<String, Object> userData = (Map<String, Object>) dataSnapshot.getValue();

                                if (userData != null) {
                                    // Modify the personal fields to blank, retain location fields
                                    userData.put("firstName", "");
                                    userData.put("lastName", "");
                                    userData.put("phoneNumber", "");
                                    userData.put("address", "");
                                    // Retaining state, district, and taluka as is, no need to modify these

                                    // Move the updated data to the 'pastUsers' node
                                    DatabaseReference pastUsersRef = FirebaseDatabase.getInstance().getReference("pastUsers");
                                    pastUsersRef.child(userId).setValue(userData)
                                            .addOnCompleteListener(task -> {
                                                if (task.isSuccessful()) {
                                                    // If data is moved successfully, update the 'Users' node with blank details
                                                    Map<String, Object> updates = new HashMap<>();
                                                    updates.put("firstName", "");
                                                    updates.put("lastName", "");
                                                    updates.put("phoneNumber", "");
                                                    updates.put("address", "");

                                                    // Update the user details in the 'Users' node to blank (personal fields only)
                                                    userRef.updateChildren(updates)
                                                            .addOnCompleteListener(updateTask -> {
                                                                if (updateTask.isSuccessful()) {
                                                                    Toast.makeText(FarmerProfileActivity.this, "Account deleted successfully.", Toast.LENGTH_SHORT).show();

                                                                    // Clear Shared Preferences
                                                                    SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                                                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                                                    editor.clear(); // Clear all saved data
                                                                    editor.apply();

                                                                    // Redirect to Signup page
                                                                    Intent intent = new Intent(FarmerProfileActivity.this, SignupPage.class);
                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear the activity stack
                                                                    startActivity(intent); // Start the SignupActivity
                                                                } else {
                                                                    Toast.makeText(FarmerProfileActivity.this, "Failed to clear account details.", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                } else {
                                                    Toast.makeText(FarmerProfileActivity.this, "Failed to move account to past users.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            } else {
                                Toast.makeText(FarmerProfileActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
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
