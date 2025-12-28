package com.firstapp.uber.service.ride;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RideRequestCache {

    private final Map<Integer, List<Integer>> rideToDrivers = new ConcurrentHashMap<>();

    public void put(Integer rideId, List<Integer> driverIds) {
        rideToDrivers.put(rideId, driverIds);
    }

    public List<Integer> get(Integer rideId) {
        return rideToDrivers.getOrDefault(rideId, List.of());
    }

    public void remove(Integer rideId) {
        rideToDrivers.remove(rideId);
    }
}
