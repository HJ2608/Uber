package com.firstapp.uber.ride.dto;

import model.Status;

import java.math.BigDecimal;

public record RideCardResponse(
        Integer rideId,
        Status status,
        BigDecimal estimatedFare,
        BigDecimal finalFare,
        String paymentStatus,
        String paymentMethod,
        double pickupLat,
        double pickupLng,
        double dropLat,
        double dropLng,
        String otpCode,
        DriverSummary driver,
        CabSummary cab,
        EtaResponse eta
) {
}

