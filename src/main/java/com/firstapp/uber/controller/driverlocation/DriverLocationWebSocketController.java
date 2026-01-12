package com.firstapp.uber.controller.driverlocation;

import com.firstapp.uber.dto.driverlocation.DriverLocationWsMessage;
import com.firstapp.uber.repository.ride.RideRepo;
import com.firstapp.uber.repository.ride.RideRepository;
import com.firstapp.uber.service.driverlocation.DriverLocationService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import com.firstapp.uber.auth.CustomUserDetails;
import java.security.Principal;

@Controller
public class DriverLocationWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final DriverLocationService locationService;
    private final RideRepo rideRepo;
    private final RideRepository rideRepository;

    public DriverLocationWebSocketController(
            SimpMessagingTemplate messagingTemplate,
            DriverLocationService locationService,
            RideRepo rideRepo,
            RideRepository rideRepository
    ) {
        this.messagingTemplate = messagingTemplate;
        this.locationService = locationService;
        this.rideRepo = rideRepo;
        this.rideRepository = rideRepository;
    }

    @MessageMapping("/driver/location")
    public void handleLocationUpdate(DriverLocationWsMessage msg, Principal principal) {

        if (!(principal instanceof Authentication authentication)) {
            return;
        }

        CustomUserDetails user =
                (CustomUserDetails) authentication.getPrincipal();

        Integer driverUserId = user.getUserId();

        locationService.updateLocation(
                driverUserId,
                msg.lat(),
                msg.lng()
        );

        rideRepository.findActiveRideByDriverId(driverUserId)
                .ifPresent(ride -> {
                    messagingTemplate.convertAndSend(
                            "/topic/ride/" + ride.getRideId() + "/location",
                            msg
                    );
                });
    }
}
