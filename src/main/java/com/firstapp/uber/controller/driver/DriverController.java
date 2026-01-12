package com.firstapp.uber.controller.driver;


import com.firstapp.uber.dto.driver.DriverCreateRequest;
import com.firstapp.uber.dto.ride.Ride;
import com.firstapp.uber.repository.driver.DriverRepo;
import com.firstapp.uber.dto.driver.Driver;
import com.firstapp.uber.repository.driverlocation.DriverLocationRepo;
import com.firstapp.uber.service.ride.RideService;
import com.firstapp.uber.service.ride.RideServiceImpl;
import model.DriverStatus;
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
    private final DriverLocationRepo driverLocationRepo;

    public DriverController(DriverRepo repo, RideService rideService,  DriverLocationRepo driverLocationRepo) {
        this.repo = repo;
        this.rideService = rideService;
        this.driverLocationRepo = driverLocationRepo;
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
    public void createDriver(@RequestBody DriverCreateRequest driver){
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

//    @GetMapping("/{driverId}/home")
//    public ResponseEntity<?> getDriverHome(@PathVariable Integer driverId) {
//        Driver driver = repo.findById(driverId).orElseThrow(() ->
//                new RuntimeException("Driver not found with id " + driverId)
//        );
//       var pendingRides = rideService.getPendingRidesForDriver(driverId);
//        var currentRide = rideService.getCurrentRideForDriver(driverId);
//        var response = Map.of(
//                "driver", driver,
//                "pendingRides", pendingRides,
//                "currentRide", currentRide
//        );
//
//        return ResponseEntity.ok(response);
//    }

    @PostMapping("/{driverId}/status")
    public ResponseEntity<?> setDriverStatus(
            @PathVariable Integer driverId,
            @RequestParam DriverStatus status
    ) {
        repo.updateDriverStatus(driverId, status);
        return ResponseEntity.ok(Map.of(
                "message", "Status updated",
                "status", status
        ));
    }

    @GetMapping("/{driverId}/home")
    public ResponseEntity<?> getDriverHome(@PathVariable Integer driverId) {
        Driver driver = repo.findById(driverId).orElseThrow(()-> new RuntimeException("Driver not found"));

        // Check if driver is online
        if (driver.getIsOnline() == DriverStatus.OFFLINE) {
            return ResponseEntity.ok(Map.of(
                    "status", "OFFLINE",
                    "message", "Driver is offline"
            ));
        }

        // Get pending rides near driver
        List<Ride> nearbyRides = rideService.getPendingRidesForDriver(driverId);

        // Get current ongoing ride
        Optional<Ride> currentRide = rideService.getCurrentRideForDriver(driverId);

        return ResponseEntity.ok(Map.of(
                "currentRide", currentRide.orElse(null),
                "nearbyRides", nearbyRides
        ));
    }

}
