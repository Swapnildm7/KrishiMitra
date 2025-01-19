package org.smartgrains.krishimitra;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class CropTranslation {

    private static final DatabaseReference translatedCropNamesRef = FirebaseDatabase.getInstance().getReference("TranslatedCropNames");

    // This method retrieves the translations for the given crop name
    public static void getTranslation(String cropName, final TranslationCallback callback) {
        translatedCropNamesRef.child(cropName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Map<String, String> translations = (Map<String, String>) snapshot.getValue();
                    if (translations != null) {
                        // Return translations in an array format (Hindi, Kannada, Marathi)
                        String[] translationArray = new String[]{
                                translations.get("Hindi"),
                                translations.get("Kannada"),
                                translations.get("Marathi")
                        };
                        callback.onTranslationFetched(translationArray);
                    } else {
                        callback.onTranslationFetched(new String[]{"N/A", "N/A", "N/A"});
                    }
                } else {
                    callback.onTranslationFetched(new String[]{"N/A", "N/A", "N/A"});
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onTranslationFetched(new String[]{"N/A", "N/A", "N/A"});
            }
        });
    }

    // Callback interface to handle the translation result asynchronously
    public interface TranslationCallback {
        void onTranslationFetched(String[] translations);
    }
}
