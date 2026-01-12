package com.firstapp.uber.service.ride;

import com.firstapp.uber.dto.driver.DriverResponse;
import com.firstapp.uber.dto.ride.CreateRideRequest;
import com.firstapp.uber.dto.ride.CreateRideResponse;
import com.firstapp.uber.dto.ride.Ride;
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

    public boolean startRide(Integer userId, String otpCode);

    public Ride assignDriver(Integer rideId, Integer driverId);

    public Ride endRide(Integer rideId);

    public EtaResponse getEta(Integer rideId);

    public Ride cancelRide(Integer rideId);

    public Ride getCurrentRide(Integer custId);

    public Ride markPaymentSuccess(Integer rideId, String method);

    public RideCardResponse getRideCard(Integer custId);

    List<Ride> getAllRides();
}