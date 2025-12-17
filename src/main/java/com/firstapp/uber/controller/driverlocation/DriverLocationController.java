package com.firstapp.uber.controller.driverlocation;

import com.firstapp.uber.repository.driverlocation.DriverLocationRepo;
import com.firstapp.uber.service.driverlocation.DriverLocationService;
import com.firstapp.uber.dto.driverlocation.DriverLocation;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/driver-location")
public class DriverLocationController {

    private final DriverLocationService service;
    private final DriverLocationRepo repo;

    public DriverLocationController(DriverLocationService service, DriverLocationRepo repo) {
        this.service = service;
        this.repo = repo;
    }

    public record UpdateLocationRequest(double lat, double lng) {}

    @PutMapping("/{driverId}")
    public DriverLocation update(
            @PathVariable Integer driverId,
            @RequestBody UpdateLocationRequest req
    ) {
        return service.updateLocation(driverId, req.lat(), req.lng());
    }
    @GetMapping("/{driverId}")
    public DriverLocation getLocation(@PathVariable Integer driverId) {
        return service.findByDriverId(driverId)
                .orElseThrow();
    }
}

