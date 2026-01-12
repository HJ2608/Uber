package com.firstapp.uber.service.ride;

import com.sun.source.doctree.SeeTree;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RideRequestCache {

    private final Map<Integer, Set<Integer>> rideSubscribers = new ConcurrentHashMap<>();

    public void addSubscriber(Integer rideId, Integer driverId) {
        rideSubscribers
                .computeIfAbsent(rideId, k -> ConcurrentHashMap.newKeySet())
                .add(driverId);
    }

    public void put(Integer rideId, List<Integer> driverIds) {
        rideSubscribers.put(rideId,
                ConcurrentHashMap.newKeySet(driverIds.size())
        );
        rideSubscribers.get(rideId).addAll(driverIds);
    }

    public List<Integer> get(Integer rideId) {
        return new ArrayList<>(
                rideSubscribers.getOrDefault(rideId, Set.of())
        );
    }

    public void remove(Integer rideId) {
        rideSubscribers.remove(rideId);
    }
}
