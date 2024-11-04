package org.smartgrains.krishimitra;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CropLanguage extends AppCompatActivity {

    private Spinner spinnerCrops;
    private EditText editTextHindi, editTextKannada, editTextMarathi;
    private Button buttonSaveCrop;

    private DatabaseReference cropResourceRef; // Reference to CropResource node
    private DatabaseReference translatedCropNamesRef; // Reference to TranslatedCropNames node

    private String selectedCropId; // Selected crop ID
    private String selectedCropName; // Selected crop name
    private List<String> cropNames = new ArrayList<>(); // List of crop names
    private List<String> cropIds = new ArrayList<>(); // List of crop IDs

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_language);

        // Initialize Firebase database references
        cropResourceRef = FirebaseDatabase.getInstance().getReference("CropResource");
        translatedCropNamesRef = FirebaseDatabase.getInstance().getReference("TranslatedCropNames");

        // Initialize views
        spinnerCrops = findViewById(R.id.spinnerCrops);
        editTextHindi = findViewById(R.id.editTextHindi);
        editTextKannada = findViewById(R.id.editTextKannada);
        editTextMarathi = findViewById(R.id.editTextMarathi);
        buttonSaveCrop = findViewById(R.id.buttonSaveCrop);

        // Fetch crop names and IDs from Firebase
        loadCropNames();

        // Set up spinner listener
        spinnerCrops.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCropId = cropIds.get(position);
                selectedCropName = cropNames.get(position);

                // Clear the EditTexts when a new crop is selected
                editTextHindi.setText("");
                editTextKannada.setText("");
                editTextMarathi.setText("");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCropId = null;
                selectedCropName = null;
            }
        });

        // Set up Save button listener
        buttonSaveCrop.setOnClickListener(v -> {
            if (selectedCropId != null && selectedCropName != null) {
                // Get translations from EditTexts
                String hindi = editTextHindi.getText().toString().trim();
                String kannada = editTextKannada.getText().toString().trim();
                String marathi = editTextMarathi.getText().toString().trim();

                if (!hindi.isEmpty() && !kannada.isEmpty() && !marathi.isEmpty()) {
                    // Save translations to Firebase
                    saveTranslations(selectedCropId, selectedCropName, hindi, kannada, marathi);
                } else {
                    Toast.makeText(CropLanguage.this, "All fields must be filled!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(CropLanguage.this, "Please select a crop!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCropNames() {
        cropResourceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cropNames.clear();
                cropIds.clear();
                for (DataSnapshot cropSnapshot : snapshot.getChildren()) {
                    String cropId = cropSnapshot.getKey(); // Get the unique crop ID
                    String cropName = cropSnapshot.child("cropName").getValue(String.class); // Get the crop name
                    if (cropId != null && cropName != null) {
                        cropIds.add(cropId);
                        cropNames.add(cropName);
                    }
                }

                // Populate spinner with crop names
                ArrayAdapter<String> adapter = new ArrayAdapter<>(CropLanguage.this, android.R.layout.simple_spinner_item, cropNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCrops.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CropLanguage.this, "Failed to load crop names", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveTranslations(String cropId, String cropName, String hindi, String kannada, String marathi) {
        // Save translations to the TranslatedCropNames node using cropName as the key
        Map<String, String> translations = new HashMap<>();
        translations.put("Hindi", hindi);
        translations.put("Kannada", kannada);
        translations.put("Marathi", marathi);

        translatedCropNamesRef.child(cropName).setValue(translations).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(CropLanguage.this, "Translations saved successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(CropLanguage.this, "Failed to save translations", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

