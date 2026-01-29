package com.firstapp.uber.websocket.controller;

import com.firstapp.uber.auth.CustomUserDetails;
import com.firstapp.uber.dto.driver.DriverRequest;
import com.firstapp.uber.dto.driver.DriverResponse;
import com.firstapp.uber.dto.ride.Ride;
import com.firstapp.uber.service.driver.DriverNotificationService;
import com.firstapp.uber.service.driver.DriverService;
import com.firstapp.uber.service.ride.RideServiceImpl;
import com.firstapp.uber.websocket.registry.WebSocketSessionRegistry;
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
    private final WebSocketSessionRegistry webSocketSessionRegistry;

    public WebSocketController(SimpMessagingTemplate messagingTemplate,
                               RideServiceImpl rideService,
                               DriverNotificationService driverNotificationService,
                               DriverService driverService,
                               WebSocketSessionRegistry webSocketSessionRegistry) {
        this.messagingTemplate = messagingTemplate;
        this.rideService = rideService;
        this.driverNotificationService = driverNotificationService;
        this.driverService = driverService;
        this.webSocketSessionRegistry = webSocketSessionRegistry;
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

            String driverPrincipal =
                    webSocketSessionRegistry.principalName(driverId);

            messagingTemplate.convertAndSendToUser(
                    driverPrincipal,
                    "/queue/ride-status",
                    "You got the ride!"
            );

            driverNotificationService.notifyRideTaken(ride.getRideId(),driverId);
        } catch (Exception e) {

            String principalName = webSocketSessionRegistry.principalName(driverId);

            messagingTemplate.convertAndSendToUser(
                    principalName,
                    "/queue/ride-status",
                    "Ride already assigned"
            );
        }
    }


    public void sendRideRequest(Integer driverId, DriverRequest request) {
        String principalName = webSocketSessionRegistry.principalName(driverId);

        messagingTemplate.convertAndSendToUser(
                principalName,
                "/queue/ride-request",
                request
        );
    }
}

