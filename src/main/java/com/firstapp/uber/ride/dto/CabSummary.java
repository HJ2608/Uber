package com.firstapp.uber.ride.dto;

import model.CabType;

public record CabSummary(
        Integer cabId,
        String model,
        String color,
        String registrationNo,
        CabType cabType
) {
}
