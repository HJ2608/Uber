package com.firstapp.uber.dto.ride;

public record CreateRideRequest(
        double pickupLat,
        double pickupLng,
        double dropLat,
        double dropLng
) {
}
