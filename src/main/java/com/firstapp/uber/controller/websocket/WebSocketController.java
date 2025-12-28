package com.firstapp.uber.controller.websocket;

import com.firstapp.uber.dto.driver.DriverRequest;
import com.firstapp.uber.dto.driver.DriverResponse;
import com.firstapp.uber.service.ride.RideServiceImpl;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final RideServiceImpl rideService;

    public WebSocketController(SimpMessagingTemplate messagingTemplate, RideServiceImpl rideService) {
        this.messagingTemplate = messagingTemplate;
        this.rideService = rideService;
    }


    @MessageMapping("/driver/response")
    public void driverResponse(DriverResponse response) {
        if (!response.accepted()) return;

        try {

            var ride = rideService.assignDriver(response.rideId(), response.driverId());

            messagingTemplate.convertAndSend("/topic/rider/" + ride.getCustId(), ride);

            messagingTemplate.convertAndSend("/topic/driver/" + response.driverId(), "You got the ride!");
        } catch (Exception e) {

            messagingTemplate.convertAndSend("/topic/driver/" + response.driverId(), "Ride already assigned");
        }
    }


    public void sendRideRequest(Integer driverId, DriverRequest request) {
        messagingTemplate.convertAndSend("/topic/driver/" + driverId, request);
    }
}

