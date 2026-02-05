package com.firstapp.uber.service.ride;

import com.firstapp.uber.dto.ride.Ride;
import com.firstapp.uber.repository.ride.RideRepo;
import com.firstapp.uber.repository.ride.RideRepository;
import com.firstapp.uber.service.driverledger.DriverLedgerService;
import com.firstapp.uber.websocket.controller.WebSocketController;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import model.PaymentStatus;
import model.Status;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class PaymentService {
    private final RideRepo rideRepo;
    private final DriverLedgerService ledgerService;
    private final RideRepository rideRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public PaymentService(
            RideRepo rideRepo,
            DriverLedgerService ledgerService,
            RideRepository rideRepository,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.rideRepo = rideRepo;
        this.ledgerService = ledgerService;
        this.rideRepository = rideRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public Optional<Ride> handlePaymentSuccess(Integer rideId, String method) {

        Optional<Ride> optionalRide = rideRepo.findById(rideId);

        if (optionalRide.isEmpty()) {
            log.warn("Payment event received but ride {} not found. Ignoring.", rideId);
            return Optional.empty();
        }

        Ride ride = optionalRide.get();
        if (PaymentStatus.COMPLETED.equals(ride.getPaymentStatus())) {
            return Optional.of(ride);
        }

        ride.setPaymentStatus(PaymentStatus.COMPLETED);
        ride.setPaymentMethod(method);

        ride.setStatus(Status.COMPLETED);
        ride.setEndedOn(LocalDateTime.now());

        rideRepository.save(ride);

        messagingTemplate.convertAndSend(
                "/topic/ride/" + rideId,
                new WebSocketController.RideStatusBroadcast(rideId, "PAYMENT_COMPLETED", ride.getDriverId())
        );


        ledgerService.createLedgerEntry(ride);

        return Optional.of(ride);
    }
}
