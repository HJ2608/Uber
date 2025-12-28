package com.firstapp.uber.repository.ride;

import com.firstapp.uber.dto.ride.Ride;
import jakarta.transaction.Transactional;
import model.Status;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


@Repository
public class RideRepo {

    private final RideRepository rideRepository;

    public RideRepo(RideRepository rideRepository) {
        this.rideRepository = rideRepository;
    }

    @Transactional
    public Ride createRide(
            Integer custId,
            double pickupLat,
            double pickupLng,
            double dropLat,
            double dropLng,
            BigDecimal estimatedFare,
            Integer otpId
    ) {
        Ride ride = new Ride();

        ride.setCustId(custId);
        ride.setPickupLat(pickupLat);
        ride.setPickupLng(pickupLng);
        ride.setDropLat(dropLat);
        ride.setDropLng(dropLng);
        ride.setEstimatedFare(estimatedFare);
        ride.setOtpId(otpId);

        ride.setStatus(Status.REQUESTED);
        ride.setOtpVerified(false);
        ride.setFinalFare(null);
        ride.setStartedOn(null);
        ride.setEndedOn(null);
        ride.setPaymentStatus("PENDING");
        ride.setPaymentMethod("CASH");

        return rideRepository.save(ride);
    }

    @Transactional
    public Ride assignDriver(Integer rideId, Integer driverId) {
//        List<Status> activeStatuses = List.of(
//                Status.ASSIGNED,
//                Status.ONGOING
//        );
//
//        if (rideRepository.existsByDriverIdAndStatusIn(driverId, activeStatuses)) {
//            throw new ResponseStatusException(
//                    HttpStatus.CONFLICT,
//                    "Driver already has an active ride"
//            );
//        }
//        Ride ride = rideRepository.findById(rideId)
//                .orElseThrow(() -> new IllegalArgumentException("Ride not found with id: " + rideId));
//        ride.setDriverId(driverId);
//        ride.setStatus(Status.ASSIGNED);

        int updated = rideRepository.assignDriverIfFree(rideId, driverId);

        if(updated == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ride already accepted by another driver");
        }
        return rideRepository.findById(rideId)
                .orElseThrow();

        //return rideRepository.save(ride);
    }

    @Transactional
    public int startRide(Integer otpId) {
        return rideRepository.startRide(otpId, Status.ONGOING);
    }

    public Optional<Ride> findById(Integer rideId) {
        return rideRepository.findById(rideId);
    }

    public Ride endRide(Integer rideId) {

        int rows = rideRepository.markRideCompleted(rideId, Status.COMPLETED);

        if (rows == 0) {
            throw new RuntimeException("Ride not found with id " + rideId);
        }
        return rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found after update"));
    }

    @Transactional
    public Ride cancelRide(Integer rideId) {
        int rows = rideRepository.markRideCancelled(rideId, Status.CANCELLED);

        if (rows == 0) {
            throw new RuntimeException("Ride not found with id " + rideId);
        }

        return rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found after cancel: " + rideId));
    }

    public Optional<Ride> getCurrentRide(Integer custId) {

        List<String> activeStatuses = List.of(
                "REQUESTED",
                "WAITING",
                "ONGOING",
                "ASSIGNED"
        );

        return rideRepository.findTopByCustIdAndStatusInOrderByRideIdDesc(
                custId,
                activeStatuses
        );
    }

    @Transactional
    public Ride updatePayment(Integer rideId, String method) {

        int rows = rideRepository.updatePayment(rideId, method);

        if (rows == 0) {
            throw new RuntimeException("Ride not found with id " + rideId);
        }

        return rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found after payment update"));
    }

    public List<Ride> findAll(){
        return rideRepository.findAll();
    }


}
