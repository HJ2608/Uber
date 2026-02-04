package com.firstapp.uber.dto.driverlocation;

public record RideLocationUpdate(
        Double lat,
        Double lng,
        Long timestamp
) {
}
