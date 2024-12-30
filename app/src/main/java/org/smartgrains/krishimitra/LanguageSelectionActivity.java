package org.smartgrains.krishimitra;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class LanguageSelectionActivity extends AppCompatActivity {

    private ImageView selectedLanguageFlag;
    private TextView selectedLanguageName;
    private Button btnSaveSettings;
    private String selectedLanguageCode = "en"; // Default language

    private final String[] languages = {"English", "हिन्दी", "ಕನ್ನಡ", "मराठी"}; // Localized language names
    private final String[] languageCodes = {"en", "hi", "kn", "mr"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.setLocale(this); // Apply user's preferred language
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_selection);

        // Make status bar transparent
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        selectedLanguageFlag = findViewById(R.id.selectedLanguageFlag);
        selectedLanguageName = findViewById(R.id.selectedLanguageName);
        btnSaveSettings = findViewById(R.id.btnSaveSettings);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewLanguages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load previously saved language preference
        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        selectedLanguageCode = preferences.getString("LanguageCode", "en");
        updateSelectedLanguageUI();

        // Set up adapter
        LanguageAdapter adapter = new LanguageAdapter((languageCode, flagResId, languageName) -> {
            // Update selected language
            selectedLanguageCode = languageCode;
            selectedLanguageFlag.setImageResource(flagResId);
            selectedLanguageName.setText(getLocalizedLanguageName(languageCode));

            // Save the language preference and recreate the activity
            LocaleHelper.saveLocale(this, languageCode);
            recreate(); // Recreate activity to apply new locale
        });

        recyclerView.setAdapter(adapter);

        // Save button functionality
        btnSaveSettings.setOnClickListener(v -> {
            saveLanguagePreference(selectedLanguageCode);
            navigateToNextScreen();
        });
    }

    private void saveLanguagePreference(String languageCode) {
        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        preferences.edit().putString("LanguageCode", languageCode).apply();
        LocaleHelper.saveLocale(this, languageCode); // Save and apply the locale
    }

    private void navigateToNextScreen() {
        // Navigate to the next screen, e.g., MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void updateSelectedLanguageUI() {
        // Update the selected language name and flag based on the saved preference
        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equals(selectedLanguageCode)) {
                selectedLanguageFlag.setImageResource(getFlagResource(i));
                selectedLanguageName.setText(getLocalizedLanguageName(selectedLanguageCode));
                break;
            }
        }
    }

    private String getLocalizedLanguageName(String languageCode) {
        // Return the localized language name based on the language code
        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equals(languageCode)) {
                return languages[i];
            }
        }
        return "English"; // Default to English
    }

    private int getFlagResource(int index) {
        // Return the flag resource ID based on the index
        int[] flags = {R.drawable.ic_english, R.drawable.ic_hindi, R.drawable.ic_kannada, R.drawable.ic_marathi};
        return flags[index];
    }
}
