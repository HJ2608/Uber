package com.firstapp.uber.dto.ride;

import java.math.BigDecimal;

public record CreateRideResponse(
        Integer rideId,
        BigDecimal estimatedFare,
        String otpCode
) {}
