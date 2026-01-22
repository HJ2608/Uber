package com.firstapp.uber.service.driver;

import com.firstapp.uber.dto.driver.Driver;
import com.firstapp.uber.repository.driver.DriverRepo;
import com.firstapp.uber.repository.driver.DriverRepository;
import org.springframework.stereotype.Service;

@Service
public class DriverService {
    private final DriverRepo driverRepo;
    private final DriverRepository driverRepository;

    public DriverService(DriverRepo driverRepo,
                         DriverRepository driverRepository) {
        this.driverRepo = driverRepo;
        this.driverRepository = driverRepository;
    }

    public Integer findDriverIdByUserId(Integer userId) {
        return driverRepository.findByUser_Id(userId)
                .map(Driver::getId)
                .orElse(null);
    }
}
