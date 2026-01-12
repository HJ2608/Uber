package com.firstapp.uber.websocket.controller;

import com.firstapp.uber.auth.CustomUserDetails;
import com.firstapp.uber.dto.driver.DriverRequest;
import com.firstapp.uber.dto.driver.DriverResponse;
import com.firstapp.uber.dto.ride.Ride;
import com.firstapp.uber.service.driver.DriverNotificationService;
import com.firstapp.uber.service.driver.DriverService;
import com.firstapp.uber.service.ride.RideServiceImpl;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final RideServiceImpl rideService;
    private final DriverNotificationService driverNotificationService;
    private final DriverService driverService;

    public WebSocketController(SimpMessagingTemplate messagingTemplate,
                               RideServiceImpl rideService,
                               DriverNotificationService driverNotificationService,
                               DriverService driverService) {
        this.messagingTemplate = messagingTemplate;
        this.rideService = rideService;
        this.driverNotificationService = driverNotificationService;
        this.driverService = driverService;
    }


    @MessageMapping("/driver/ride/response")
    public void driverResponse(DriverResponse response, Principal principal) {
        if (!response.accepted()) return;

        Authentication auth = (Authentication) principal;
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();

        if (!"DRIVER".equals(user.getRole())) return;

        Integer userId = user.getUserId();
        Integer driverId = driverService.findDriverIdByUserId(userId);

        rideService.handleDriverResponse(response,driverId);
        try {

            Ride ride = rideService.assignDriver(response.rideId(), response.driverId());

            messagingTemplate.convertAndSend("/queue/ride-status" + ride.getCustId(), ride);

            messagingTemplate.convertAndSend("/queue/ride-status" + driverId.toString(), "You got the ride!");

            driverNotificationService.notifyRideTaken(ride.getRideId(),driverId);
        } catch (Exception e) {

            messagingTemplate.convertAndSend("/queue/ride-status" + driverId.toString(), "Ride already assigned");
        }
    }


    public void sendRideRequest(Integer driverId, DriverRequest request) {
        messagingTemplate.convertAndSend("/queue/ride-requests" + driverId.toString(), request);
    }
}

