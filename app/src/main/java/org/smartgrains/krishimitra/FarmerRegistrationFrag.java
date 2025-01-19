package org.smartgrains.krishimitra;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.method.LinkMovementMethod;
import android.widget.CheckBox;

import androidx.appcompat.app.AlertDialog;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FarmerRegistrationFrag extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final String TAG = "MainActivity";
    private FusedLocationProviderClient fusedLocationClient;
    private EditText etAddress, etLocality;
    private TextView tvState, tvDistrict, tvTaluka, tvPostalcode;
    private Spinner spinnerVillage;
    private ProgressBar progressBar;
    private String postalCode;
    private Button buttonRegister;
    private DatabaseReference databaseReference;
    private String firstName, lastName, phoneNumber, password;
    private CheckBox privacyPolicyCheckbox; // Declare the checkbox
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_ROLE = "USER_ROLE";
    private static final String KEY_USER_ID = "USER_ID";
    private String address;
    private String locality;
    private String selectedState;
    private String selectedDistrict;
    private String selectedTaluka;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_autofill);

        // Make status bar transparent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            EdgeToEdgeUtil.configureEdgeToEdge(getWindow());
        }

        // Initialize UI elements
        Button btnUseMyLocation = findViewById(R.id.btnUseMyLocation);
        etAddress = findViewById(R.id.etAddress);
        tvState = findViewById(R.id.tvState);
        tvTaluka = findViewById(R.id.tvTaluka);
        tvDistrict = findViewById(R.id.tvDistrict);
        tvPostalcode = findViewById(R.id.tvPostalcode);
        spinnerVillage = findViewById(R.id.spinnerVillage);
        buttonRegister = findViewById(R.id.buttonRegister);
        privacyPolicyCheckbox = findViewById(R.id.privacy_policy_checkbox); // Initialize the checkbox
        privacyPolicyCheckbox.setMovementMethod(LinkMovementMethod.getInstance());
        progressBar = findViewById(R.id.progressBar);
        Button btnManually = findViewById(R.id.btnManual);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Get Intent data
        Intent intent = getIntent();
        firstName = intent.getStringExtra("FIRST_NAME");
        lastName = intent.getStringExtra("LAST_NAME");
        phoneNumber = intent.getStringExtra("PHONE_NUMBER");
        password = intent.getStringExtra("PASSWORD");

        btnManually.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FarmerRegistrationFrag.this, FarmerRegistrationActivity.class);

                // Pass data to the next activity
                intent.putExtra("FIRST_NAME", firstName);
                intent.putExtra("LAST_NAME", lastName);
                intent.putExtra("PHONE_NUMBER", phoneNumber);
                intent.putExtra("PASSWORD", password); // Consider hashing password before passing
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Set click listener for "Use My Location" button
        btnUseMyLocation.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                progressBar.setVisibility(View.VISIBLE);
                fetchLocation();
            }
        });

        // Register Button Click Listener
        buttonRegister.setOnClickListener(v -> {
            if (!privacyPolicyCheckbox.isChecked()) {
                showPrivacyPolicyAlert(); // Show alert if checkbox is not checked
                return;
            }

            // Validate input fields
            if (address.isEmpty() || selectedState.isEmpty() || selectedDistrict.isEmpty() || selectedTaluka.isEmpty()) {
                Toast.makeText(FarmerRegistrationFrag.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Disable the button and show progress bar
            buttonRegister.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);

            // Check if user is already registered before saving
            checkIfUserRegistered(phoneNumber, firstName, lastName, password, address, selectedState, selectedDistrict, selectedTaluka, locality);
        });
    }

    private void fetchLocation() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000); // 5 seconds
        locationRequest.setFastestInterval(2000); // 2 seconds

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permissions are missing", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null && !locationResult.getLocations().isEmpty()) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        Log.d(TAG, "Location fetched: " + location.getLatitude() + ", " + location.getLongitude());
                        getAddressFromLocation(location.getLatitude(), location.getLongitude());
                    } else {
                        Log.e(TAG, "Location is null");
                        Toast.makeText(FarmerRegistrationFrag.this, "Unable to fetch location", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "LocationResult is empty");
                    Toast.makeText(FarmerRegistrationFrag.this, "No location available", Toast.LENGTH_SHORT).show();
                }
                fusedLocationClient.removeLocationUpdates(this);
            }
        }, getMainLooper());
    }

    private void getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address addressDetails = addresses.get(0);

                // Populate the fields
                etAddress.setText(addressDetails.getAddressLine(0)); // Full address
                tvPostalcode.setText(addressDetails.getPostalCode()); // Pincode
                tvState.setText(addressDetails.getAdminArea()); // State
                tvTaluka.setText(addressDetails.getLocality()); // Taluka or locality

                postalCode = addressDetails.getPostalCode();
                selectedState = addressDetails.getAdminArea();
                selectedTaluka = addressDetails.getLocality();

                // Listen for changes in the address EditText
                etAddress.setOnFocusChangeListener((v, hasFocus) -> {
                    if (!hasFocus) {
                        // Update the address variable when the user leaves the field
                        address = etAddress.getText().toString().trim();
                    }
                });

                // Alternatively, use a TextWatcher for real-time updates
                etAddress.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        address = s.toString().trim();
                    }
                });

                fetchPincodeData(postalCode, address, selectedState, selectedTaluka);

                progressBar.setVisibility(View.GONE);

            } else {
                Log.e(TAG, "No address found for the location");
                Toast.makeText(this, "No address found for this location", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error fetching address: " + e.getMessage());
            Toast.makeText(this, "Error fetching address: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchPincodeData(String postalCode, String address, String selectedState, String selectedTaluka) {
        if (postalCode == null || postalCode.isEmpty()) {
            Toast.makeText(this, "Invalid postal code", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "https://api.postalpincode.in/pincode/" + postalCode;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject responseObject = response.getJSONObject(0);
                        if ("Success".equals(responseObject.getString("Status"))) {
                            JSONArray postOffices = responseObject.getJSONArray("PostOffice");
                            selectedDistrict = postOffices.getJSONObject(0).getString("District");
                            tvDistrict.setText(selectedDistrict);

                            // Create village list with a hint option
                            ArrayList<String> villageList = new ArrayList<>();
                            villageList.add("Select your village below");  // Adding hint option
                            for (int i = 0; i < postOffices.length(); i++) {
                                villageList.add(postOffices.getJSONObject(i).getString("Name"));
                            }

                            // Set up the Spinner adapter
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    FarmerRegistrationFrag.this,
                                    android.R.layout.simple_spinner_item,
                                    villageList);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerVillage.setAdapter(adapter);

                            // Set a listener to store the selected village
                            spinnerVillage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                                    locality = villageList.get(position);
                                    // Check if the user has selected a valid village or left it on the hint
                                    if (!"Select your village below".equals(locality)) {

                                        Log.d(TAG, "Selected Village: " + locality);

                                    } else {
                                        // No valid selection made
                                        locality = "";
                                        Log.d(TAG, "No village selected");
                                    }
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parentView) {
                                    // Handle case when nothing is selected (optional)
                                }
                            });

                        } else {
                            Toast.makeText(this, "Invalid postal code response", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing pincode data", e);
                    }
                },
                error -> Log.e(TAG, "API call failed", error));

        Volley.newRequestQueue(this).add(jsonArrayRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkIfUserRegistered(String phoneNumber, String firstName, String lastName, String password,
                                       String address, String state, String district, String taluka, String locality) {
        databaseReference.orderByChild("phoneNumber").equalTo(phoneNumber)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Toast.makeText(FarmerRegistrationFrag.this, "User already registered with this phone number", Toast.LENGTH_SHORT).show();
                            resetProgress();
                        } else {
                            String hashedPassword = hashPassword(password); // Hash the password
                            saveUserInfo(firstName, lastName, phoneNumber, hashedPassword, address, state, district, taluka, locality);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("FirebaseError", databaseError.getMessage());
                        Toast.makeText(FarmerRegistrationFrag.this, "Error checking registration", Toast.LENGTH_SHORT).show();
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

                            Toast.makeText(FarmerRegistrationFrag.this, "Farmer registered successfully", Toast.LENGTH_SHORT).show();

                            // Navigate to the Farmer Dashboard
                            Intent intent = new Intent(FarmerRegistrationFrag.this, FarmerDashboardActivity.class);
                            intent.putExtra("USER_ID", userId);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(FarmerRegistrationFrag.this, "Registration failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
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

    private void showPrivacyPolicyAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Privacy Policy")
                .setMessage("You must agree to the privacy policy to register.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
