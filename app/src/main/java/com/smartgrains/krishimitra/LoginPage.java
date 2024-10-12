package com.smartgrains.krishimitra;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginPage extends AppCompatActivity {

    private EditText editTextPhoneNumber, editTextPassword;
    private Button buttonLogin;
    private TextView signUpText, forgotPasswordText;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        // Make status bar transparent
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize Views
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        signUpText = findViewById(R.id.sign_up_text);
        forgotPasswordText = findViewById(R.id.forgot_password_text);

        // Set Click Listener for Login Button
        buttonLogin.setOnClickListener(v -> loginUser());

        // Set Click Listener for Sign Up Text
        signUpText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginPage.this, SignupPage.class);
            startActivity(intent);
        });

        // Set Click Listener for Forgot Password Text
        forgotPasswordText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginPage.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Validate input fields
        if (phoneNumber.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginPage.this, "Please enter phone number and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check user credentials in the database
        databaseReference.orderByChild("phoneNumber").equalTo(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String storedPassword = userSnapshot.child("password").getValue(String.class);
                        String role = userSnapshot.child("role").getValue(String.class);
                        String userId = userSnapshot.getKey(); // Get the User ID (key)

                        if (storedPassword != null && storedPassword.equals(password)) {
                            // Navigate to the corresponding dashboard with User ID
                            navigateToDashboard(role, userId);
                        } else {
                            Toast.makeText(LoginPage.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(LoginPage.this, "User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(LoginPage.this, "Error checking credentials", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToDashboard(String role, String userId) {
        Intent intent;
        if ("Trader".equals(role)) {
            intent = new Intent(LoginPage.this, TraderDashboardActivity.class);
        } else if ("Farmer".equals(role)) {
            intent = new Intent(LoginPage.this, FarmerDashboardActivity.class);
        } else {
            Toast.makeText(LoginPage.this, "Invalid role", Toast.LENGTH_SHORT).show();
            return;
        }

        // Pass the User ID to the dashboard activity
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
        finish(); // Close the login page
    }
}
