package org.smartgrains.krishimitra;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class TraderRegistrationActivity extends AppCompatActivity {

    private EditText editTextShopName, editTextShopAddress;
    private Spinner stateSpinner, districtSpinner, talukaSpinner;
    private Button buttonRegister;

    private DatabaseReference databaseReference;
    private String firstName, lastName, phoneNumber, password;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trader_registration);

        // Make status bar transparent
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        CheckBox privacyPolicyCheckbox = findViewById(R.id.privacy_policy_checkbox);
        privacyPolicyCheckbox.setMovementMethod(LinkMovementMethod.getInstance());

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize Views
        editTextShopName = findViewById(R.id.editTextShopName);
        editTextShopAddress = findViewById(R.id.editTextShopAddress);
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
            String shopName = editTextShopName.getText().toString().trim();
            String shopAddress = editTextShopAddress.getText().toString().trim();
            String selectedState = stateSpinner.getSelectedItem().toString();
            String selectedDistrict = districtSpinner.getSelectedItem().toString();
            String selectedTaluka = talukaSpinner.getSelectedItem().toString();

            // Validate input fields
            if (shopName.isEmpty() || shopAddress.isEmpty() ||
                    selectedState.isEmpty() || selectedDistrict.isEmpty() || selectedTaluka.isEmpty()) {
                Toast.makeText(TraderRegistrationActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if the user is already registered
            checkUserExists(phoneNumber, shopName, shopAddress, selectedState, selectedDistrict, selectedTaluka);
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

    private void checkUserExists(String phoneNumber, String shopName, String shopAddress,
                                 String state, String district, String taluka) {
        Query query = databaseReference.orderByChild("phoneNumber").equalTo(phoneNumber);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Toast.makeText(TraderRegistrationActivity.this, "User already registered with this phone number", Toast.LENGTH_SHORT).show();
                } else {
                    // Save user info to Firebase
                    String hashedPassword = hashPassword(password); // Hash the password
                    saveUserInfo(firstName, lastName, phoneNumber, hashedPassword, shopName, shopAddress, state, district, taluka);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TraderRegistrationActivity.this, "Error checking user registration", Toast.LENGTH_SHORT).show();
            }

        });
    }

    private void saveUserInfo(String firstName, String lastName, String phoneNumber, String password,
                              String shopName, String shopAddress, String state, String district, String taluka) {
        String userId = databaseReference.push().getKey();
        User user = new User(userId, firstName, lastName, phoneNumber, password, "Trader", shopName, shopAddress, state, district, taluka);

        if (userId != null) {
            databaseReference.child(userId).setValue(user)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(TraderRegistrationActivity.this, "Trader registered successfully", Toast.LENGTH_SHORT).show();

                            // Navigate to TraderDashboardActivity
                            Intent intent = new Intent(TraderRegistrationActivity.this, TraderDashboardActivity.class);
                            intent.putExtra("USER_ID", userId); // Pass the user ID
                            startActivity(intent);
                            finish(); // Close the activity
                        } else {
                            Toast.makeText(TraderRegistrationActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
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
}
