package com.firstapp.uber.scheduler;

import com.firstapp.uber.dto.cab.Cab;
import com.firstapp.uber.dto.driver.Driver;
import com.firstapp.uber.dto.driverlocation.DriverLocation;
import com.firstapp.uber.repository.cab.CabRepository;
import com.firstapp.uber.repository.driver.DriverRepository;
import com.firstapp.uber.repository.driverlocation.DriverLocationRepo;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Component
public class DriverLocationScheduler {

    private final CabRepository cabRepository;
    private final DriverRepository driverRepo;
    private final DriverLocationRepo driverLocationRepo;
    private final Random random = new Random();

    public DriverLocationScheduler(
            CabRepository cabRepository,
            DriverRepository driverRepo,
            DriverLocationRepo driverLocationRepo
    ) {
        this.cabRepository = cabRepository;
        this.driverRepo = driverRepo;
        this.driverLocationRepo = driverLocationRepo;
    }

    @Scheduled(fixedRate = 5*60*1000)
    @Transactional
    public void updateDriverLocations() {

        List<Cab> activeCabs = cabRepository.findByIsActiveTrue();
        if (activeCabs.isEmpty()) return;

        List<Integer> cabIds = activeCabs.stream()
                .map(Cab::getId)
                .toList();

        List<Driver> drivers = driverRepo.findByCabIdIn(cabIds);
        if (drivers.isEmpty()) return;

        for (Driver driver : drivers) {
            Integer driverId = driver.getId();

            Optional<DriverLocation> existing =
                    driverLocationRepo.findDriverLocationById(driverId);

            double baseLat = existing.map(l -> l.getLat().doubleValue()).orElse(28.6139);
            double baseLng = existing.map(l -> l.getLng().doubleValue()).orElse(77.2090);

            double deltaLat = (random.nextDouble() - 0.5) * 0.003;
            double deltaLng = (random.nextDouble() - 0.5) * 0.003;

            double newLat = baseLat + deltaLat;
            double newLng = baseLng + deltaLng;

            driverLocationRepo.upsertLocation(
                    driverId,
                    newLat,
                    newLng
            );
        }

        System.out.println("[Scheduler] Updated locations for active drivers: " + drivers.size());
    }
}
