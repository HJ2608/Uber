package com.firstapp.uber.service.driverlocation;

import com.firstapp.uber.repository.driverlocation.DriverLocationRepo;
import com.firstapp.uber.dto.driverlocation.DriverLocation;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DriverLocationServiceImpl implements DriverLocationService{

    private final DriverLocationRepo driverLocationRepo;
    public DriverLocationServiceImpl(DriverLocationRepo driverLocationRepo){
        this.driverLocationRepo = driverLocationRepo;
    }

    public DriverLocation updateLocation(Integer driverId, double lat, double lng){
        return driverLocationRepo.upsertLocation(driverId,lat,lng);
    }
    public Optional<DriverLocation> findByDriverId(Integer driverId) {
        return driverLocationRepo.findDriverLocationById(driverId);
    }
    public DriverLocation getOrThrow(Integer driverId) {
        return driverLocationRepo.findDriverLocationById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver location not found for id " + driverId));
    }
}
