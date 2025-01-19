package org.smartgrains.krishimitra;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LocationData {

    private static DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("LocationData");

    // Fetch states dynamically
    public static void getStates(Callback<List<String>> callback) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<String> states = new ArrayList<>();
                for (DataSnapshot stateSnapshot : snapshot.getChildren()) {
                    states.add(stateSnapshot.getKey());
                }
                callback.onSuccess(states);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        });
    }

    // Fetch districts for a given state dynamically
    public static void getDistricts(String state, Callback<List<String>> callback) {
        databaseReference.child(state).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<String> districts = new ArrayList<>();
                for (DataSnapshot districtSnapshot : snapshot.getChildren()) {
                    districts.add(districtSnapshot.getKey());
                }
                callback.onSuccess(districts);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        });
    }

    // Fetch talukas for a given district dynamically
    public static void getTalukas(String state, String district, Callback<List<String>> callback) {
        databaseReference.child(state).child(district).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<String> talukas = new ArrayList<>();
                for (DataSnapshot talukaSnapshot : snapshot.getChildren()) {
                    talukas.add(talukaSnapshot.getValue(String.class));
                }
                callback.onSuccess(talukas);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        });
    }

    // Callback interface for async data fetching
    public interface Callback<T> {
        void onSuccess(T result);

        void onFailure(String error);
    }
}
