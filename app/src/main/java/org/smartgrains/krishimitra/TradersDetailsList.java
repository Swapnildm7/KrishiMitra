package org.smartgrains.krishimitra;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TradersDetailsList extends AppCompatActivity {

    private Spinner stateSpinner, districtSpinner, talukaSpinner;
    private ProgressBar progressBar;
    private Button applyFilterButton;
    private String intentState, intentDistrict, intentTaluka, intentUserId;
    private TextView textMessageTrader;
    List<Trader> tradersList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traders_details_list);

        // Make status bar transparent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            EdgeToEdgeUtil.configureEdgeToEdge(getWindow());
        }

        // Initialize Views
        stateSpinner = findViewById(R.id.state_spinner);
        districtSpinner = findViewById(R.id.district_spinner);
        talukaSpinner = findViewById(R.id.taluka_spinner);
        progressBar = findViewById(R.id.progressBar);
        applyFilterButton = findViewById(R.id.applyFilterButton); // Button for apply filter
        textMessageTrader = findViewById(R.id.textMessageTrader); // Initialize the TextView

        // Get values from intent
        Intent intent = getIntent();
        intentState = intent.getStringExtra("FARMER_STATE");
        intentDistrict = intent.getStringExtra("FARMER_DISTRICT");
        intentTaluka = intent.getStringExtra("FARMER_TALUKA");
        intentUserId = intent.getStringExtra("USER_ID");

        // Populate spinners and set listeners
        populateLocationSpinners();

        // Fetch initial trader list based on intent values
        fetchTraders(intentState, intentDistrict, intentTaluka);

        // Set click listener for Apply Filter button
        setupApplyFilterButton();
    }

    private void populateLocationSpinners() {
        LocationData.getStates(new LocationData.Callback<List<String>>() {
            @Override
            public void onSuccess(List<String> states) {

                // Add "State" as the first option
                List<String> statesWithPlaceholder = new ArrayList<>();
                statesWithPlaceholder.add("State");
                statesWithPlaceholder.addAll(states);

                String[] statesArray = statesWithPlaceholder.toArray(new String[0]);
                CustomSpinnerAdapter stateAdapter = new CustomSpinnerAdapter(TradersDetailsList.this, statesArray);
                stateSpinner.setAdapter(stateAdapter);

                stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (position > 0) { // Ignore "State" placeholder
                            String selectedState = statesWithPlaceholder.get(position);
                            populateDistrictSpinner(selectedState); // Populate district spinner
                        } else {
                            resetDistrictAndTalukaSpinners(); // Reset dependent spinners
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(TradersDetailsList.this, "Failed to load states: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateDistrictSpinner(String state) {
        LocationData.getDistricts(state, new LocationData.Callback<List<String>>() {
            @Override
            public void onSuccess(List<String> districts) {

                // Add "District" as the first option
                List<String> districtsWithPlaceholder = new ArrayList<>();
                districtsWithPlaceholder.add("District");
                districtsWithPlaceholder.addAll(districts);

                String[] districtsArray = districtsWithPlaceholder.toArray(new String[0]);
                CustomSpinnerAdapter districtAdapter = new CustomSpinnerAdapter(TradersDetailsList.this, districtsArray);
                districtSpinner.setAdapter(districtAdapter);

                districtSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (position > 0) { // Ignore "District" placeholder
                            String selectedDistrict = districtsWithPlaceholder.get(position);
                            populateTalukaSpinner(state, selectedDistrict); // Populate taluka spinner
                        } else {
                            resetTalukaSpinner(); // Reset taluka spinner
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(TradersDetailsList.this, "Failed to load districts: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateTalukaSpinner(String state, String district) {
        LocationData.getTalukas(state, district, new LocationData.Callback<List<String>>() {
            @Override
            public void onSuccess(List<String> talukas) {

                // Add "Taluka" as the first option
                List<String> talukasWithPlaceholder = new ArrayList<>();
                talukasWithPlaceholder.add("Taluka");
                talukasWithPlaceholder.addAll(talukas);

                String[] talukasArray = talukasWithPlaceholder.toArray(new String[0]);
                CustomSpinnerAdapter talukaAdapter = new CustomSpinnerAdapter(TradersDetailsList.this, talukasArray);
                talukaSpinner.setAdapter(talukaAdapter);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(TradersDetailsList.this, "Failed to load talukas: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetDistrictAndTalukaSpinners() {
        // Reset District Spinner
        List<String> districtsPlaceholder = new ArrayList<>();
        districtsPlaceholder.add("District");
        districtSpinner.setAdapter(new CustomSpinnerAdapter(this, districtsPlaceholder.toArray(new String[0])));

        // Reset Taluka Spinner
        resetTalukaSpinner();
    }

    private void resetTalukaSpinner() {
        // Reset Taluka Spinner
        List<String> talukasPlaceholder = new ArrayList<>();
        talukasPlaceholder.add("Taluka");
        talukaSpinner.setAdapter(new CustomSpinnerAdapter(this, talukasPlaceholder.toArray(new String[0])));
    }

    private void setupApplyFilterButton() {
        applyFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyFilters(); // Apply filters when the button is clicked
            }
        });
    }

    private void applyFilters() {
        // Ensure selected items are not null
        String selectedState = stateSpinner.getSelectedItem() != null ? stateSpinner.getSelectedItem().toString() : "State";
        String selectedDistrict = districtSpinner.getSelectedItem() != null ? districtSpinner.getSelectedItem().toString() : "District";
        String selectedTaluka = talukaSpinner.getSelectedItem() != null ? talukaSpinner.getSelectedItem().toString() : "Taluka";

        String stateFilter = selectedState.equals("State") ? intentState : selectedState;
        String districtFilter = selectedDistrict.equals("District") ? intentDistrict : selectedDistrict;
        String talukaFilter = selectedTaluka.equals("Taluka") ? intentTaluka : selectedTaluka;

        // Handle different cases based on selections
        if (selectedState.equals("State") && selectedDistrict.equals("District") && selectedTaluka.equals("Taluka")) {
            // All filters are "Select", pass intent values
            fetchTraders(intentState, intentDistrict, intentTaluka);

        } else if (selectedDistrict.equals("District") && selectedTaluka.equals("Taluka")) {
            // Only state and taluka are not selected
            Toast.makeText(this, "Please select District", Toast.LENGTH_SHORT).show();
        } else if (selectedTaluka.equals("Taluka")) {
            // Only state and taluka are not selected
            Toast.makeText(this, "Please select Taluka", Toast.LENGTH_SHORT).show();
        } else {
            // If all filters are valid, fetch traders based on applied filters
            fetchTraders(stateFilter, districtFilter, talukaFilter);
        }
    }

    private void fetchTraders(String stateFilter, String districtFilter, String talukaFilter) {
        showProgressBar(true);

        FirebaseDatabase.getInstance().getReference("Users")
                .orderByChild("role")
                .equalTo("Trader")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        tradersList = new ArrayList<>();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String state = snapshot.child("state").getValue(String.class);
                            String district = snapshot.child("district").getValue(String.class);
                            String taluka = snapshot.child("taluka").getValue(String.class);

                            if ((stateFilter.equals(state)) &&
                                    (districtFilter.equals(district)) &&
                                    (talukaFilter.equals(taluka))) {

                                Trader trader = new Trader();
                                trader.setTraderName(snapshot.child("firstName").getValue(String.class));
                                trader.setShopName(snapshot.child("shopName").getValue(String.class));
                                trader.setShopAddress(snapshot.child("shopAddress").getValue(String.class));
                                trader.setPhoneNumber(snapshot.child("phoneNumber").getValue(String.class));
                                trader.setUserId(snapshot.child("userId").getValue(String.class));

                                tradersList.add(trader);

                            }
                        }
                        showProgressBar(false);

                        if (tradersList.isEmpty()) {
                            // If no traders are found, display the message
                            textMessageTrader.setText("No trader Found in: " + talukaFilter);
                        } else if (tradersList.size() == 1) {
                            // If traders are found, clear the message
                            textMessageTrader.setText(tradersList.size() + " Trader Found in: " + talukaFilter);
                        } else {
                            // If traders are found, clear the message
                            textMessageTrader.setText(tradersList.size() + " Traders Found: " + talukaFilter);
                        }

                        updateRecyclerView(tradersList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        showProgressBar(false);
                        Toast.makeText(TradersDetailsList.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateRecyclerView(List<Trader> tradersList) {
        RecyclerView recyclerView = findViewById(R.id.traderRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new TraderListAdapter(this, tradersList, intentUserId));
    }

    private void showProgressBar(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
