package org.smartgrains.krishimitra;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddLocation extends AppCompatActivity {

    private EditText inputState, inputDistrict, inputTaluka;
    private Button buttonSubmit;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);

        // Make status bar transparent
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        inputState = findViewById(R.id.inputState);
        inputDistrict = findViewById(R.id.inputDistrict);
        inputTaluka = findViewById(R.id.inputTaluka);
        buttonSubmit = findViewById(R.id.buttonSubmit);

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("LocationData");

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String state = inputState.getText().toString().trim();
                String district = inputDistrict.getText().toString().trim();
                String taluka = inputTaluka.getText().toString().trim();

                if (TextUtils.isEmpty(state) || TextUtils.isEmpty(district) || TextUtils.isEmpty(taluka)) {
                    Toast.makeText(AddLocation.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                    // Add data to Firebase
                    addLocationToFirebase(state, district, taluka);
                }
            }
        });
    }

    private void addLocationToFirebase(String state, String district, String taluka) {
        // Add taluka under the specified state and district
        databaseReference.child(state).child(district).push().setValue(taluka)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AddLocation.this, "Location added successfully!", Toast.LENGTH_SHORT).show();
                    // Clear the input fields
                    inputState.setText("");
                    inputDistrict.setText("");
                    inputTaluka.setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddLocation.this, "Failed to add location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
