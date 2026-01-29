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
import com.firstapp.uber.websocket.registry.WebSocketSessionRegistry;
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
        private final WebSocketSessionRegistry webSocketSessionRegistry;

    public DriverNotificationService(SimpMessagingTemplate messagingTemplate,
                                     DriverRepo driverRepository,
                                     RideRepo rideRepository,
                                     RideRequestCache rideRequestCache,
                                     WebSocketSessionRegistry webSocketSessionRegistry) {
        this.messagingTemplate = messagingTemplate;
        this.driverRepository = driverRepository;
        this.rideRepository = rideRepository;
        this.rideRequestCache = rideRequestCache;
        this.webSocketSessionRegistry = webSocketSessionRegistry;
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
        System.out.println("Sending ride request to driverId=" + driverId);
        messagingTemplate.convertAndSend(
                "/topic/ride-request-test",
                "WS_TEST_BROADCAST: sendRideRequest() called for driverId=" + driverId
        );
        System.out.println("✅ Sent BROADCAST to /topic/ride-request-test for driverId=" + driverId);

        String principalName = webSocketSessionRegistry.principalName(driverId);
        System.out.println(
                "WS SEND → principal=" + principalName +
                        " destination=/user/queue/ride-request payload=" + request
        );
        System.out.println("Sending ride request to driverId=" + driverId
                + " principal=" + principalName);
        messagingTemplate.convertAndSendToUser(
                principalName,
                "/queue/ride-request",
                request
        );
        System.out.println(
                "Sending WS message to user=[" + principalName + "] destination=/queue/ride-request"
        );
        System.out.println("Published to /user" + "/queue/ride-request");

    }

    public void notifyRideTaken(Integer rideId, Integer acceptedDriverId) {
        List<Integer> alertedDrivers = rideRequestCache.get(rideId);
        alertedDrivers.forEach(driverId -> {
            if (!driverId.equals(acceptedDriverId)) {
                String principalName = webSocketSessionRegistry.principalName(driverId);
                messagingTemplate.convertAndSendToUser(
                        principalName,
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
        messagingTemplate.convertAndSend("/queue/ride-status", ride);
        messagingTemplate.convertAndSend("/queue/ride-status", "You got the ride!");
        notifyRideTaken(ride.getRideId(), driverId);
    }

    public void notifyRideAssignmentFailed(Integer driverId) {
        String principalName = webSocketSessionRegistry.principalName(driverId);

        messagingTemplate.convertAndSendToUser(
                principalName,
                "/queue/ride-status",
                "Ride already assigned"
        );
    }

}

