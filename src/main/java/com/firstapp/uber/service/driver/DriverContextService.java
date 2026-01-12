package com.firstapp.uber.service.driver;

import com.firstapp.uber.auth.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class DriverContextService {
    private final DriverService driverService;
    public DriverContextService(DriverService driverService) {
        this.driverService = driverService;
    }
    public Integer getDriverId(Authentication auth) {

        if (auth == null || auth.getPrincipal() == null) {
            throw new RuntimeException("Unauthenticated access");
        }

        CustomUserDetails user =
                (CustomUserDetails) auth.getPrincipal();

        if (!"DRIVER".equals(user.getRole())) {
            throw new RuntimeException("User is not a driver");
        }

        Integer driverId =
                driverService.findDriverIdByUserId(user.getUserId());

        if (driverId == null) {
            throw new RuntimeException(
                    "Driver profile not created for userId=" + user.getUserId()
            );
        }

        return driverId;
    }
}
