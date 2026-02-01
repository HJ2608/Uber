package com.firstapp.uber.dto.ride;

import java.math.BigDecimal;

public record RideDetail(
        Integer rideId,
        String riderName,
        String riderMobile,
        Double pickupLat,
        Double pickupLng,
        Double dropLat,
        Double dropLng,
        BigDecimal estimatedFare
) {
}
