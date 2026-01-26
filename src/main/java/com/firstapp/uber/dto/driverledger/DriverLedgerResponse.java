package com.firstapp.uber.dto.driverledger;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DriverLedgerResponse(
        Long id,
        Integer rideId,
        BigDecimal totalFare,
        BigDecimal driverCut,
        BigDecimal companyCut,
        LocalDateTime createdAt
) {}
