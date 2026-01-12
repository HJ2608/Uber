package com.firstapp.uber.repository.driverlocation;

import com.firstapp.uber.dto.driverlocation.DriverLocation;
import com.firstapp.uber.dto.ride.Ride;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class DriverLocationRepo {
    private final DriverLocationRepository repo;
    public DriverLocationRepo(DriverLocationRepository driverLocationRepository) {
        this.repo = driverLocationRepository;
    }

    public DriverLocation upsertLocation(Integer driverId, double lat, double lng) {
        DriverLocation location = new DriverLocation(
            driverId,
            BigDecimal.valueOf(lat),
            BigDecimal.valueOf(lng),
                LocalDateTime.now()
        );
        return repo.save(location);
    }

    public Optional<DriverLocation> findDriverLocationById(Integer driverId) {
        return repo.findById(driverId);
    }

    public Ride assignDriver(Integer ride_id, Integer driver_id) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    public List<Integer> findNearbyAvailableDrivers(
            double pickupLat,
            double pickupLng
    ) {
        return repo.findNearbyAvailableDrivers(pickupLat, pickupLng);
    }


}
