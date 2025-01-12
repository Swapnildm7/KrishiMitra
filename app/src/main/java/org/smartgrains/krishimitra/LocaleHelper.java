package org.smartgrains.krishimitra;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public class LocaleHelper {

    private static final String LANGUAGE_KEY = "LanguageCode";

    public static void setLocale(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String languageCode = preferences.getString(LANGUAGE_KEY, "en"); // Default to English
        updateResources(context, languageCode);
    }

    public static void saveLocale(Context context, String languageCode) {
        SharedPreferences preferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        preferences.edit().putString(LANGUAGE_KEY, languageCode).apply();
        updateResources(context, languageCode);
    }

    private static void updateResources(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }
}