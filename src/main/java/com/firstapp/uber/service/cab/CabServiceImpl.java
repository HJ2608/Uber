package com.firstapp.uber.service.cab;

import com.firstapp.uber.dto.cab.Cab;
import com.firstapp.uber.repository.cab.CabRepository;
import com.firstapp.uber.repository.driver.DriverRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CabServiceImpl implements CabService {
    private final CabRepository cabRepository;
    private final DriverRepository driverRepository;

    public CabServiceImpl(CabRepository cabRepository, DriverRepository driverRepository) {
        this.cabRepository = cabRepository;
        this.driverRepository = driverRepository;
    }

    @Override
    public Cab saveCab(Cab cab) {
        return cabRepository.save(cab);
    }

    @Override
    public Cab getCabById(Integer cabId) {
        return cabRepository.findById(cabId).orElse(null);
    }

    @Override
    public List<Cab> getAllCabs() {
        return cabRepository.findAll();
    }

    @Override
    public Cab updateCab(Cab cab) {
        return cabRepository.save(cab);
    }

    @Override
    public void deleteCab(Integer cabId) {
        cabRepository.deleteById(cabId);
    }

    @Override
    public boolean assignDriverToCab(Integer cabId, Integer driverId) {
        var cabOpt = cabRepository.findById(cabId);
        var driverOpt = driverRepository.findById(driverId);
        if (cabOpt.isPresent() && driverOpt.isPresent()) {
            var driver = driverOpt.get();
            driver.setCabId(cabId);
            driverRepository.save(driver);
            return true;
        }
        return false;
    }
}
