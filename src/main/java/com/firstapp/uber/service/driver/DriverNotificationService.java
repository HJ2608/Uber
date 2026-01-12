package com.firstapp.uber.service.driver;

import com.firstapp.uber.dto.driver.DriverRequest;
import com.firstapp.uber.dto.driver.DriverResponse;
import com.firstapp.uber.dto.ride.Ride;
import com.firstapp.uber.repository.driver.DriverRepo;
import com.firstapp.uber.repository.driver.DriverRepository;
import com.firstapp.uber.repository.ride.RideRepo;
import com.firstapp.uber.service.otp.OtpService;
import com.firstapp.uber.service.ride.RideRequestCache;
import com.firstapp.uber.service.ride.RideServiceImpl;
import jakarta.transaction.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DriverNotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final DriverRepo driverRepository;
    private final RideRepo rideRepository;
        private final RideRequestCache rideRequestCache;

    public DriverNotificationService(SimpMessagingTemplate messagingTemplate,
                                     DriverRepo driverRepository,
                                     RideRepo rideRepository,
                                     RideRequestCache rideRequestCache) {
        this.messagingTemplate = messagingTemplate;
        this.driverRepository = driverRepository;
        this.rideRepository = rideRepository;
        this.rideRequestCache = rideRequestCache;
    }

    @Transactional
    public void setDriverOnline(Integer driverId) {
        driverRepository.setOnline(driverId);
    }

    @Transactional
    public void setDriverOffline(Integer driverId) {
        driverRepository.setOffline(driverId);
    }

    public void sendRideRequest(Integer driverId, DriverRequest request) {
        messagingTemplate.convertAndSendToUser(
                driverId.toString(),
                "/queue/ride-request",
                request
        );
    }

    public void notifyRideTaken(Integer rideId, Integer acceptedDriverId) {
        List<Integer> alertedDrivers = rideRequestCache.get(rideId);
        alertedDrivers.forEach(driverId -> {
            if (!driverId.equals(acceptedDriverId)) {
                messagingTemplate.convertAndSendToUser(
                        driverId.toString(),
                        "/queue/ride-cancelled",
                        "Ride already accepted"
                );
            }
        });
        rideRequestCache.remove(rideId);
    }

    //public Ride getCurrentRideForDriver(Integer driverId) {
        //return rideServiceImpl.getActiveRideForDriver(driverId);
    //}

//    public void handleDriverResponse(DriverResponse driverResponse, Integer driverId) {
//        try {
//            Ride ride = rideServiceImpl.assignDriver(driverResponse.rideId(), driverId);
//            notifyRideAssignment(ride, driverId);
//        } catch (Exception e) {
//            notifyRideAssignmentFailed(driverId);
//        }
//    }

    public void notifyRideAssignment(Ride ride, Integer driverId) {
        messagingTemplate.convertAndSend("/queue/ride-status" + ride.getCustId(), ride);
        messagingTemplate.convertAndSend("/queue/ride-status" + driverId, "You got the ride!");
        notifyRideTaken(ride.getRideId(), driverId);
    }

    public void notifyRideAssignmentFailed(Integer driverId) {
        messagingTemplate.convertAndSend("/queue/ride-status" + driverId, "Ride already assigned");
    }

}

