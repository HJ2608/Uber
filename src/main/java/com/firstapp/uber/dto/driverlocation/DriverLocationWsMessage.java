package com.firstapp.uber.dto.driverlocation;

public record DriverLocationWsMessage(
        double lat,
        double lng,
        long timestamp
) {}
