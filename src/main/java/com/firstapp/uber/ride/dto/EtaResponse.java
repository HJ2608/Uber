package com.firstapp.uber.ride.dto;

public record EtaResponse(
        double distanceKm,
        int etaMinutes
) {
}
