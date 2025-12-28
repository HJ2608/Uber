package com.firstapp.uber.controller.driverlocation;

import com.firstapp.uber.dto.driverlocation.DriverLocationWsMessage;
import com.firstapp.uber.repository.ride.RideRepo;
import com.firstapp.uber.service.driverlocation.DriverLocationService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class DriverLocationWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final DriverLocationService locationService;
    private final RideRepo rideRepo;

    public DriverLocationWebSocketController(
            SimpMessagingTemplate messagingTemplate,
            DriverLocationService locationService,
            RideRepo rideRepo
    ) {
        this.messagingTemplate = messagingTemplate;
        this.locationService = locationService;
        this.rideRepo = rideRepo;
    }

    @MessageMapping("/driver/location")
    public void handleLocationUpdate(DriverLocationWsMessage msg) {

        var rideOpt = rideRepo.findById(msg.rideId());
        if (rideOpt.isEmpty()) return;

        var ride = rideOpt.get();

        if (!msg.driverId().equals(ride.getDriverId())) {
            return;
        }

        locationService.updateLocation(
                msg.driverId(),
                msg.lat(),
                msg.lng()
        );

        messagingTemplate.convertAndSend(
                "/topic/ride/" + msg.rideId() + "/location",
                msg
        );
    }
}
