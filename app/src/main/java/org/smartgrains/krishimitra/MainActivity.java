package org.smartgrains.krishimitra;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.setLocale(this);
        setContentView(R.layout.activity_main);

        // Make status bar transparent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            EdgeToEdgeUtil.configureEdgeToEdge(getWindow());
        }

        // Setup login and signup buttons
        Button buttonLogin = findViewById(R.id.buttonLogin);
        Button buttonSignup = findViewById(R.id.buttonSignup);

        // Set the button text from string resources
        buttonLogin.setText(getString(R.string.main_login));
        buttonSignup.setText(getString(R.string.main_sign_up));

        // Login button click listener
        buttonLogin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Signup button click listener
        buttonSignup.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }
}


