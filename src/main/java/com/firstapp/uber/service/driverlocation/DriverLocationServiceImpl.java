package com.firstapp.uber.service.driverlocation;

import com.firstapp.uber.repository.driverlocation.DriverLocationRedisRepo;
import com.firstapp.uber.repository.driverlocation.DriverLocationRepo;
import com.firstapp.uber.dto.driverlocation.DriverLocation;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DriverLocationServiceImpl implements DriverLocationService{

    private final DriverLocationRepo driverLocationRepo;
    private final DriverLocationRedisRepo redisRepo;
    public DriverLocationServiceImpl(DriverLocationRepo driverLocationRepo,
                                     DriverLocationRedisRepo redisRepo) {
        this.driverLocationRepo = driverLocationRepo;
        this.redisRepo = redisRepo;
    }

    @Override
    public DriverLocation updateLocation(Integer driverId, double lat, double lng){
        redisRepo.upsertLocation(driverId, lat, lng);
        return redisRepo.getLocation(driverId).orElseGet(() ->
                driverLocationRepo.upsertLocation(driverId, lat, lng)
        );
    }

    @Override
    public Optional<DriverLocation> findByDriverId(Integer driverId) {
        return redisRepo.getLocation(driverId).or(() -> driverLocationRepo.findDriverLocationById(driverId));
    }
    @Override
    public DriverLocation getOrThrow(Integer driverId) {
        return findByDriverId(driverId)
                .orElseThrow(() -> new RuntimeException("Driver location not found for id " + driverId));
    }
}
