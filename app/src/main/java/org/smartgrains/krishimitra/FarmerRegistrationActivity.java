package org.smartgrains.krishimitra;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class FarmerRegistrationActivity extends AppCompatActivity {

    private EditText editTextAddress, editTextLocality;
    private Spinner stateSpinner, districtSpinner, talukaSpinner;
    private Button buttonRegister, btnAutofill;
    private ProgressBar progressBar;
    private DatabaseReference databaseReference;
    private String firstName, lastName, phoneNumber, password;
    private CheckBox privacyPolicyCheckbox; // Declare the checkbox
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_ROLE = "USER_ROLE";
    private static final String KEY_USER_ID = "USER_ID";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_registration);

        // Make status bar transparent
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        // Initialize Views
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextLocality = findViewById(R.id.editTextLocality);
        stateSpinner = findViewById(R.id.state_spinner);
        districtSpinner = findViewById(R.id.district_spinner);
        talukaSpinner = findViewById(R.id.taluka_spinner);
        buttonRegister = findViewById(R.id.buttonRegister);
//        btnAutofill = findViewById(R.id.btnAutofill);
        progressBar = findViewById(R.id.progressBar); // Initialize progress bar
        privacyPolicyCheckbox = findViewById(R.id.privacy_policy_checkbox); // Initialize the checkbox
        privacyPolicyCheckbox.setMovementMethod(LinkMovementMethod.getInstance());

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Get Intent data
        Intent intent = getIntent();
        firstName = intent.getStringExtra("FIRST_NAME");
        lastName = intent.getStringExtra("LAST_NAME");
        phoneNumber = intent.getStringExtra("PHONE_NUMBER");
        password = intent.getStringExtra("PASSWORD"); // Get password from the previous activity

//        btnAutofill.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(FarmerRegistrationActivity.this, FarmerRegistrationFrag.class);
//
//                // Pass data to the next activity
//                intent.putExtra("FIRST_NAME", firstName);
//                intent.putExtra("LAST_NAME", lastName);
//                intent.putExtra("PHONE_NUMBER", phoneNumber);
//                intent.putExtra("PASSWORD", password); // Consider hashing password before passing
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                startActivity(intent);
//            }
//        });

        // Populate Location Spinners
        populateLocationSpinners();

        // Register Button Click Listener
        buttonRegister.setOnClickListener(v -> {
            if (!privacyPolicyCheckbox.isChecked()) {
                showPrivacyPolicyAlert(); // Show alert if checkbox is not checked
                return;
            }

            String address = editTextAddress.getText().toString().trim();
            String locality = editTextLocality.getText().toString().trim();
            String selectedState = stateSpinner.getSelectedItem().toString();
            String selectedDistrict = districtSpinner.getSelectedItem().toString();
            String selectedTaluka = talukaSpinner.getSelectedItem().toString();

            // Validate input fields
            if (address.isEmpty() || selectedState.isEmpty() || selectedDistrict.isEmpty() || selectedTaluka.isEmpty()) {
                Toast.makeText(FarmerRegistrationActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Disable the button and show progress bar
            buttonRegister.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);

            // Check if user is already registered before saving
            checkIfUserRegistered(phoneNumber, firstName, lastName, password, address, selectedState, selectedDistrict, selectedTaluka, locality);
        });
    }

    private void populateLocationSpinners() {
        LocationData.getStates(new LocationData.Callback<List<String>>() {
            @Override
            public void onSuccess(List<String> states) {
                String[] statesArray = states.toArray(new String[0]); // Convert List to String[]
                CustomSpinnerAdapter stateAdapter = new CustomSpinnerAdapter(FarmerRegistrationActivity.this, statesArray);
                stateSpinner.setAdapter(stateAdapter);

                stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selectedState = states.get(position);
                        populateDistrictSpinner(selectedState);  // Populate district spinner based on selected state
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(FarmerRegistrationActivity.this, "Failed to load states: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateDistrictSpinner(String state) {
        LocationData.getDistricts(state, new LocationData.Callback<List<String>>() {
            @Override
            public void onSuccess(List<String> districts) {
                String[] districtsArray = districts.toArray(new String[0]); // Convert List to String[]
                CustomSpinnerAdapter districtAdapter = new CustomSpinnerAdapter(FarmerRegistrationActivity.this, districtsArray);
                districtSpinner.setAdapter(districtAdapter);

                districtSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selectedDistrict = districts.get(position);
                        populateTalukaSpinner(state, selectedDistrict);  // Populate taluka spinner based on selected district
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(FarmerRegistrationActivity.this, "Failed to load districts: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateTalukaSpinner(String state, String district) {
        LocationData.getTalukas(state, district, new LocationData.Callback<List<String>>() {
            @Override
            public void onSuccess(List<String> talukas) {
                String[] talukasArray = talukas.toArray(new String[0]); // Convert List to String[]
                CustomSpinnerAdapter talukaAdapter = new CustomSpinnerAdapter(FarmerRegistrationActivity.this, talukasArray);
                talukaSpinner.setAdapter(talukaAdapter);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(FarmerRegistrationActivity.this, "Failed to load talukas: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfUserRegistered(String phoneNumber, String firstName, String lastName, String password,
                                       String address, String state, String district, String taluka, String locality) {
        databaseReference.orderByChild("phoneNumber").equalTo(phoneNumber)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Toast.makeText(FarmerRegistrationActivity.this, "User already registered with this phone number", Toast.LENGTH_SHORT).show();
                            resetProgress();
                        } else {
                            String hashedPassword = hashPassword(password); // Hash the password
                            saveUserInfo(firstName, lastName, phoneNumber, hashedPassword, address, state, district, taluka, locality);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("FirebaseError", databaseError.getMessage());
                        Toast.makeText(FarmerRegistrationActivity.this, "Error checking registration", Toast.LENGTH_SHORT).show();
                        resetProgress();
                    }
                });
    }

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

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e("HashError", "Error hashing password: " + e.getMessage());
            return password;
        }
    }

    private void saveUserInfo(String firstName, String lastName, String phoneNumber, String password,
                              String address, String state, String district, String taluka, String locality) {
        String userId = databaseReference.push().getKey();
        User user = new User(userId, firstName, lastName, phoneNumber, password, "Farmer", address, state, district, taluka, locality);

        if (userId != null) {
            databaseReference.child(userId).setValue(user)
                    .addOnCompleteListener(task -> {
                        resetProgress();
                        if (task.isSuccessful()) {
                            // Save user role and ID in SharedPreferences
                            saveLoginState("Farmer", userId);

                            Toast.makeText(FarmerRegistrationActivity.this, "Farmer registered successfully", Toast.LENGTH_SHORT).show();

                            // Navigate to the Farmer Dashboard
                            Intent intent = new Intent(FarmerRegistrationActivity.this, FarmerDashboardActivity.class);
                            intent.putExtra("USER_ID", userId);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(FarmerRegistrationActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void showPrivacyPolicyAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Privacy Policy")
                .setMessage("You must agree to the privacy policy to register.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void saveLoginState(String role, String userId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_ROLE, role);
        editor.putString(KEY_USER_ID, userId);
        editor.apply();
    }

    private void resetProgress() {
        progressBar.setVisibility(View.GONE); // Hide the progress bar
        buttonRegister.setEnabled(true); // Re-enable the button
    }

//    @Override
//    public void onBackPressed() {
//        // Redirect the user to the signup page
//        super.onBackPressed();
//        Intent intent = new Intent(FarmerRegistrationActivity.this, FarmerRegistrationFrag.class);
//        startActivity(intent);
//        finish(); // Optionally finish the current activity
//    }
}
