package com.firstapp.uber.service.ride;

import com.firstapp.uber.dto.ride.Ride;
import com.firstapp.uber.repository.ride.RideRepo;
import com.firstapp.uber.repository.ride.RideRepository;
import com.firstapp.uber.service.driverledger.DriverLedgerService;
import jakarta.transaction.Transactional;
import model.Status;

import java.time.Instant;
import java.time.LocalDateTime;

public class PaymentService {
    private final RideRepo rideRepo;
    private final DriverLedgerService ledgerService;
    private final RideRepository rideRepository;

    public PaymentService(
            RideRepo rideRepo,
            DriverLedgerService ledgerService,
            RideRepository rideRepository
    ) {
        this.rideRepo = rideRepo;
        this.ledgerService = ledgerService;
        this.rideRepository = rideRepository;
    }

    @Transactional
    public Ride handlePaymentSuccess(Integer rideId, String method) {

        Ride ride = rideRepo.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        if ("COMPLETED".equals(ride.getStatus())) {
            return ride;
        }

        if ("SUCCESS".equals(ride.getPaymentStatus())) {
            return ride;
        }

        ride.setPaymentStatus("SUCCESS");
        ride.setPaymentMethod(method);

        ride.setStatus(Status.COMPLETED);
        ride.setEndedOn(LocalDateTime.now());

        rideRepository.save(ride);

        ledgerService.createLedgerEntry(ride);

        return ride;
    }
}
