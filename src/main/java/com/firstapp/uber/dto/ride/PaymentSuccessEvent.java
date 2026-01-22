package com.firstapp.uber.dto.ride;

import model.PaymentStatus;

public record PaymentSuccessEvent(
        Integer rideId,
        String method,
        PaymentStatus paymentStatus,
        Long timestamp
) {
}
