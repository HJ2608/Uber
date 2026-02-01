package com.firstapp.uber.service.ride;

import com.firstapp.uber.dto.driver.DriverResponse;
import com.firstapp.uber.dto.ride.*;
import com.firstapp.uber.ride.dto.EtaResponse;
import com.firstapp.uber.ride.dto.RideCardResponse;

import java.util.List;
import java.util.Optional;


public interface RideService {

    public CreateRideResponse createRide(Integer userId,
                                         double pickupLat, double pickupLng,
                                         double dropLat, double dropLng);

    List<Ride> getPendingRidesForDriver(Integer driverId);
    Optional<Ride> getCurrentRideForDriver(Integer driverId);
    public void acceptRide(Integer driverId, Integer rideId);

    public void handleDriverResponse(DriverResponse driverResponse, Integer driverId);

    public boolean startRide(Integer rideId, String otp);

    public Ride assignDriver(Integer rideId, Integer driverId);

    public Ride endRide(Integer rideId);

    public EtaResponse getEta(Integer rideId);

    public Ride cancelRide(Integer rideId);

    public Ride getCurrentRide(Integer custId);

    public RideDetail getCurrentRideFromRideId(Integer rideId);

    public Ride markPaymentSuccess(Integer rideId, String method);

    public RideCardResponse getRideCard(Integer custId);

    public void publishPaymentCompleted(PaymentSuccessEvent event);

    List<Ride> getAllRides();
}