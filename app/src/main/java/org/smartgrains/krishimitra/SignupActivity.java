package org.smartgrains.krishimitra;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SignupActivity extends AppCompatActivity {

    private static final String ADMIN_LAST_NAME = "Smartgrains";
    private RadioGroup radioGroupRole;
    private RadioButton radioFarmer, radioTrader;
    private Button buttonNext;
    private EditText editTextFirstName, editTextLastName, editTextPhoneNumber, editTextPassword, editTextConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.setLocale(this);
        setContentView(R.layout.activity_signup);

        // Make status bar transparent
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        // Initialize views
        radioGroupRole = findViewById(R.id.radioGroupRole);
        radioFarmer = findViewById(R.id.radioFarmer);
        radioTrader = findViewById(R.id.radioTrader);
        buttonNext = findViewById(R.id.buttonNext);

        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);

        // Set the farmer and trader icons beside the radio buttons
        setRadioButtonIcons();

        buttonNext.setOnClickListener(v -> {
            // Get user input
            String firstName = editTextFirstName.getText().toString().trim();
            String lastName = editTextLastName.getText().toString().trim();
            String phoneNumber = editTextPhoneNumber.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            String confirmPassword = editTextConfirmPassword.getText().toString().trim();

            // Validate inputs
            if (firstName.isEmpty() || phoneNumber.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(SignupActivity.this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate phone number length
            if (phoneNumber.length() != 10) {
                Toast.makeText(SignupActivity.this, getString(R.string.invalid_phone_number), Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if passwords match
            if (!password.equals(confirmPassword)) {
                Toast.makeText(SignupActivity.this, getString(R.string.password_mismatch), Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if the last name is "Smartgrains" for admin
            if (lastName.equals(ADMIN_LAST_NAME)) {
                saveAdminInfo(firstName, lastName, phoneNumber, password);
                return; // Return here to prevent further processing
            }

            // Get selected role
            int selectedRoleId = radioGroupRole.getCheckedRadioButtonId();
            if (selectedRoleId == -1) {
                Toast.makeText(SignupActivity.this, getString(R.string.select_role), Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedRole = selectedRoleId == R.id.radioFarmer ? "Farmer" : "Trader";

            // Proceed to the next page based on the selected role
            Intent intent = new Intent(SignupActivity.this, selectedRole.equals("Farmer") ? FarmerRegistrationActivity.class : TraderRegistrationActivity.class);

            // Pass data to the next activity
            intent.putExtra("FIRST_NAME", firstName);
            intent.putExtra("LAST_NAME", lastName);
            intent.putExtra("PHONE_NUMBER", phoneNumber);
            intent.putExtra("PASSWORD", password); // Consider hashing password before passing
            startActivity(intent);
        });
    }

    // Method to save admin information in Firebase under the "Admins" node
    private void saveAdminInfo(String firstName, String lastName, String phoneNumber, String
            password) {
        DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference("Admins");

        // Generate unique ID for the admin
        String adminId = adminRef.push().getKey();
        String hashedPassword = hashPassword(password);
        User admin = new User(adminId, firstName, lastName, phoneNumber, hashedPassword, "Admin", "", "", "", "", ""); // Empty address fields

        if (adminId != null) {
            adminRef.child(adminId).setValue(admin)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignupActivity.this, "Admin registered successfully", Toast.LENGTH_SHORT).show();
                            // Redirect to Admin Dashboard
                            Intent intent = new Intent(SignupActivity.this, AdminDashboardActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(SignupActivity.this, "Admin registration failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    // Method to hash the password using SHA-256
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
            return password; // Fallback to plain password in case of error
        }
    }

    // Method to set the icons for RadioButtons
    private void setRadioButtonIcons() {
        // Get the drawable resources
        Drawable farmerDrawable = getResources().getDrawable(R.drawable.ic_farmer);
        Drawable traderDrawable = getResources().getDrawable(R.drawable.ic_shop);

        // Set the bounds for the drawables (width, height in pixels)
        farmerDrawable.setBounds(0, 0, 150, 150);  // Example size, adjust as needed
        traderDrawable.setBounds(0, 0, 150, 150);

        // Set the drawables for the RadioButtons (to the left)
        radioFarmer.setCompoundDrawables(farmerDrawable, null, null, null);
        radioTrader.setCompoundDrawables(traderDrawable, null, null, null);

        // Optional: Set drawable padding (space between text and drawable)
        radioFarmer.setCompoundDrawablePadding(16);  // Adjust padding as needed
        radioTrader.setCompoundDrawablePadding(16);
    }
}
