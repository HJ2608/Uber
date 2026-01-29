package com.firstapp.uber.dto.driver;

import jakarta.persistence.criteria.CriteriaBuilder;

import java.math.BigDecimal;

public record DriverRequest(
        Integer rideId,
        BigDecimal estimatedFare,
        double pickupLat,
        double pickupLng,
        double dropLat,
        double dropLng
) {}
