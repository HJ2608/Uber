package com.firstapp.uber.dto.ride;

import java.math.BigDecimal;

public record DriverRequest(
        BigDecimal estimatedFare,
        double pickupLat,
        double pickupLng,
        double dropLat,
        double dropLng,
        Integer distPickup
) {
}
