package com.firstapp.uber.controller.ride;

import com.firstapp.uber.auth.CustomUserDetails;
import com.firstapp.uber.dto.ride.*;
import com.firstapp.uber.repository.ride.RideRepository;
import com.firstapp.uber.service.ride.RideService;
import com.firstapp.uber.ride.dto.RideCardResponse;
import model.PaymentStatus;
import model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.firstapp.uber.ride.PaymentRequest;
import com.firstapp.uber.ride.dto.EtaResponse;
import org.springframework.security.core.Authentication;
import java.util.List;

@RestController
@RequestMapping("api/rides")
public class RideController {
    private final RideService rideService;
    private final RideRepository rideRepository;
    public RideController(RideService rideService, RideRepository rideRepository) {

        this.rideService = rideService;
        this.rideRepository = rideRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CreateRideResponse> createRide(@RequestBody CreateRideRequest req,
                                                         Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        Integer userId = user.getUserId();

        CreateRideResponse resp = rideService.createRide(userId,
                req.pickupLat(), req.pickupLng(),
                req.dropLat(), req.dropLng());

        return ResponseEntity.ok(resp);
    }

    public record StartRideRequest(Integer rideId, String otp) {}
    public record StartRideResponse(boolean success, String message) {}

    public record AssignDriverRequest(Integer driverId) {}

    @PostMapping("/{rideId}/assign-driver")
    public Ride assignDriver(@PathVariable Integer rideId,
                             @RequestBody AssignDriverRequest request) {
        return rideService.assignDriver(rideId, request.driverId());
    }

    @PostMapping("/start")
    public ResponseEntity<StartRideResponse> startRide(@RequestBody StartRideRequest req) {
        boolean ok = rideService.startRide(req.rideId(), req.otp());

        if (!ok) {
            return ResponseEntity
                    .badRequest()
                    .body(new StartRideResponse(false, "Invalid or expired OTP"));
        }

        return ResponseEntity.ok(new StartRideResponse(true, "Ride started successfully"));
    }

    @PostMapping("/{rideId}/end")
    public Ride endRide(@PathVariable Integer rideId) {
        return rideService.endRide(rideId);
    }

    @PostMapping("/{rideId}/cancel")
    public Ride cancelRide(@PathVariable Integer rideId) {
        return rideService.cancelRide(rideId);
    }

    @GetMapping("/current")
    public Ride getCurrentRide(@RequestParam Integer custId) {
        return rideService.getCurrentRide(custId);
    }

    @GetMapping("/driver/current/{rideId}")
    public RideDetail getCurrentRideFromRideId(@PathVariable Integer rideId) {
        long t0 = System.currentTimeMillis();
        System.out.println("✅ controller entered rideId=" + rideId);
        RideDetail rideDetail = rideService.getCurrentRideFromRideId(rideId);
        System.out.println("✅ service returned rideId=" + rideId);
        long ms = System.currentTimeMillis() - t0;
        System.out.println("✅ service returned rideId=" + rideId + " in " + ms + "ms");
        return rideService.getCurrentRideFromRideId(rideId);

    }

    @PostMapping("/{rideId}/payment-success")
    public ResponseEntity<String> markPaymentSuccess(
            @PathVariable Integer rideId,
            @RequestBody PaymentRequest req
    ) {
        PaymentSuccessEvent event = new PaymentSuccessEvent(rideId, req.method(), PaymentStatus.COMPLETED,
                System.currentTimeMillis());
        rideService.publishPaymentCompleted(event);
        return ResponseEntity.accepted().body("Payment event published");
        //return rideService.markPaymentSuccess(rideId, req.method());
    }
    @GetMapping("/{rideId}/eta")
    public EtaResponse getEta(@PathVariable Integer rideId) {
        return rideService.getEta(rideId);
    }

    @GetMapping("/card")
    public RideCardResponse getRideCard(@RequestParam Integer rideId) {
        return rideService.getRideCard(rideId);
    }

    @GetMapping("")
    public List<Ride> getAllRides() {
        return rideRepository.findAll();
    }
}
