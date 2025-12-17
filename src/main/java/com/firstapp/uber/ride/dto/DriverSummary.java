package com.firstapp.uber.ride.dto;

public record DriverSummary(
        Integer driverId,
        String name,
        Double avgRating
) {
}
