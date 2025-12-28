package com.firstapp.uber.service.driver;

import com.firstapp.uber.dto.driver.DriverRequest;
import com.firstapp.uber.dto.ride.Ride;
import com.firstapp.uber.repository.driver.DriverRepo;
import com.firstapp.uber.repository.driver.DriverRepository;
import jakarta.transaction.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DriverNotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final DriverRepo driverRepository;

    public DriverNotificationService(SimpMessagingTemplate messagingTemplate, DriverRepo driverRepository) {
        this.messagingTemplate = messagingTemplate;
        this.driverRepository = driverRepository;
    }

    @Transactional
    public void setDriverOnline(Integer driverId) {
        driverRepository.setOnline(driverId);
    }

    @Transactional
    public void setDriverOffline(Integer driverId) {
        driverRepository.setOffline(driverId);
    }

    public void sendRideRequest(Integer driverId, DriverRequest request) {
        messagingTemplate.convertAndSendToUser(
                driverId.toString(),
                "/queue/ride-request",
                request
        );
    }

    public void notifyRideTaken(Integer rideId, Integer acceptedDriverId, List<Integer> otherDrivers) {
        otherDrivers.forEach(driverId -> {
            if (!driverId.equals(acceptedDriverId)) {
                messagingTemplate.convertAndSendToUser(
                        driverId.toString(),
                        "/queue/ride-cancelled",
                        "Ride already accepted"
                );
            }
        });
    }

    public Optional<Ride> getCurrentRideForDriver(Integer driverId) {
        List<String> activeStatuses = List.of("ASSIGNED", "ONGOING");
        return rideRepo.findTopByDriverIdAndStatusInOrderByRideIdDesc(driverId, activeStatuses);
    }

}

