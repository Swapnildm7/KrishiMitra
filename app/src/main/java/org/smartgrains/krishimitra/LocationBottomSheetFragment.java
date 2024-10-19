package org.smartgrains.krishimitra;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

public class LocationBottomSheetFragment extends BottomSheetDialogFragment {

    private Spinner stateSpinner, districtSpinner, talukaSpinner;
    private Button selectButton;

    // Define the listener interface
    private OnLocationSelectedListener listener;

    // Interface for location selection callback
    public interface OnLocationSelectedListener {
        void onLocationSelected(String state, String district, String taluka);
    }

    // Method to set the listener
    public void setOnLocationSelectedListener(OnLocationSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_location, container, false);

        stateSpinner = view.findViewById(R.id.stateSpinner);
        districtSpinner = view.findViewById(R.id.districtSpinner);
        talukaSpinner = view.findViewById(R.id.talukaSpinner);
        selectButton = view.findViewById(R.id.selectButton);

        populateStateSpinner();

        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedState = (String) parent.getItemAtPosition(position);
                populateDistrictSpinner(selectedState);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle case when nothing is selected
            }
        });

        districtSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDistrict = (String) parent.getItemAtPosition(position);
                populateTalukaSpinner(selectedDistrict);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle case when nothing is selected
            }
        });

        selectButton.setOnClickListener(v -> {
            if (isSelectionValid()) {
                String selectedState = stateSpinner.getSelectedItem().toString();
                String selectedDistrict = districtSpinner.getSelectedItem().toString();
                String selectedTaluka = talukaSpinner.getSelectedItem().toString();

                // Notify the listener about the selected location
                if (listener != null) {
                    listener.onLocationSelected(selectedState, selectedDistrict, selectedTaluka);
                }

                dismiss(); // Close the bottom sheet
            } else {
                Toast.makeText(getContext(), "Please select State, District, and Taluka", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void populateStateSpinner() {
        // Populate states spinner
        List<String> states = LocationData.getStates(); // Retrieve states from your LocationData
        String[] statesArray = states.toArray(new String[0]); // Convert List to String[]
        CustomSpinnerAdapter stateAdapter = new CustomSpinnerAdapter(getContext(), statesArray);
        stateSpinner.setAdapter(stateAdapter);
    }

    private void populateDistrictSpinner(String state) {
        List<String> districts = LocationData.getDistricts(state); // Retrieve districts by state
        String[] districtsArray = districts.toArray(new String[0]); // Convert List to String[]
        CustomSpinnerAdapter districtAdapter = new CustomSpinnerAdapter(getContext(), districtsArray);
        districtSpinner.setAdapter(districtAdapter);
        talukaSpinner.setAdapter(null); // Clear taluka spinner when district changes
    }

    private void populateTalukaSpinner(String district) {
        List<String> talukas = LocationData.getTalukas(district); // Retrieve talukas by district
        String[] talukasArray = talukas.toArray(new String[0]); // Convert List to String[]
        CustomSpinnerAdapter talukaAdapter = new CustomSpinnerAdapter(getContext(), talukasArray);
        talukaSpinner.setAdapter(talukaAdapter);
    }

    private boolean isSelectionValid() {
        return stateSpinner.getSelectedItem() != null &&
                districtSpinner.getSelectedItem() != null &&
                talukaSpinner.getSelectedItem() != null;
    }
}
