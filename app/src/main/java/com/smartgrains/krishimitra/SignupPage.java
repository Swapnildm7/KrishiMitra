package com.smartgrains.krishimitra;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignupPage extends AppCompatActivity {

    private Spinner roleSpinner;
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
        roleSpinner = findViewById(R.id.role_spinner);
        buttonNext = findViewById(R.id.buttonNext);
        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);

        // Populate role spinner
        populateRoleSpinner();

        // Button click listener
        buttonNext.setOnClickListener(v -> {
            String selectedRole = roleSpinner.getSelectedItem().toString();
            Intent intent;

            // Get user input
            String firstName = editTextFirstName.getText().toString().trim();
            String lastName = editTextLastName.getText().toString().trim();
            String phoneNumber = editTextPhoneNumber.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            String confirmPassword = editTextConfirmPassword.getText().toString().trim();

            // Validate inputs
            if (firstName.isEmpty() || lastName.isEmpty() || phoneNumber.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(SignupPage.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if passwords match
            if (!password.equals(confirmPassword)) {
                Toast.makeText(SignupPage.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Proceed to the next page based on the selected role
            if (selectedRole.equals("Farmer")) {
                intent = new Intent(SignupPage.this, FarmerRegistrationActivity.class);
            } else {
                intent = new Intent(SignupPage.this, TraderRegistrationActivity.class);
            }

            // Pass data to the next activity
            intent.putExtra("FIRST_NAME", firstName);
            intent.putExtra("LAST_NAME", lastName);
            intent.putExtra("PHONE_NUMBER", phoneNumber);
            intent.putExtra("PASSWORD", password); // You may or may not want to pass the password, depending on security needs
            startActivity(intent);
        });
    }

    private void populateRoleSpinner() {
        String[] roles = {"Farmer", "Trader"};
        // Instantiate the custom spinner adapter
        CustomSpinnerAdapter roleAdapter = new CustomSpinnerAdapter(this, roles);
        // Set the adapter to the role spinner
        roleSpinner.setAdapter(roleAdapter);
    }

}
