package com.smartgrains.krishimitra;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class FarmerRegistrationActivity extends AppCompatActivity {

    private EditText editTextAddress;
    private Spinner stateSpinner, districtSpinner, talukaSpinner;
    private Button buttonRegister;

    private DatabaseReference databaseReference;
    private String firstName, lastName, phoneNumber, password;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_registration);

        // Make status bar transparent
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize Views
        editTextAddress = findViewById(R.id.editTextAddress);
        stateSpinner = findViewById(R.id.state_spinner);
        districtSpinner = findViewById(R.id.district_spinner);
        talukaSpinner = findViewById(R.id.taluka_spinner);
        buttonRegister = findViewById(R.id.buttonRegister);

        // Get Intent data
        Intent intent = getIntent();
        firstName = intent.getStringExtra("FIRST_NAME");
        lastName = intent.getStringExtra("LAST_NAME");
        phoneNumber = intent.getStringExtra("PHONE_NUMBER");
        password = intent.getStringExtra("PASSWORD"); // Get password from the previous activity

        // Populate Location Spinners
        populateLocationSpinners();

        // Register Button Click Listener
        buttonRegister.setOnClickListener(v -> {
            String address = editTextAddress.getText().toString().trim();
            String selectedState = stateSpinner.getSelectedItem().toString();
            String selectedDistrict = districtSpinner.getSelectedItem().toString();
            String selectedTaluka = talukaSpinner.getSelectedItem().toString();

            // Validate input fields
            if (address.isEmpty() || selectedState.isEmpty() || selectedDistrict.isEmpty() || selectedTaluka.isEmpty()) {
                Toast.makeText(FarmerRegistrationActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if user is already registered before saving
            checkIfUserRegistered(phoneNumber, firstName, lastName, password, address, selectedState, selectedDistrict, selectedTaluka);
        });
    }

    private void populateLocationSpinners() {
        List<String> states = LocationData.getStates();
        String[] statesArray = states.toArray(new String[0]); // Convert List to String[]
        CustomSpinnerAdapter stateAdapter = new CustomSpinnerAdapter(this, statesArray);
        stateSpinner.setAdapter(stateAdapter);

        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedState = states.get(position);
                populateDistrictSpinner(selectedState);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void populateDistrictSpinner(String state) {
        List<String> districts = LocationData.getDistricts(state);
        String[] districtsArray = districts.toArray(new String[0]); // Convert List to String[]
        CustomSpinnerAdapter districtAdapter = new CustomSpinnerAdapter(this, districtsArray);
        districtSpinner.setAdapter(districtAdapter);

        districtSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDistrict = districts.get(position);
                populateTalukaSpinner(selectedDistrict);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void populateTalukaSpinner(String district) {
        List<String> talukas = LocationData.getTalukas(district);
        String[] talukasArray = talukas.toArray(new String[0]); // Convert List to String[]
        CustomSpinnerAdapter talukaAdapter = new CustomSpinnerAdapter(this, talukasArray);
        talukaSpinner.setAdapter(talukaAdapter);
    }

    // Method to check if user is already registered by phone number
    private void checkIfUserRegistered(String phoneNumber, String firstName, String lastName, String password,
                                       String address, String state, String district, String taluka) {
        databaseReference.orderByChild("phoneNumber").equalTo(phoneNumber)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // User already registered
                            Toast.makeText(FarmerRegistrationActivity.this, "User already registered with this phone number", Toast.LENGTH_SHORT).show();
                        } else {
                            // User not registered, proceed with registration
                            saveUserInfo(firstName, lastName, phoneNumber, password, address, state, district, taluka);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("FirebaseError", databaseError.getMessage());
                        Toast.makeText(FarmerRegistrationActivity.this, "Error checking registration", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserInfo(String firstName, String lastName, String phoneNumber, String password,
                              String address, String state, String district, String taluka) {
        String userId = databaseReference.push().getKey();
        User user = new User(userId, firstName, lastName, phoneNumber, password, "Farmer", address, state, district, taluka);

        if (userId != null) {
            databaseReference.child(userId).setValue(user)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(FarmerRegistrationActivity.this, "Farmer registered successfully", Toast.LENGTH_SHORT).show();

                            // Navigate to TraderDashboardActivity
                            Intent intent = new Intent(FarmerRegistrationActivity.this, FarmerDashboardActivity.class);
                            intent.putExtra("USER_ID", userId); // Pass the user ID or any other needed data
                            startActivity(intent);
                            finish(); // Close the activity

                        } else {
                            Toast.makeText(FarmerRegistrationActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
