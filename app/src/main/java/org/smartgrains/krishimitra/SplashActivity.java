package org.smartgrains.krishimitra;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashActivity extends AppCompatActivity {

    // SharedPreferences constants
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_ROLE = "USER_ROLE";
    private static final String KEY_USER_ID = "USER_ID";
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.setLocale(this); // Apply user's preferred language
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            EdgeToEdgeUtil.configureEdgeToEdge(getWindow());
        }

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Find the ImageView by ID
        ImageView logo = findViewById(R.id.logo);
        TextView appName = findViewById(R.id.appName);
        TextView slogan = findViewById(R.id.slogan);

        // Load and start animation
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.logo_animation);
        Animation appNameAnimation = AnimationUtils.loadAnimation(this, R.anim.appname_animation);

        logo.startAnimation(animation);
        appName.startAnimation(appNameAnimation);
        slogan.startAnimation(appNameAnimation);

        // Delay and transition to the correct screen based on login status
        new Handler().postDelayed(() -> {
            checkLoginState(); // Check if the user is already logged in
        }, 3000); // 3-second delay
    }

    private void checkLoginState() {
        boolean isFirstTimeUser = sharedPreferences.getBoolean("IS_FIRST_TIME_USER", true);
        String role = sharedPreferences.getString(KEY_ROLE, null);
        String userId = sharedPreferences.getString(KEY_USER_ID, null);

        if (role != null && userId != null) {
            // User is already logged in, navigate to the respective dashboard
            navigateToDashboard(role, userId);
        } else if (isFirstTimeUser) {
            // First-time user, navigate to LanguageSelectionActivity
            Intent intent = new Intent(SplashActivity.this, LanguageSelectionActivity.class);
            startActivity(intent);
            finish();
        } else {
            // Returning user, but not logged in, navigate to LoginActivity
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }


    private void navigateToDashboard(String role, String userId) {
        Intent intent;
        if ("Trader".equals(role)) {
            intent = new Intent(SplashActivity.this, TraderDashboardActivity.class);
        } else if ("Farmer".equals(role)) {
            intent = new Intent(SplashActivity.this, FarmerDashboardActivity.class);
        } else if ("Admin".equals(role)) {
            intent = new Intent(SplashActivity.this, AdminDashboardActivity.class);
        } else {
            // In case of invalid role, return to LoginActivity
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }

        // Pass the User ID to the dashboard activity
        intent.putExtra("USER_ID", userId);

        // Clear the back stack to prevent returning to splash screen
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish(); // Close the splash screen
    }
}