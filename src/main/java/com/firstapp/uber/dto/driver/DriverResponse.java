package com.firstapp.uber.dto.driver;

public record DriverResponse(
        Integer rideId,
        Integer driverId,
        boolean accepted
) {}
