package com.firstapp.uber.dto.ride;

import model.PaymentStatus;

public record PaymentEvent(
        Integer rideId,
        String paymentMethod,
        PaymentStatus status,
        Long occurredAt
) {
}
