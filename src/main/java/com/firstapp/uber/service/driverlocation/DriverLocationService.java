package com.firstapp.uber.service.driverlocation;


import com.firstapp.uber.dto.driverlocation.DriverLocation;

import java.util.Optional;


public interface DriverLocationService {

    public DriverLocation updateLocation(Integer driverId, double lat, double lng);
    public Optional<DriverLocation> findByDriverId(Integer driverId);
    public DriverLocation getOrThrow(Integer driverId);
}
