package org.smartgrains.krishimitra;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignupPage extends AppCompatActivity {

    private RadioGroup radioGroupRole;
    private RadioButton radioFarmer, radioTrader;
    private Button buttonNext;
    private EditText editTextFirstName, editTextLastName, editTextPhoneNumber, editTextPassword, editTextConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_page);

        // Make status bar transparent
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
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

        // Button click listener
        buttonNext.setOnClickListener(v -> {
            // Get selected role
            int selectedRoleId = radioGroupRole.getCheckedRadioButtonId();

            if (selectedRoleId == -1) {
                Toast.makeText(SignupPage.this, "Please select a role", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedRole;
            if (selectedRoleId == R.id.radioFarmer) {
                selectedRole = "Farmer";
            } else {
                selectedRole = "Trader";
            }

            // Get user input
            String firstName = editTextFirstName.getText().toString().trim();
            String lastName = editTextLastName.getText().toString().trim();
            String phoneNumber = editTextPhoneNumber.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            String confirmPassword = editTextConfirmPassword.getText().toString().trim();

            // Validate inputs
            if (firstName.isEmpty() || phoneNumber.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(SignupPage.this, "Please fill in all compulsory fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate phone number length
            if (phoneNumber.length() != 10) {
                Toast.makeText(SignupPage.this, "Phone number must be 10 digits", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if passwords match
            if (!password.equals(confirmPassword)) {
                Toast.makeText(SignupPage.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Proceed to the next page based on the selected role
            Intent intent;
            if (selectedRole.equals("Farmer")) {
                intent = new Intent(SignupPage.this, FarmerRegistrationActivity.class);
            } else {
                intent = new Intent(SignupPage.this, TraderRegistrationActivity.class);
            }

            // Pass data to the next activity
            intent.putExtra("FIRST_NAME", firstName);
            intent.putExtra("LAST_NAME", lastName);
            intent.putExtra("PHONE_NUMBER", phoneNumber);
            intent.putExtra("PASSWORD", password); // Consider hashing password before passing
            startActivity(intent);
        });
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
