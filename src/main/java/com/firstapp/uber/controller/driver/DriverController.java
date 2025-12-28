package com.firstapp.uber.controller.driver;


import com.firstapp.uber.dto.ride.Ride;
import com.firstapp.uber.repository.driver.DriverRepo;
import com.firstapp.uber.dto.driver.Driver;
import com.firstapp.uber.service.ride.RideService;
import com.firstapp.uber.service.ride.RideServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/drivers")
public class DriverController {
    private final DriverRepo repo;
    private final RideService rideService;

    public DriverController(DriverRepo repo, RideService rideService) {
        this.repo = repo;
        this.rideService = rideService;
    }
    @GetMapping
    public List<Driver> getAllDriver(){
        return repo.findAll();
    }
    @GetMapping("/id/{id}")
    public Driver getDriverById(@PathVariable Integer id){
        return repo.findById(id).orElseThrow();
    }
    @PostMapping()
    public void createDriver(@RequestBody Driver driver){
        repo.createDriver(driver);
    }
    @PutMapping("/id/{id}")
    public boolean updateDriver(@RequestBody Driver driver, @PathVariable Integer id){
        return repo.update(id,driver);
    }
    @DeleteMapping("/id/{id}")
    public void deleteDriver(@PathVariable Integer id){
        repo.deleteDriver(id);
    }

    @PostMapping("/{driverId}/accept/{rideId}")
    public ResponseEntity<?> acceptRide(
            @PathVariable Integer driverId,
            @PathVariable Integer rideId
    ) {
        rideService.acceptRide(driverId, rideId);
        return ResponseEntity.ok("Ride accepted");
    }

    @GetMapping("/{driverId}/home")
    public ResponseEntity<?> getDriverHome(@PathVariable Integer driverId) {
        Driver driver = repo.findById(driverId).orElseThrow(() ->
                new RuntimeException("Driver not found with id " + driverId)
        );
        var pendingRides = rideService.getPendingRidesForDriver(driverId);
        var currentRide = rideService.getCurrentRideForDriver(driverId);
        var response = Map.of(
                "driver", driver,
                "pendingRides", pendingRides,
                "currentRide", currentRide
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{driverId}/status")
    public ResponseEntity<?> setDriverStatus(
            @PathVariable Integer driverId,
            @RequestParam boolean isOnline
    ) {
        repo.setDriverOnlineStatus(driverId, isOnline);
        return ResponseEntity.ok(Map.of("message", "Status updated"));
    }

    @GetMapping("/{driverId}/home")
    public ResponseEntity<?> getDriverHome(@PathVariable Integer driverId) {
        Driver driver = repo.findById(driverId).orElseThrow();

        // Check if driver is online
        if (!driver.getIsOnline()) {
            return ResponseEntity.ok("Driver is offline");
        }

        // Get pending rides near driver
        List<Ride> nearbyRides = rideService.getNearbyRides(driverId);

        // Get current ongoing ride
        Optional<Ride> currentRide = rideService.getCurrentRideForDriver(driverId);

        return ResponseEntity.ok(Map.of(
                "currentRide", currentRide.orElse(null),
                "nearbyRides", nearbyRides
        ));
    }

}
