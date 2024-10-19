package org.smartgrains.krishimitra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationData {

    // Hardcoded states
    public static List<String> getStates() {
        List<String> states = new ArrayList<>();
        states.add("Maharashtra");
        states.add("Karnataka");
        return states;
    }

    // Hardcoded districts for a given state
    public static List<String> getDistricts(String state) {
        Map<String, List<String>> districtMap = getDistrictMap();
        return districtMap.getOrDefault(state, new ArrayList<>());
    }

    // Hardcoded talukas for a given district
    public static List<String> getTalukas(String district) {
        Map<String, List<String>> talukaMap = getTalukaMap();
        return talukaMap.getOrDefault(district, new ArrayList<>());
    }

    // Hardcoded districts map
    public static Map<String, List<String>> getDistrictMap() {
        Map<String, List<String>> districtMap = new HashMap<>();
        districtMap.put("Maharashtra", List.of("Latur")); // Added more example districts
        districtMap.put("Karnataka", List.of("Bidar")); // Added more example districts
        return districtMap;
    }

    // Hardcoded talukas map
    public static Map<String, List<String>> getTalukaMap() {
        Map<String, List<String>> talukaMap = new HashMap<>();
        talukaMap.put("Latur", List.of("Latur","Udgir"));
        talukaMap.put("Bidar", List.of("Aurad", "Bidar", "Bhalki"));
        return talukaMap;
    }
}
