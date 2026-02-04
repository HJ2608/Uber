package com.firstapp.uber.websocket.controller;

import com.firstapp.uber.auth.CustomUserDetails;
import com.firstapp.uber.dto.driver.DriverRequest;
import com.firstapp.uber.dto.driver.DriverResponse;
import com.firstapp.uber.dto.driverlocation.RideLocationUpdate;
import com.firstapp.uber.dto.ride.Ride;
import com.firstapp.uber.repository.driverlocation.DriverLocationRedisRepo;
import com.firstapp.uber.repository.ride.RideRepository;
import com.firstapp.uber.service.driver.DriverNotificationService;
import com.firstapp.uber.service.driver.DriverService;
import com.firstapp.uber.service.ride.RideServiceImpl;
import com.firstapp.uber.websocket.registry.WebSocketSessionRegistry;
import model.Status;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
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
    private final DriverLocationRedisRepo driverLocationRedisRepo;
    private final RideRepository rideRepository;

    public WebSocketController(SimpMessagingTemplate messagingTemplate,
                               RideServiceImpl rideService,
                               DriverNotificationService driverNotificationService,
                               DriverService driverService,
                               WebSocketSessionRegistry webSocketSessionRegistry,
                               DriverLocationRedisRepo driverLocationRedisRepo,
                               RideRepository rideRepository) {
        this.messagingTemplate = messagingTemplate;
        this.rideService = rideService;
        this.driverNotificationService = driverNotificationService;
        this.driverService = driverService;
        this.webSocketSessionRegistry = webSocketSessionRegistry;
        this.driverLocationRedisRepo = driverLocationRedisRepo;
        this.rideRepository = rideRepository;
    }


    @MessageMapping("/driver/ride/response")
    public void driverResponse(DriverResponse response, Principal principal) {
        System.out.println("driverResponse is called!! response=" + response);

        System.out.println("accepted=" + response.accepted() + " rideId=" + response.rideId());

        if (!response.accepted()){
            System.out.println("RETURNING because accepted=false");
            return;
        }

//        Authentication auth = (Authentication) principal;
//        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
//
//        if (!"DRIVER".equals(user.getRole())) return;
//
//        Integer userId = user.getUserId();
//        Integer driverId = driverService.findDriverIdByUserId(userId);

        String phone = principal.getName();
        Integer driverId = driverService.findDriverIdByPhone(phone);

        if (!driverService.isDriverPhone(phone)) return;

        try {
            System.out.println("Inside try block of driverResponse");
            Ride ride = rideService.handleDriverResponse(response,driverId);

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

    @MessageMapping("/ride/location")
    public void rideLocation(@Payload RideLocationUpdate update, Principal principal) {

        String phone = principal.getName();
        Integer driverId = driverService.findDriverIdByPhone(phone);

        if (update.lat() == null || update.lng() == null) {
            return;
        }

        if (driverId == null) {
            return;
        }

        driverLocationRedisRepo.upsertLocation(driverId, update.lat(), update.lng());



        Ride ride = rideRepository.findTopByDriverIdAndStatusOrderByStartedOnDesc(driverId, Status.ONGOING)
                .orElse(null);

        if(ride == null){
            System.out.println("ride is null, no broadcast yet ");
            return;
        }

        long ts = (update.timestamp() != null) ? update.timestamp() : System.currentTimeMillis();

        messagingTemplate.convertAndSend(
                "/topic/ride/" + ride.getRideId() + "/location",
                new RideLocationBroadcast(driverId, update.lat(), update.lng(), ts)
        );
        System.out.println("rideLocation is called!! rideId=" + ride.getRideId());
        System.out.println("rideLocation published to path /topic/ride/" + ride.getRideId() + "/location");
    }

    public record RideLocationBroadcast(Integer driverId, Double lat, Double lng, Long timestamp) {}

}

