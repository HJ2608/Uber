package com.firstapp.uber.dto.driverlocation;

public record DriverLocationWsMessage(
        Integer rideId,
        Integer driverId,
        double lat,
        double lng,
        long timestamp
) {}
