package org.smartgrains.krishimitra;

import android.content.Context;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class TraderRegistrationActivity extends AppCompatActivity {

    private static final String TAG = "TraderRegistrationActivity"; // For logging

    private EditText editTextShopName, editTextShopAddress, editTextLocality;
    private Spinner stateSpinner, districtSpinner, talukaSpinner;
    private Button buttonRegister;
    private CheckBox privacyPolicyCheckbox;
    private ProgressBar progressBar;
    private DatabaseReference databaseReference;
    private String firstName, lastName, phoneNumber, password;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_ROLE = "USER_ROLE";
    private static final String KEY_USER_ID = "USER_ID";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trader_registration);

        // Make status bar transparent
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Initialize Views
        initializeViews();

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Get Intent data
        Intent intent = getIntent();
        retrieveIntentData(intent);

        // Populate Location Spinners
        populateLocationSpinners();

        // Register Button Click Listener
        buttonRegister.setOnClickListener(v -> {
            if (!privacyPolicyCheckbox.isChecked()) {
                showPrivacyPolicyAlert();
                return;
            }

            if (validateInputs()) {
                startLoadingProcess();
                checkUserExists(phoneNumber, editTextShopName.getText().toString().trim(),
                        editTextShopAddress.getText().toString().trim(),
                        stateSpinner.getSelectedItem().toString(),
                        districtSpinner.getSelectedItem().toString(),
                        talukaSpinner.getSelectedItem().toString(),
                        editTextLocality.getText().toString().trim());
            }
        });
    }

    private void initializeViews() {
        editTextShopName = findViewById(R.id.editTextShopName);
        editTextShopAddress = findViewById(R.id.editTextShopAddress);
        editTextLocality = findViewById(R.id.editTextLocality);
        stateSpinner = findViewById(R.id.state_spinner);
        districtSpinner = findViewById(R.id.district_spinner);
        talukaSpinner = findViewById(R.id.taluka_spinner);
        buttonRegister = findViewById(R.id.buttonRegister);
        privacyPolicyCheckbox = findViewById(R.id.privacy_policy_checkbox);
        progressBar = findViewById(R.id.progressBar);
        privacyPolicyCheckbox.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void retrieveIntentData(Intent intent) {
        firstName = intent.getStringExtra("FIRST_NAME");
        lastName = intent.getStringExtra("LAST_NAME");
        phoneNumber = intent.getStringExtra("PHONE_NUMBER");
        password = intent.getStringExtra("PASSWORD");
    }

    private boolean validateInputs() {
        String shopName = editTextShopName.getText().toString().trim();
        String shopAddress = editTextShopAddress.getText().toString().trim();
        String selectedState = stateSpinner.getSelectedItem().toString();
        String selectedDistrict = districtSpinner.getSelectedItem().toString();
        String selectedTaluka = talukaSpinner.getSelectedItem().toString();

        if (shopName.isEmpty() || shopAddress.isEmpty() || selectedState.isEmpty() ||
                selectedDistrict.isEmpty() || selectedTaluka.isEmpty()) {
            showToast("Please fill in all fields");
            return false;
        }
        return true;
    }

    private void startLoadingProcess() {
        buttonRegister.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void showPrivacyPolicyAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Privacy Policy")
                .setMessage("You must agree to the privacy policy to register.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void populateLocationSpinners() {
        LocationData.getStates(new LocationData.Callback<List<String>>() {
            @Override
            public void onSuccess(List<String> states) {
                String[] statesArray = states.toArray(new String[0]);
                CustomSpinnerAdapter stateAdapter = new CustomSpinnerAdapter(TraderRegistrationActivity.this, statesArray);
                stateSpinner.setAdapter(stateAdapter);

                stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        populateDistrictSpinner(states.get(position));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                showToast("Failed to load states: " + error);
            }
        });
    }

    private void populateDistrictSpinner(String state) {
        LocationData.getDistricts(state, new LocationData.Callback<List<String>>() {
            @Override
            public void onSuccess(List<String> districts) {
                String[] districtsArray = districts.toArray(new String[0]);
                CustomSpinnerAdapter districtAdapter = new CustomSpinnerAdapter(TraderRegistrationActivity.this, districtsArray);
                districtSpinner.setAdapter(districtAdapter);

                districtSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        populateTalukaSpinner(districts.get(position));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                showToast("Failed to load districts: " + error);
            }
        });
    }

    private void populateTalukaSpinner(String district) {
        LocationData.getTalukas(stateSpinner.getSelectedItem().toString(), district, new LocationData.Callback<List<String>>() {
            @Override
            public void onSuccess(List<String> talukas) {
                String[] talukasArray = talukas.toArray(new String[0]);
                CustomSpinnerAdapter talukaAdapter = new CustomSpinnerAdapter(TraderRegistrationActivity.this, talukasArray);
                talukaSpinner.setAdapter(talukaAdapter);
            }

            @Override
            public void onFailure(String error) {
                showToast("Failed to load talukas: " + error);
            }
        });
    }

    private void checkUserExists(String phoneNumber, String shopName, String shopAddress,
                                 String state, String district, String taluka, String locality) {
        Query query = databaseReference.orderByChild("phoneNumber").equalTo(phoneNumber);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                buttonRegister.setEnabled(true);
                if (dataSnapshot.exists()) {
                    showToast("User already registered with this phone number");
                } else {
                    String hashedPassword = hashPassword(password);
                    saveUserInfo(firstName, lastName, phoneNumber, hashedPassword, shopName, shopAddress, state, district, taluka, locality);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                buttonRegister.setEnabled(true);
                showToast("Error checking user registration: " + error.getMessage());
                logError("checkUserExists", error.toException());
            }
        });
    }

    private void saveUserInfo(String firstName, String lastName, String phoneNumber, String password,
                              String shopName, String shopAddress, String state, String district, String taluka, String locality) {
        String userId = databaseReference.push().getKey();
        User user = new User(userId, firstName, lastName, phoneNumber, password, "Trader", shopName, shopAddress, state, district, taluka, locality);

        if (userId != null) {
            databaseReference.child(userId).setValue(user)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        buttonRegister.setEnabled(true);
                        if (task.isSuccessful()) {
                            showToast("Trader registered successfully");
                            saveLoginState("Farmer", userId);
                            navigateToDashboard(userId);
                        } else {
                            showToast("Registration failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                            logError("saveUserInfo", task.getException());
                        }
                    });
        } else {
            showToast("Error creating user ID. Please try again.");
            logError("saveUserInfo", new Exception("User ID is null"));
        }
    }

    private void saveLoginState(String role, String userId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_ROLE, role);
        editor.putString(KEY_USER_ID, userId);
        editor.apply();
    }

    private void navigateToDashboard(String userId) {
        Intent intent = new Intent(TraderRegistrationActivity.this, TraderDashboardActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
        finish();
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
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            logError("hashPassword", e);
            showToast("Error hashing password. Please try again.");
            return null; // Handle password hashing failure gracefully
        }
    }

    private void showToast(String message) {
        Toast.makeText(TraderRegistrationActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void logError(String methodName, Exception e) {
        Log.e(TAG, "Method: " + methodName, e); // Log error with method name
        e.printStackTrace(); // For simplicity, we also print the stack trace
    }
}
